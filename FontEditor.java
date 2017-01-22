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

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Color;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class FontEditor
    extends JFrame
    implements java.awt.event.ItemListener,
               java.awt.event.ActionListener,
               FontMap.TileSelectListener,
               TileEditor.TileChangedListener {

    private JPanel contentPane;

    private FontMap fontMap;
    private TileEditor tileEditor;

    private JComboBox fontSelector;

    private JRadioButton color1;
    private JRadioButton color2;
    private JRadioButton color3;
    javax.swing.ButtonGroup colorGroup;

    private byte romImage[] = null;
    private int fontOffset = -1;
    private int nameOffset = -1;

    private int fontCount = 3;
    private int fontHeaderSize = 130;
    private int fontSize = 0xe96;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FontEditor frame = new FontEditor();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public FontEditor() {
        setTitle("Font Editor");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 415, 324);
        setResizable(false);

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

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        fontMap = new FontMap();
        fontMap.setBounds(10, 42, 128, 146);
        fontMap.setTileSelectListener(this);
        contentPane.add(fontMap);

        tileEditor = new TileEditor();
        tileEditor.setBounds(148, 11, 240, 240);
        tileEditor.setTileChangedListener(this);
        contentPane.add(tileEditor);

        fontSelector = new JComboBox();
        fontSelector.setBounds(10, 11, 128, 20);
        fontSelector.setEditable(true);
        fontSelector.addItemListener(this);
        contentPane.add(fontSelector);

        color1 = new JRadioButton("1");
        color1.setBounds(10, 220, 37, 23);
        color1.addItemListener(this);
        color1.setMnemonic(KeyEvent.VK_1);
        contentPane.add(color1);

        color2 = new JRadioButton("2");
        color2.setBounds(49, 220, 37, 23);
        color2.addItemListener(this);
        color2.setMnemonic(KeyEvent.VK_2);
        contentPane.add(color2);

        color3 = new JRadioButton("3");
        color3.setBounds(88, 220, 37, 23);
        color3.addItemListener(this);
        color3.setSelected(true);
        color3.setMnemonic(KeyEvent.VK_3);
        contentPane.add(color3);

        colorGroup = new javax.swing.ButtonGroup();
        colorGroup.add(color1);
        colorGroup.add(color2);
        colorGroup.add(color3);

        JLabel lblColor = new JLabel("Color:");
        lblColor.setBounds(10, 199, 46, 14);
        contentPane.add(lblColor);
    }

    private int findFontOffset() {
        int i = 30 * 0x4000;  // Bank 30.
        while (i < 31 * 0x4000) {
            // Looks for the end of graphics font.
            if (romImage[i] == 0 &&
                    romImage[i + 1] == 0 &&
                    romImage[i + 2] == 0 &&
                    romImage[i + 3] == 0 &&
                    romImage[i + 4] == (byte)0xd0 &&
                    romImage[i + 5] == (byte)0x90 &&
                    romImage[i + 6] == 0x50 &&
                    romImage[i + 7] == 0x50 &&
                    romImage[i + 8] == 0x50 &&
                    romImage[i + 9] == 0x50 &&
                    romImage[i + 10] == 0x50 &&
                    romImage[i + 11] == 0x50 &&
                    romImage[i + 12] == (byte)0xd0 &&
                    romImage[i + 13] == (byte)0x90 &&
                    romImage[i + 14] == 0 &&
                    romImage[i + 15] == 0) {
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
            if (romImage[i] == 'G' &&  // gray
                    romImage[i + 1] == 'R' &&
                    romImage[i + 2] == 'A' &&
                    romImage[i + 3] == 'Y' &&
                    romImage[i + 4] == 0 &&
                    romImage[i + 5] == 'I' &&  // inv
                    romImage[i + 6] == 'N' &&
                    romImage[i + 7] == 'V' &&
                    romImage[i + 8] == ' ' &&
                    romImage[i + 9] == 0 &&
                    romImage[i + 10] == 0 &&  // empty
                    romImage[i + 11] == 0 &&
                    romImage[i + 12] == 0 &&
                    romImage[i + 13] == 0 &&
                    romImage[i + 14] == 0 &&
                    romImage[i + 15] == 0 &&  // empty
                    romImage[i + 16] == 0 &&
                    romImage[i + 17] == 0 &&
                    romImage[i + 18] == 0 &&
                    romImage[i + 19] == 0 &&
                    romImage[i + 20] == 0 &&  // empty
                    romImage[i + 21] == 0 &&
                    romImage[i + 22] == 0 &&
                    romImage[i + 23] == 0 &&
                    romImage[i + 24] == 0 &&
                    romImage[i + 25] == 0 &&  // empty
                    romImage[i + 26] == 0 &&
                    romImage[i + 27] == 0 &&
                    romImage[i + 28] == 0 &&
                    romImage[i + 29] == 0) {
                        return i - 15;
                    }
            ++i;
        }
        return -1;
    }

    private String fontName(int font) {
        int fontNameSize = 5;
        String s = new String();
        s = s + (char)romImage[nameOffset + font * fontNameSize + 0];
        s = s + (char)romImage[nameOffset + font * fontNameSize + 1];
        s = s + (char)romImage[nameOffset + font * fontNameSize + 2];
        s = s + (char)romImage[nameOffset + font * fontNameSize + 3];
        return s;
    }

    private void populateFontSelector() {
        fontSelector.removeAllItems();
        for (int i = 0; i < fontCount; ++i) {
            fontSelector.addItem(fontName(i));
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
                if (fontSelector.getSelectedIndex() != -1) {
                    int selectedFontOffset = fontOffset + index * fontSize + fontHeaderSize;
                    fontMap.setFontOffset(selectedFontOffset);
                    tileEditor.setFontOffset(selectedFontOffset);
                }
            } else if (colorGroup != null) {
                // Handle color switch.
                switch (colorGroup.getSelection().getMnemonic()) {
                    case KeyEvent.VK_1:
                        setColor(1);
                        break;
                    case KeyEvent.VK_2:
                        setColor(2);
                        break;
                    case KeyEvent.VK_3:
                        setColor(3);
                        break;
                    default:
                        assert false;
                }
            }
        }
    }

    void setColor(int color) {
        assert color >= 1 && color <= 3;
        tileEditor.setColor(color);
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
			fontMap.load(chooser.getSelectedFile());
		}
    }

    void showSaveDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("LSDj Font", "lsdfnt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = chooser.getSelectedFile().toString();
			if (!filename.endsWith("lsdfnt")) {
				filename += ".lsdfnt";
			}
			fontMap.save(filename);
		}
    }
}
