package fontEditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import structures.LSDJFont;
import utils.FontIO;
import utils.JFileChooserFactory;
import utils.JFileChooserFactory.FileOperation;
import utils.JFileChooserFactory.FileType;
import utils.RomUtilities;

public class FontEditor extends JFrame implements java.awt.event.ItemListener, java.awt.event.ActionListener,
        FontMap.TileSelectListener, TileEditor.TileChangedListener {

    private static final long serialVersionUID = 5296681614787155252L;

    private final FontMap fontMap;
    private final TileEditor tileEditor;

    private final JComboBox<String> fontSelector;

    private byte romImage[] = null;
    private int fontOffset = -1;
    private int selectedFontOffset = -1;


    private int previousSelectedFont = -1;

    public FontEditor() {
        setTitle("Font Editor");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setResizable(true);
        GridBagConstraints constraints = new GridBagConstraints();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        createFileMenu(menuBar);

        createEditMenu(menuBar);

        GridBagLayout layout = new GridBagLayout();
        JPanel contentPane = new JPanel();
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

        fontSelector = new JComboBox<>();
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

        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
        FontEditorColorSelector colorSelector = new FontEditorColorSelector(colorPanel);
        colorSelector.addChangeEventListener(new ChangeEventListener() {
            @Override
            public void onChange(int color, ChangeEventMouseSide side) {
                if (side == ChangeEventMouseSide.LEFT)
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

        JPanel shiftButtonPanel = new JPanel();
        shiftButtonPanel.setLayout(new BoxLayout(shiftButtonPanel, BoxLayout.X_AXIS));

        BufferedImage shiftUpImage = loadImage("/shift_up.png");
        JButton shiftUp = new JButton();
        setUpButtonIconOrText(shiftUp, shiftUpImage);
        shiftUp.addActionListener(e -> tileEditor.shiftUp(tileEditor.getTile()));
        shiftButtonPanel.add(shiftUp);


        BufferedImage shiftDownImage = loadImage("/shift_down.png");
        JButton shiftDown = new JButton();
        setUpButtonIconOrText(shiftDown, shiftDownImage);
        shiftDown.addActionListener(e -> tileEditor.shiftDown(tileEditor.getTile()));
        shiftButtonPanel.add(shiftDown);


        BufferedImage shiftLeftImage = loadImage("/shift_left.png");
        JButton shiftLeft = new JButton();
        setUpButtonIconOrText(shiftLeft, shiftLeftImage);
        shiftLeft.addActionListener(e -> tileEditor.shiftLeft(tileEditor.getTile()));
        shiftButtonPanel.add(shiftLeft);

        BufferedImage shiftRightImage = loadImage("/shift_right.png");
        JButton shiftRight = new JButton();
        setUpButtonIconOrText(shiftRight, shiftRightImage);
        shiftRight.addActionListener(e -> tileEditor.shiftRight(tileEditor.getTile()));
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

    private void createFileMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save...");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(this);
        fileMenu.add(saveMenuItem);

        JMenuItem importBitmapMenuItem = new JMenuItem("Import bitmap...");
        importBitmapMenuItem.setMnemonic(KeyEvent.VK_I);
        importBitmapMenuItem.addActionListener(e -> importBitmap());
        fileMenu.add(importBitmapMenuItem);

        JMenuItem importAllMenuItem = new JMenuItem("Import all fonts...");
        importAllMenuItem.setMnemonic(KeyEvent.VK_G);
        importAllMenuItem.addActionListener(e -> importAllFonts());
        fileMenu.add(importAllMenuItem);

        JMenuItem exportBitmapMenuItem = new JMenuItem("Export bitmap...");
        exportBitmapMenuItem.setMnemonic(KeyEvent.VK_E);
        exportBitmapMenuItem.addActionListener(e -> exportBitmap());
        fileMenu.add(exportBitmapMenuItem);

        JMenuItem exportAllMenuItem = new JMenuItem("Export all fonts...");
        exportAllMenuItem.setMnemonic(KeyEvent.VK_F);
        exportAllMenuItem.addActionListener(e -> exportAllFonts());
        fileMenu.add(exportAllMenuItem);
    }

    private void createEditMenu(JMenuBar menuBar) {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        JMenuItem copyMenuItem = new JMenuItem("Copy Tile");
        copyMenuItem.addActionListener(this);
        copyMenuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem("Paste Tile");
        pasteMenuItem.setMnemonic(KeyEvent.VK_P);
        pasteMenuItem.addActionListener(this);
        editMenu.add(pasteMenuItem);
    }

    private BufferedImage loadImage(String iconPath) {
        try {
            return javax.imageio.ImageIO.read(getClass().getResource(iconPath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private void setUpButtonIconOrText(JButton button, BufferedImage image) {
        if (image != null)
            button.setIcon(new ImageIcon(image));
        else
            button.setText("Shift Down");
    }

    private void populateFontSelector() {
        fontSelector.removeAllItems();
        for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
            fontSelector.addItem(RomUtilities.getFontName(romImage, i));
        }
    }

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
        fontMap.setRomImage(romImage);
        tileEditor.setRomImage(romImage);

        fontOffset = RomUtilities.findFontOffset(romImage);
        if (fontOffset == -1) {
            System.err.println("Could not find font offset!");
        }
        int nameOffset = RomUtilities.findFontNameOffset(romImage);
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
                    selectedFontOffset = fontOffset + index * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
                    fontMap.setFontOffset(selectedFontOffset);
                    tileEditor.setFontOffset(selectedFontOffset);
                }
            }
        }
    }

    private void setColor(int color) {
        assert color >= 1 && color <= 3;
        tileEditor.setColor(color);
    }

    private void setRightColor(int color) {
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
        if (cmd.equals("Copy Tile")) {
            tileEditor.copyTile();
        } else if (cmd.equals("Paste Tile")) {
            tileEditor.pasteTile();
        } else if (cmd.equals("Open...")) {
            showOpenDialog();
        } else if (cmd.equals("Save...")) {
            showSaveDialog();
        } else if (cmd.equals("comboBoxChanged") && e.getSource() instanceof JComboBox) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getSelectedIndex() != -1) {
                previousSelectedFont = cb.getSelectedIndex();
            }
        } else if (cmd.equals("comboBoxEdited") && e.getSource() instanceof JComboBox) {
            @SuppressWarnings("unchecked") JComboBox cb = (JComboBox<String>) e.getSource();
            String selectedItem = (String) cb.getSelectedItem();
            if (cb.getSelectedIndex() == -1 && selectedItem != null) {
                int index = previousSelectedFont;
                RomUtilities.setFontName(romImage, index, selectedItem);
                populateFontSelector();
                fontSelector.setSelectedIndex(index);
                cb.setSelectedIndex(index);
            } else {
                previousSelectedFont = cb.getSelectedIndex();
            }
        } else {
            assert false;
        }
    }

    private void showOpenDialog() {
        try {
            JFileChooser chooser = JFileChooserFactory.createChooser("Open Font", FileType.Lsdfnt, FileOperation.Load);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                JFileChooserFactory.recordNewBaseFolder(f.getParent());
                String fontName;
                fontName = FontIO.loadFnt(f, romImage, selectedFontOffset);
                tileEditor.generateShadedAndInvertedTiles();
                RomUtilities.setFontName(romImage, fontSelector.getSelectedIndex(), fontName);
                tileEditor.tileChanged();
                tileChanged();
                // Refresh the name list.
                int previousIndex = fontSelector.getSelectedIndex();
                populateFontSelector();
                fontSelector.setSelectedIndex(previousIndex);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Couldn't open fnt file.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSaveDialog() {
        try {
            JFileChooser chooser = JFileChooserFactory.createChooser("Save Font", FileType.Lsdfnt, FileOperation.Save);

            if (fontSelector.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Couldn't read the selected font name.");
                return;
            }

            String fontName = (String) fontSelector.getSelectedItem();
            int returnVal = chooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                JFileChooserFactory.recordNewBaseFolder(f.getParent());
                String filename = f.toString();
                if (!filename.endsWith("lsdfnt")) {
                    //noinspection UnusedAssignment
                    filename = filename.concat(".lsdfnt");
                }
                FontIO.saveFnt(f, fontName, romImage, selectedFontOffset);

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Couldn't save fnt file.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void importBitmap() {
        // File Choose
        JFileChooser chooser = JFileChooserFactory.createChooser("Import Font Image", FileType.Png, FileOperation.Load);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File bitmap = chooser.getSelectedFile();
            JFileChooserFactory.recordNewBaseFolder(bitmap.getParent());
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

    private void exportBitmap() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Export Font " + RomUtilities.getFontName(romImage, previousSelectedFont), FileType.Png, FileOperation.Save);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            JFileChooserFactory.recordNewBaseFolder(f.getParent());
            String filename = f.toString();
            if (!filename.endsWith("png")) {
                filename += ".png";
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
                ImageIO.write(image, "PNG", new File(filename));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Couldn't export the font map.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void importAllFonts() {
        int backupFontNumber = previousSelectedFont;
        for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
            selectedFontOffset = fontOffset + i * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
            fontMap.setFontOffset(selectedFontOffset);
            tileEditor.setFontOffset(selectedFontOffset);
            fontSelector.setSelectedIndex(i);
            importBitmap();
        }

        selectedFontOffset = fontOffset + backupFontNumber * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
        fontMap.setFontOffset(selectedFontOffset);
        tileEditor.setFontOffset(selectedFontOffset);
        fontSelector.setSelectedIndex(backupFontNumber);

    }

    private void exportAllFonts() {
        int backupFontNumber = previousSelectedFont;
        for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
            selectedFontOffset = fontOffset + i * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
            fontMap.setFontOffset(selectedFontOffset);
            tileEditor.setFontOffset(selectedFontOffset);
            fontSelector.setSelectedIndex(i);
            exportBitmap();
        }

        selectedFontOffset = fontOffset + backupFontNumber * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
        fontMap.setFontOffset(selectedFontOffset);
        tileEditor.setFontOffset(selectedFontOffset);
        fontSelector.setSelectedIndex(backupFontNumber);

    }

}
