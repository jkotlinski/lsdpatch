package fontEditor;

/** Copyright (C) 2017 by Johan Kotlinski

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE. */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FontEditor extends JFrame implements java.awt.event.ItemListener, java.awt.event.ActionListener,
		FontMap.TileSelectListener, TileEditor.TileChangedListener {

	private static final long serialVersionUID = 5296681614787155252L;

	private JPanel contentPane;

	private FontMap fontMap;
	private TileEditor tileEditor;

	private JComboBox<String> fontSelector;

	private JPanel leftColorPanel;
	private FontEditorColorSelector leftColorSelector;

	private JPanel rightColorPanel;
	private FontEditorColorSelector rightColorSelector;

	private JButton shiftUp;
	private JButton shiftDown;
	private JButton shiftLeft;
	private JButton shiftRight;

	private byte romImage[] = null;
	private int fontOffset = -1;
	private int nameOffset = -1;

	private int fontCount = 3;
	private int fontHeaderSize = 130;
	private int fontSize = 0xe96;

	int previousSelectedFont = -1;

	public FontEditor() {
		setTitle("Font Editor");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setResizable(true);
		GridBagConstraints constraints = new GridBagConstraints();

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);

		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.setMnemonic(KeyEvent.VK_O);
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save...");
		mntmSave.setMnemonic(KeyEvent.VK_S);
		mntmSave.addActionListener(this);
		mnFile.add(mntmSave);

		JMenuItem mntmImport = new JMenuItem("Import bitmap...");
		mntmImport.setMnemonic(KeyEvent.VK_I);
		mntmImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importBitmap();
			}
		});

		mnFile.add(mntmImport);

		JMenuItem mntmExport = new JMenuItem("Export bitmap...");
		mntmExport.setMnemonic(KeyEvent.VK_E);
		mntmExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportBitmap();
			}
		});
		mnFile.add(mntmExport);

		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mnEdit);

		JMenuItem mntmCopy = new JMenuItem("Copy Tile");
		mntmCopy.addActionListener(this);
		mntmCopy.setMnemonic(KeyEvent.VK_C);
		mnEdit.add(mntmCopy);

		JMenuItem mntmPaste = new JMenuItem("Paste Tile");
		mntmPaste.setMnemonic(KeyEvent.VK_P);
		mntmPaste.addActionListener(this);
		mnEdit.add(mntmPaste);

		GridBagLayout layout = new GridBagLayout();
		contentPane = new JPanel();
		contentPane.setLayout(layout);
		setContentPane(contentPane);

		tileEditor = new TileEditor();
		tileEditor.setMinimumSize(new Dimension(240, 240));
		tileEditor.setPreferredSize(new Dimension(240, 240));
		tileEditor.setTileChangedListener(this);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridheight = 7;
		contentPane.add(tileEditor, constraints);

		fontSelector = new JComboBox<String>();
		fontSelector.setEditable(true);
		fontSelector.addItemListener(this);
		fontSelector.addActionListener(this);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 2;
		contentPane.add(fontSelector, constraints);

		JLabel lblColorLeft = new JLabel("Left click");
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.ipady = 5;
		constraints.gridwidth = 1;
		constraints.ipadx = 8;
		contentPane.add(lblColorLeft, constraints);

		JLabel lblColorRight = new JLabel("Right click");
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.ipady = 5;
		constraints.gridwidth = 1;
		contentPane.add(lblColorRight, constraints);

		leftColorPanel = new JPanel();
		leftColorPanel.setLayout(new BoxLayout(leftColorPanel, BoxLayout.X_AXIS));
		leftColorSelector = new FontEditorColorSelector(leftColorPanel);
		leftColorSelector.addChangeEventListener(new ChangeEventListener() {
			@Override
			public void onChange(int color) {
				setColor(color);
			}
		});
		leftColorSelector.setSelectedColor(1);
		setColor(1);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.ipady = 0;
		constraints.ipadx = 0;
		contentPane.add(leftColorPanel, constraints);

		rightColorPanel = new JPanel();
		rightColorPanel.setLayout(new BoxLayout(rightColorPanel, BoxLayout.X_AXIS));
		rightColorSelector = new FontEditorColorSelector(rightColorPanel);
		rightColorSelector.addChangeEventListener(new ChangeEventListener() {
			@Override
			public void onChange(int color) {
				setRightColor(color);
			}
		});
		rightColorSelector.setSelectedColor(3);
		setRightColor(3);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 1;
		constraints.gridy = 3;
		contentPane.add(rightColorPanel, constraints);

		fontMap = new FontMap();
		fontMap.setMinimumSize(new Dimension(128, 16 * 8 * 2));
		fontMap.setPreferredSize(new Dimension(128, 16 * 8 * 2));
		fontMap.setTileSelectListener(this);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.ipadx = 0;
		constraints.ipady = 5;
		constraints.weighty = 1;
		constraints.gridwidth = 2;
		contentPane.add(fontMap, constraints);

		setMinimumSize(layout.preferredLayoutSize(contentPane));

		shiftUp = new JButton("Shift up");
		shiftUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftUp(tileEditor.getTile());
			}
		});
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weighty = 0;
		constraints.gridwidth = 1;
		contentPane.add(shiftUp, constraints);

		shiftDown = new JButton("Shift down");
		shiftDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftDown(tileEditor.getTile());
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 5;
		contentPane.add(shiftDown, constraints);

		shiftLeft = new JButton("Shift left");
		shiftLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftLeft(tileEditor.getTile());
			}
		});
		constraints.gridx = 0;
		constraints.gridy = 6;
		contentPane.add(shiftLeft, constraints);

		shiftRight = new JButton("Shift right");
		shiftRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftRight(tileEditor.getTile());
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 6;
		contentPane.add(shiftRight, constraints);
	}

	private int findFontOffset() {
		int i = 30 * 0x4000; // Bank 30.
		while (i < 31 * 0x4000) {
			// Looks for the end of graphics font.
			if (romImage[i] == 0 && romImage[i + 1] == 0 && romImage[i + 2] == 0 && romImage[i + 3] == 0
					&& romImage[i + 4] == (byte) 0xd0 && romImage[i + 5] == (byte) 0x90 && romImage[i + 6] == 0x50
					&& romImage[i + 7] == 0x50 && romImage[i + 8] == 0x50 && romImage[i + 9] == 0x50
					&& romImage[i + 10] == 0x50 && romImage[i + 11] == 0x50 && romImage[i + 12] == (byte) 0xd0
					&& romImage[i + 13] == (byte) 0x90 && romImage[i + 14] == 0 && romImage[i + 15] == 0) {
				return i + 16;
			}
			++i;
		}
		return -1;
	}

	private int findNameOffset() {
		// Palette names are in bank 27.
		int i = 0x4000 * 27;
		while (i < 0x4000 * 28) {
			if (romImage[i] == 'G' && // gray
					romImage[i + 1] == 'R' && romImage[i + 2] == 'A' && romImage[i + 3] == 'Y' && romImage[i + 4] == 0
					&& romImage[i + 5] == 'I' && // inv
					romImage[i + 6] == 'N' && romImage[i + 7] == 'V' && romImage[i + 8] == ' ' && romImage[i + 9] == 0
					&& romImage[i + 10] == 0 && // empty
					romImage[i + 11] == 0 && romImage[i + 12] == 0 && romImage[i + 13] == 0 && romImage[i + 14] == 0
					&& romImage[i + 15] == 0 && // empty
					romImage[i + 16] == 0 && romImage[i + 17] == 0 && romImage[i + 18] == 0 && romImage[i + 19] == 0
					&& romImage[i + 20] == 0 && // empty
					romImage[i + 21] == 0 && romImage[i + 22] == 0 && romImage[i + 23] == 0 && romImage[i + 24] == 0
					&& romImage[i + 25] == 0 && // empty
					romImage[i + 26] == 0 && romImage[i + 27] == 0 && romImage[i + 28] == 0 && romImage[i + 29] == 0) {
				return i - 15;
			}
			++i;
		}
		return -1;
	}

	private String getFontName(int font) {
		int fontNameSize = 5;
		String s = new String();
		s = s + (char) romImage[nameOffset + font * fontNameSize + 0];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 1];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 2];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 3];
		return s;
	}

	private void setFontName(int fontIndex, String fontName) {
		while (fontName.length() < 4) {
			fontName += " ";
		}
		int fontNameSize = 5;
		romImage[nameOffset + fontIndex * fontNameSize + 0] = (byte) fontName.charAt(0);
		romImage[nameOffset + fontIndex * fontNameSize + 1] = (byte) fontName.charAt(1);
		romImage[nameOffset + fontIndex * fontNameSize + 2] = (byte) fontName.charAt(2);
		romImage[nameOffset + fontIndex * fontNameSize + 3] = (byte) fontName.charAt(3);
		populateFontSelector();
		fontSelector.setSelectedIndex(fontIndex);
	}

	private void populateFontSelector() {
		fontSelector.removeAllItems();
		for (int i = 0; i < fontCount; ++i) {
			fontSelector.addItem(getFontName(i));
		}
	}

	public void setRomImage(byte[] romImage) {
		this.romImage = romImage;
		fontMap.setRomImage(romImage);
		tileEditor.setRomImage(romImage);

		fontOffset = findFontOffset();
		if (fontOffset == -1) {
			System.err.println("Could not find font offset!");
		}
		nameOffset = findNameOffset();
		if (nameOffset == -1) {
			System.err.println("Could not find font name offset!");
		}
		populateFontSelector();
	}

	public void itemStateChanged(java.awt.event.ItemEvent e) {
		if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
			if (e.getItemSelectable() == fontSelector) {
				// Font changed.
				int index = fontSelector.getSelectedIndex();
				if (index != -1) {
					previousSelectedFont = index;
					index = (index + 1) % 3; // Adjusts for fonts being defined in wrong order.
					int selectedFontOffset = fontOffset + index * fontSize + fontHeaderSize;
					fontMap.setFontOffset(selectedFontOffset);
					tileEditor.setFontOffset(selectedFontOffset);
				}
			}
		}
	}

	void setColor(int color) {
		assert color >= 1 && color <= 3;
		tileEditor.setColor(color);
	}

	void setRightColor(int color) {
		assert color >= 1 && color <= 3;
		tileEditor.setRightColor(color);
	}

	public void tileSelected(int tile) {
		tileEditor.setTile(tile);
	}

	public void tileChanged() {
		fontMap.repaint();
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "Copy Tile") {
			tileEditor.copyTile();
		} else if (cmd == "Paste Tile") {
			tileEditor.pasteTile();
		} else if (cmd == "Open...") {
			showOpenDialog();
		} else if (cmd == "Save...") {
			showSaveDialog();
		} else if (cmd == "comboBoxChanged" && e.getSource() instanceof JComboBox<?>) {
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			if (cb.getSelectedIndex() != -1) {
				previousSelectedFont = cb.getSelectedIndex();
			}
		} else if (cmd == "comboBoxEdited" && e.getSource() instanceof JComboBox<?>) {
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			if (cb.getSelectedIndex() == -1) {
				int index = previousSelectedFont;
				setFontName(index, (String) cb.getSelectedItem());
				populateFontSelector();
				cb.setSelectedIndex(index);
			} else {
				previousSelectedFont = cb.getSelectedIndex();
			}
		} else {
			assert false;
		}
	}

	void showOpenDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("LSDj Font", "lsdfnt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fontName = fontMap.load(chooser.getSelectedFile());
			tileEditor.generateShadedAndInvertedTiles();
			setFontName(fontSelector.getSelectedIndex(), fontName);
		}
	}

	void showSaveDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("LSDj Font", "lsdfnt");
		chooser.setFileFilter(filter);
		String fontName = fontSelector.getSelectedItem().toString();
		chooser.setSelectedFile(new java.io.File(fontName + ".lsdfnt"));
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = chooser.getSelectedFile().toString();
			if (!filename.endsWith("lsdfnt")) {
				filename += ".lsdfnt";
			}
			fontMap.save(filename, fontName);
		}
	}

	void importBitmap() {
		// File Choose
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap (*.bmp)", "bmp");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File bitmap = chooser.getSelectedFile();
			try {
				BufferedImage image = ImageIO.read(bitmap);
				if (image.getWidth() != 64 && image.getHeight() != 72) {
					JOptionPane.showMessageDialog(this,
							"Make sure your picture has the right dimensions (64 * 72 pixels).");
					return;
				}

				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						int rgb = image.getRGB(x, y);
						float color[] = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
						int lum = (int) (color[2] * 255);

						int currentTileIndex = (y / 8) * 8 + x / 8;
						int localX = x % 8;
						int localY = y % 8;
						int col = 3;
						if (lum >= 192)
							col = 1;
						else if (lum >= 64)
							col = 2;
						else if (lum >= 0)
							col = 3;
						tileEditor.setDirectPixel(currentTileIndex, localX, localY, col);
					}
				}
				tileEditor.tileChanged();
				tileChanged();

			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Couldn't load the given picture.\n" + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	void exportBitmap() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap (*.bmp)", "bmp");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = chooser.getSelectedFile().toString();
			if (!filename.endsWith("bmp")) {
				filename += ".bmp";
			}
			BufferedImage image = new BufferedImage(64, 72, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < 72; y++) {
				for (int x = 0; x < 64; x++) {
					int color = tileEditor.getDirectPixel(x, y);
					switch (color) {
					case 0:
						image.setRGB(x, y, 0xFFFFFF);
						break;
					case 1:
						image.setRGB(x, y, 0x808080);
						break;
					case 3:
						image.setRGB(x, y, 0x000000);
						break;
					default:
						image.setRGB(x, y, 0xFFFFFF);
						break;
					}
				}
			}
			try {
				ImageIO.write(image, "BMP", new File(filename));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Couldn't export the fontmap.\n" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
