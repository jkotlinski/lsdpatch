package fontEditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import Document.Document;
import structures.LSDJFont;
import utils.EditorPreferences;
import utils.FontIO;
import utils.JFileChooserFactory;
import utils.JFileChooserFactory.FileOperation;
import utils.JFileChooserFactory.FileType;
import utils.RomUtilities;

public class FontEditor extends JFrame implements FontMap.TileSelectListener, TileEditor.TileChangedListener {

    private static final long serialVersionUID = 5296681614787155252L;

    private final FontMap fontMap;
    private final TileEditor tileEditor;

    private final JComboBox<String> fontSelector;

    private byte[] romImage = null;
    private int fontOffset = -1;
    private int selectedFontOffset = -1;
    private int previousSelectedFont = -1;

    public FontEditor(Document document) {
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
        // TODO is there a way to remove the action listener implementation from this class?
        fontSelector.addItemListener(this::fontSelectorItemChanged);
        fontSelector.addActionListener(this::fontSelectorAction);
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

        addImageButtonToPanel(shiftButtonPanel, "/shift_up.png", "Rotate up", e -> tileEditor.shiftUp(tileEditor.getTile()));
        addImageButtonToPanel(shiftButtonPanel, "/shift_down.png", "Rotate down", e -> tileEditor.shiftDown(tileEditor.getTile()));
        addImageButtonToPanel(shiftButtonPanel, "/shift_left.png", "Rotate left", e -> tileEditor.shiftLeft(tileEditor.getTile()));
        addImageButtonToPanel(shiftButtonPanel, "/shift_right.png", "Rotate right", e -> tileEditor.shiftRight(tileEditor.getTile()));

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
        pack();

        setRomImage(document.romImage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                document.setRomImage(fontMap.romImage());
            }
        });
    }

    private void addImageButtonToPanel(JPanel panel, String imagePath, String altText, ActionListener event) {
        BufferedImage buttonImage = loadImage(imagePath);
        JButton button = new JButton();
        setUpButtonIconOrText(button, buttonImage, altText);
        button.addActionListener(event);
        panel.add(button);
    }

    private int getFontDataLocation(int fontNumber) {
        return fontOffset + fontNumber * LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE;
    }

    private void addMenuEntry(JMenu destination, String name, int key, ActionListener event) {
        JMenuItem newMenuEntry = new JMenuItem(name);
        newMenuEntry.setMnemonic(key);
        newMenuEntry.addActionListener(event);
        destination.add(newMenuEntry);
    }

    private void createFileMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        addMenuEntry(fileMenu, "Open...", KeyEvent.VK_O, e -> showOpenDialog());
        addMenuEntry(fileMenu, "Save...", KeyEvent.VK_S, e -> showSaveDialog());
        addMenuEntry(fileMenu, "Import to image...", KeyEvent.VK_I, e -> importBitmap());
        addMenuEntry(fileMenu, "Import all fonts...", KeyEvent.VK_G, e -> importAllFonts());
        addMenuEntry(fileMenu, "Export to image...", KeyEvent.VK_E, e -> exportBitmap());
        addMenuEntry(fileMenu, "Export all fonts...", KeyEvent.VK_F, e -> exportAllFonts());
    }

    private void createEditMenu(JMenuBar menuBar) {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        addMenuEntry(editMenu, "Copy Tile", KeyEvent.VK_C, e -> tileEditor.copyTile());
        addMenuEntry(editMenu, "Paste Tile", KeyEvent.VK_V, e -> tileEditor.pasteTile());
    }

    private BufferedImage loadImage(String iconPath) {
        try {
            return javax.imageio.ImageIO.read(getClass().getResource(iconPath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private void setUpButtonIconOrText(JButton button, BufferedImage image, String altText) {
        if (image != null)
            button.setIcon(new ImageIcon(image));
        else
            button.setText(altText);
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

    private void fontSelectorItemChanged(java.awt.event.ItemEvent e) {
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

    private void fontSelectorAction(java.awt.event.ActionEvent e) {
        switch (e.getActionCommand()) {
            case "comboBoxChanged":
                if (fontSelector.getSelectedIndex() != -1) {
                    previousSelectedFont = fontSelector.getSelectedIndex();
                }
                break;
            case "comboBoxEdited":
                String selectedItem = (String) fontSelector.getSelectedItem();
                if (fontSelector.getSelectedIndex() == -1 && selectedItem != null) {
                    int index = previousSelectedFont;
                    RomUtilities.setFontName(romImage, index, selectedItem);
                    populateFontSelector();
                    fontSelector.setSelectedIndex(index);
                    fontSelector.setSelectedIndex(index);
                } else {
                    previousSelectedFont = fontSelector.getSelectedIndex();
                }
                break;
        }
    }

    private void showOpenDialog() {
        try {
            JFileChooser chooser = JFileChooserFactory.createChooser("Open Font", FileType.Lsdfnt, FileOperation.Load);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
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
                EditorPreferences.setLastPath("lsdfnt", f.getAbsolutePath());
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
                String filename = f.toString();
                if (!filename.endsWith("lsdfnt")) {
                    //noinspection UnusedAssignment
                    filename = filename.concat(".lsdfnt");
                }
                FontIO.saveFnt(f, fontName, romImage, selectedFontOffset);
                EditorPreferences.setLastPath("lsdfnt", f.getAbsolutePath());
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
                EditorPreferences.setLastPath("png", bitmap.getAbsolutePath());
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
            String filename = f.toString();
            if (!filename.endsWith("png")) {
                filename += ".png";
            }
            BufferedImage image = tileEditor.createImage();
            try {
                ImageIO.write(image, "PNG", new File(filename));
                EditorPreferences.setLastPath("png", f.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Couldn't export the font map.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void importAllFonts() {
        int previousSelectedFont = this.previousSelectedFont;
        for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
            selectedFontOffset = getFontDataLocation(i);
            fontMap.setFontOffset(selectedFontOffset);
            tileEditor.setFontOffset(selectedFontOffset);
            fontSelector.setSelectedIndex(i);
            importBitmap();
        }

        selectedFontOffset = getFontDataLocation(previousSelectedFont);
        fontMap.setFontOffset(selectedFontOffset);
        tileEditor.setFontOffset(selectedFontOffset);
        fontSelector.setSelectedIndex(previousSelectedFont);
    }

    private void exportAllFonts() {
        int previousSelectedFont = this.previousSelectedFont;
        for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
            selectedFontOffset = getFontDataLocation(i);
            fontMap.setFontOffset(selectedFontOffset);
            tileEditor.setFontOffset(selectedFontOffset);
            fontSelector.setSelectedIndex(i);
            exportBitmap();
        }

        selectedFontOffset = getFontDataLocation(previousSelectedFont);
        fontMap.setFontOffset(selectedFontOffset);
        tileEditor.setFontOffset(selectedFontOffset);
        fontSelector.setSelectedIndex(previousSelectedFont);

    }

}
