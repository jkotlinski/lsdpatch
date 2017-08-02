package fontEditor;

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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.FontIO;
import utils.GlobalHolder;

public class FontEditor extends JFrame implements java.awt.event.ItemListener, java.awt.event.ActionListener,
		FontMap.TileSelectListener, TileEditor.TileChangedListener {

	private static final long serialVersionUID = 5296681614787155252L;

	private JPanel contentPane;

	private FontMap fontMap;
	private TileEditor tileEditor;

	private JComboBox fontSelector;

	private JPanel colorPanel;
	private FontEditorColorSelector colorSelector;

	private JButton shiftUp = null;
	private JButton shiftDown = null;
	private JButton shiftLeft = null;
	private JButton shiftRight = null;

	private byte romImage[] = null;
	private int fontOffset = -1;
	private int selectedFontOffset = -1;
	private int nameOffset = -1;

	private int fontCount = 3;
	private int fontHeaderSize = 130;
	private int fontSize = 0xe96;

	private JPanel shiftButtonPanel = null;
	BufferedImage shiftUpImage = null;
	BufferedImage shiftDownImage = null;
	BufferedImage shiftLeftImage = null;
	BufferedImage shiftRightImage = null;

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
		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridheight = 6;
		contentPane.add(tileEditor, constraints);

		fontSelector = new JComboBox();
		fontSelector.setEditable(true);
		fontSelector.addItemListener(this);
		fontSelector.addActionListener(this);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 3;
		contentPane.add(fontSelector, constraints);

		colorPanel = new JPanel();
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
		colorSelector = new FontEditorColorSelector(colorPanel);
		colorSelector.addChangeEventListener(new ChangeEventListener() {
			@Override
			public void onChange(int color, ChangeEventMouseSide side) {
				if(side == ChangeEventMouseSide.LEFT)
					setColor(color);
				else
					setRightColor(color);
			}
		});

		setColor(1);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.ipady = 0;
		contentPane.add(colorPanel, constraints);

		shiftButtonPanel = new JPanel();
		shiftButtonPanel.setLayout(new BoxLayout(shiftButtonPanel, BoxLayout.X_AXIS));
		
		shiftUpImage = loadImage("/shift_up.png");
		shiftUp = new JButton();
		setUpButtonIconOrText(shiftUp, shiftUpImage, "Shift Up");
		shiftUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftUp(tileEditor.getTile());
			}
		});
		shiftButtonPanel.add(shiftUp);
		
		
		shiftDownImage = loadImage("/shift_down.png");
		shiftDown = new JButton();
		setUpButtonIconOrText(shiftDown, shiftDownImage, "Shift Down");
		shiftDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftDown(tileEditor.getTile());
			}
		});
		shiftButtonPanel.add(shiftDown);
		
		
		shiftLeftImage = loadImage("/shift_left.png");
		shiftLeft = new JButton();
		setUpButtonIconOrText(shiftLeft, shiftLeftImage, "Shift left");
		shiftLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftLeft(tileEditor.getTile());
			}
		});
		shiftButtonPanel.add(shiftLeft);
		
		shiftRightImage = loadImage("/shift_right.png");
		shiftRight = new JButton();
		setUpButtonIconOrText(shiftRight, shiftRightImage, "Shift right");
		shiftRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tileEditor.shiftRight(tileEditor.getTile());
			}
		});
		shiftButtonPanel.add(shiftRight);
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.NONE;
		contentPane.add(shiftButtonPanel, constraints);


		fontMap = new FontMap();
		fontMap.setMinimumSize(new Dimension(128, 16 * 8 * 2));
		fontMap.setPreferredSize(new Dimension(128, 16 * 8 * 2));
		fontMap.setTileSelectListener(this);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 1;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		contentPane.add(fontMap, constraints);

		setMinimumSize(layout.preferredLayoutSize(contentPane));

	}

	private BufferedImage loadImage(String iconPath) {
		try {
			return javax.imageio.ImageIO.read(getClass().getResource(iconPath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	private void setUpButtonIconOrText(JButton button, BufferedImage image, String text)
	{
		if (image != null)
			button.setIcon(new ImageIcon(image));
		else
			button.setText("Shift Down");		
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
					selectedFontOffset = fontOffset + index * fontSize + fontHeaderSize;
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
		} else if (cmd == "comboBoxChanged" && e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
			if (cb.getSelectedIndex() != -1) {
				previousSelectedFont = cb.getSelectedIndex();
			}
		} else if (cmd == "comboBoxEdited" && e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
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
		try {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("LSDj Font", "lsdfnt");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(GlobalHolder.get(File.class, "JFileChooser"));
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
    			GlobalHolder.set(f.getParentFile(), File.class, "JFileChooser");
				String fontName;
				fontName = FontIO.loadFnt(f, romImage, selectedFontOffset);
				tileEditor.generateShadedAndInvertedTiles();
				setFontName(fontSelector.getSelectedIndex(), fontName);
				tileEditor.tileChanged();
				tileChanged();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Couldn't open fnt file.\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	void showSaveDialog() {
		try {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("LSDj Font", "lsdfnt");
			chooser.setFileFilter(filter);
			String fontName = fontSelector.getSelectedItem().toString();
			chooser.setSelectedFile(new java.io.File(fontName + ".lsdfnt"));
			chooser.setCurrentDirectory(GlobalHolder.get(File.class, "JFileChooser"));
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
    			GlobalHolder.set(f.getParentFile(), File.class, "JFileChooser");
				String filename = f.toString();
				if (!filename.endsWith("lsdfnt")) {
					filename += ".lsdfnt";
				}
				FontIO.saveFnt(f, fontName, romImage, selectedFontOffset);

			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Couldn't save fnt file.\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	void importBitmap() {
		// File Choose
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap (*.bmp)", "bmp");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(GlobalHolder.get(File.class, "JFileChooser"));
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File bitmap = chooser.getSelectedFile();
			GlobalHolder.set(bitmap.getParentFile(), File.class, "JFileChooser");
			try {
				BufferedImage image = ImageIO.read(bitmap);
				if (image.getWidth() != 64 && image.getHeight() != 72) {
					JOptionPane.showMessageDialog(this,
							"Make sure your picture has the right dimensions (64 * 72 pixels).");
					return;
				}
				tileEditor.readImage(chooser.getSelectedFile().getName(), image);

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
		chooser.setCurrentDirectory(GlobalHolder.get(File.class, "JFileChooser"));
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			GlobalHolder.set(f.getParentFile(), File.class, "JFileChooser");
			String filename = f.toString();
			if (!filename.endsWith("bmp")) {
				filename += ".bmp";
			}
			BufferedImage image = tileEditor.createImage();

			new BufferedImage(64, 72, BufferedImage.TYPE_INT_RGB);
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
