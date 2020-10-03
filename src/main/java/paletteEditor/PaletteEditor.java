package paletteEditor;

import java.awt.*;

import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import Document.Document;
import net.miginfocom.swing.MigLayout;
import utils.JFileChooserFactory;
import utils.JFileChooserFactory.FileOperation;
import utils.JFileChooserFactory.FileType;
import utils.RomUtilities;
import utils.StretchIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PaletteEditor
        extends JFrame
        implements java.awt.event.ActionListener {
    private static final long serialVersionUID = 5286120830758415869L;

    private byte[] romImage = null;
    private int paletteOffset = -1;
    private int nameOffset = -1;
    private final int previewScale = 2;
    java.io.File clipboard;

    private final JLabel previewSongLabel = new JLabel();
    private final JLabel previewInstrLabel = new JLabel();

    private final ColorPicker colorPicker = new ColorPicker();

    private final PaletteUIEntry normalEntry = new PaletteUIEntry(colorPicker);
    private final PaletteUIEntry shadedEntry = new PaletteUIEntry(colorPicker);
    private final PaletteUIEntry alternativeEntry = new PaletteUIEntry(colorPicker);
    private final PaletteUIEntry selectionEntry = new PaletteUIEntry(colorPicker);
    private final PaletteUIEntry scrollbarEntry = new PaletteUIEntry(colorPicker);

    private JComboBox<String> paletteSelector;

    JMenuItem menuItemPaste;

    private BufferedImage songImage;
    private BufferedImage instrImage;

    private int lastSelectedPaletteIndex = -1;

    private boolean updatingSpinners = false;
    private boolean populatingPaletteSelector = false;

    private final JCheckBox desaturateButton = new javax.swing.JCheckBox("Desaturate");

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(e -> showOpenDialog());
        mnFile.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save...");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(e -> showSaveDialog());
        mnFile.add(saveMenuItem);

        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setMnemonic(KeyEvent.VK_E);
        menuBar.add(mnEdit);

        JMenuItem menuItemCopy = new JMenuItem("Copy Palette");
        menuItemCopy.addActionListener(e -> copyPalette());
        menuItemCopy.setMnemonic(KeyEvent.VK_C);
        mnEdit.add(menuItemCopy);

        menuItemPaste = new JMenuItem("Paste Palette");
        menuItemPaste.setMnemonic(KeyEvent.VK_P);
        menuItemPaste.addActionListener(e -> pastePalette());
        menuItemPaste.setEnabled(false);
        mnEdit.add(menuItemPaste);
    }

    public PaletteEditor(Document document) {
        setupMenuBar();

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setTitle("Palette Editor");
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout());

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new MigLayout());

        JPanel topRowPanel = new JPanel();
        topRowPanel.setLayout(new MigLayout());
        addPaletteSelector(topRowPanel);
        addDesaturateButton(topRowPanel);
        addRandomizeButton(topRowPanel);

        midPanel.add(topRowPanel, "wrap");
        midPanel.add(colorPicker);

        JPanel pickerPanel = new JPanel();
        pickerPanel.setLayout(new MigLayout());
        normalEntry.registerToPanel(pickerPanel, "Normal");
        shadedEntry.registerToPanel(pickerPanel, "Shaded");
        alternativeEntry.registerToPanel(pickerPanel, "Alternate");
        selectionEntry.registerToPanel(pickerPanel, "Cursor");
        scrollbarEntry.registerToPanel(pickerPanel, "Scroll Bar");

        previewSongLabel.setMinimumSize(new Dimension(160 * previewScale, 144 * previewScale));
        contentPane.add(previewSongLabel);
        previewSongLabel.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                songImagePressed(e);
            }
            @Override
            public void mouseClicked(MouseEvent e) { }
            @Override
            public void mouseReleased(MouseEvent e) { }
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
        });

        contentPane.add(midPanel);
        contentPane.add(pickerPanel);

        previewInstrLabel.setMinimumSize(new Dimension(160 * previewScale, 144 * previewScale));
        previewInstrLabel.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                instrImagePressed(e);
            }
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        contentPane.add(previewInstrLabel, "gap 10");

        listenToSpinners();

        try {
            songImage = javax.imageio.ImageIO.read(getClass().getResource("/song.bmp"));
            instrImage = javax.imageio.ImageIO.read(getClass().getResource("/instr.bmp"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        setRomImage(document.romImage());
        updatePreviewPanes();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                document.setRomImage(romImage);
            }
        });

        pack();
        setMinimumSize(getPreferredSize());

        normalEntry.selectBackground();
    }

    private void songImagePressed(MouseEvent e) {
        selectColor(songImage.getRGB(e.getX() / previewScale, e.getY() / previewScale));
    }

    private void instrImagePressed(MouseEvent e) {
        selectColor(instrImage.getRGB(e.getX() / previewScale, e.getY() / previewScale));
    }

    private void selectColor(int rgb) {
        switch (rgb) {
            case 0xff000000:
                normalEntry.selectBackground();
                break;
            case 0xff000008:
            case 0xff000019:
                normalEntry.selectForeground();
                break;
            case 0xff000800:
                shadedEntry.selectBackground();
                break;
            case 0xff000808:
            case 0xff000819:
                shadedEntry.selectForeground();
                break;
            case 0xff001000:
                alternativeEntry.selectBackground();
                break;
            case 0xff001008:
            case 0xff001019:
                alternativeEntry.selectForeground();
                break;
            case 0xff001900:
                selectionEntry.selectBackground();
                break;
            case 0xff001908:
            case 0xff001919:
                selectionEntry.selectForeground();
                break;
            case 0xff002100:
                scrollbarEntry.selectBackground();
                break;
            case 0xff002108:
            case 0xff002119:
                scrollbarEntry.selectForeground();
                break;
        }
    }

    private void addRandomizeButton(JPanel spinnerPanel) {
        JButton randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener((e) -> randomizeColors());
        spinnerPanel.add(randomizeButton, "grow, wrap");
    }

    private void addPaletteSelector(JPanel spinnerPanel) {
        paletteSelector = new JComboBox<>();
        paletteSelector.setEditable(true);
        paletteSelector.addActionListener(this);
        paletteSelector.addItemListener(e -> onPaletteSelected());
        paletteSelector.setMaximumSize(new Dimension(80, 1000));
        spinnerPanel.add(paletteSelector);
    }

    private void addDesaturateButton(JPanel spinnerPanel) {
        desaturateButton.addItemListener(e -> updatePreviewPanes());
        desaturateButton.setToolTipText("Tip: Enjoyable palettes look OK when desaturated, too!");
        spinnerPanel.add(desaturateButton, "grow");
    }

    private void listenToSpinners() {
        normalEntry.addListenerToAllSpinners(e -> onSpinnerChanged());
        shadedEntry.addListenerToAllSpinners(e -> onSpinnerChanged());
        alternativeEntry.addListenerToAllSpinners(e -> onSpinnerChanged());
        selectionEntry.addListenerToAllSpinners(e -> onSpinnerChanged());
        scrollbarEntry.addListenerToAllSpinners(e -> onSpinnerChanged());
    }

    private void setRomImage(byte[] romImage) {
        this.romImage = romImage;
        paletteOffset = RomUtilities.findPaletteOffset(romImage);
        if (paletteOffset == -1) {
            System.err.println("Could not find palette offset!");
        }
        nameOffset = RomUtilities.findPaletteNameOffset(romImage);
        if (nameOffset == -1) {
            System.err.println("Could not find palette name offset!");
        }
        populatePaletteSelector();
    }

    private int selectedPalette() {
        int palette = paletteSelector.getSelectedIndex();
        assert palette >= 0;
        assert palette < RomUtilities.getNumberOfPalettes(romImage);
        return palette;
    }

    // Returns color scaled to 0-0xf8.
    private java.awt.Color color(int offset) {
        // gggrrrrr 0bbbbbgg
        int r = (romImage[offset] & 0x1f) << 3;
        int g = ((romImage[offset + 1] & 3) << 6) | ((romImage[offset] & 0xe0) >> 2);
        int b = (romImage[offset + 1] << 1) & 0xf8;
        return new java.awt.Color(r, g, b);
    }

    // Shout-out to Defense Mechanism
    private void randomizeColors() {
        updatingSpinners = true;
        Random rand = new Random();
        normalEntry.randomize(rand);
        shadedEntry.randomize(rand);
        alternativeEntry.randomize(rand);
        selectionEntry.randomize(rand);
        scrollbarEntry.randomize(rand);
        updatingSpinners = false;
        onSpinnerChanged();
    }

    private int selectedPaletteOffset() {
        return paletteOffset + selectedPalette() * RomUtilities.PALETTE_SIZE;
    }

    private void updateRomFromSpinners() {
        normalEntry.writeToRom(romImage, selectedPaletteOffset());
        shadedEntry.writeToRom(romImage, selectedPaletteOffset() + 8);
        alternativeEntry.writeToRom(romImage, selectedPaletteOffset() + 16);
        selectionEntry.writeToRom(romImage, selectedPaletteOffset() + 24);
        scrollbarEntry.writeToRom(romImage, selectedPaletteOffset() + 32);
    }

    private java.awt.Color firstColor(int colorSet) {
        assert colorSet >= 0;
        assert colorSet < RomUtilities.NUM_COLOR_SETS;
        int offset = selectedPaletteOffset() + colorSet * RomUtilities.COLOR_SET_SIZE;
        return color(offset);
    }

    private java.awt.Color secondColor(int colorSet) {
        assert colorSet >= 0;
        assert colorSet < RomUtilities.NUM_COLOR_SETS;
        int offset = selectedPaletteOffset() + colorSet * RomUtilities.COLOR_SET_SIZE + 3 * 2;
        return color(offset);
    }

    private java.awt.Color midColor(int colorSet) {
        assert colorSet >= 0;
        assert colorSet < RomUtilities.NUM_COLOR_SETS;
        int offset = selectedPaletteOffset() + colorSet * RomUtilities.COLOR_SET_SIZE + 2;
        return color(offset);
    }

    private String paletteName(int palette) {
        assert palette >= 0;
        assert palette < RomUtilities.getNumberOfPalettes(romImage);
        String s = "";
        s += (char) romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE];
        s += (char) romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 1];
        s += (char) romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 2];
        s += (char) romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 3];
        return s;
    }

    private void setPaletteName(int palette, String name) {
        if (name == null) {
            return;
        }
        if (name.length() >= RomUtilities.PALETTE_NAME_SIZE) {
            name = name.substring(0, RomUtilities.PALETTE_NAME_SIZE - 1);
        } else {
            StringBuilder nameBuilder = new StringBuilder(name);
            while (nameBuilder.length() < RomUtilities.PALETTE_NAME_SIZE - 1) {
                nameBuilder.append(" ");
            }
            name = nameBuilder.toString();
        }

        romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE] = (byte) name.charAt(0);
        romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 1] = (byte) name.charAt(1);
        romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 2] = (byte) name.charAt(2);
        romImage[nameOffset + palette * RomUtilities.PALETTE_NAME_SIZE + 3] = (byte) name.charAt(3);
    }

    private void populatePaletteSelector() {
        populatingPaletteSelector = true;
        paletteSelector.removeAllItems();
        // -2 to hide the GB palettes
        for (int i = 0; i < RomUtilities.getNumberOfPalettes(romImage); ++i) {
            paletteSelector.addItem(paletteName(i));
        }
        populatingPaletteSelector = false;
    }

    private java.awt.image.BufferedImage modifyUsingPalette(java.awt.image.BufferedImage srcImage) {
        int w = srcImage.getWidth(); int h = srcImage.getHeight();
        java.awt.image.BufferedImage dstImage = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                int rgb = srcImage.getRGB(x, y);
                java.awt.Color c;
                if (rgb == 0xff000000) {
                    c = firstColor(0);
                } else if (rgb == 0xff000008) {
                    c = midColor(0);
                } else if (rgb == 0xff000019) {
                    c = secondColor(0);
                } else if (rgb == 0xff000800) {
                    c = firstColor(1);
                } else if (rgb == 0xff000808) {
                    c = midColor(1);
                } else if (rgb == 0xff000819) {
                    c = secondColor(1);
                } else if (rgb == 0xff001000) {
                    c = firstColor(2);
                } else if (rgb == 0xff001008) {
                    c = midColor(2);
                } else if (rgb == 0xff001019) {
                    c = secondColor(2);
                } else if (rgb == 0xff001900) {
                    c = firstColor(3);
                } else if (rgb == 0xff001908) {
                    c = midColor(3);
                } else if (rgb == 0xff001919) {
                    c = secondColor(3);
                } else if (rgb == 0xff002100) {
                    c = firstColor(4);
                } else if (rgb == 0xff002108) {
                    c = midColor(4);
                } else if (rgb == 0xff002119) {
                    c = secondColor(4);
                } else {
                    System.err.printf("%x%n", rgb);
                    c = new java.awt.Color(255, 0, 255);
                }
                dstImage.setRGB(x, y, ColorUtil.colorCorrect(c));
            }
        }
        if (desaturateButton.isSelected()) {
            ColorSpace colorSpace = ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY);
            return new java.awt.image.ColorConvertOp(colorSpace, null).filter(dstImage, dstImage);
        }
        return dstImage;
    }

    private void updateSongAndInstrScreens() {
        previewSongLabel.setIcon(new StretchIcon(modifyUsingPalette(songImage)));
        previewInstrLabel.setIcon(new StretchIcon(modifyUsingPalette(instrImage)));
    }

    private void updatePreviewPanes() {
        normalEntry.updatePreviews(
                new java.awt.Color(ColorUtil.colorCorrect(firstColor(0))),
                new java.awt.Color(ColorUtil.colorCorrect(secondColor(0))));
        shadedEntry.updatePreviews(
                new java.awt.Color(ColorUtil.colorCorrect(firstColor(1))),
                new java.awt.Color(ColorUtil.colorCorrect(secondColor(1))));
        alternativeEntry.updatePreviews(
                new java.awt.Color(ColorUtil.colorCorrect(firstColor(2))),
                new java.awt.Color(ColorUtil.colorCorrect(secondColor(2))));
        selectionEntry.updatePreviews(
                new java.awt.Color(ColorUtil.colorCorrect(firstColor(3))),
                new java.awt.Color(ColorUtil.colorCorrect(secondColor(3))));
        scrollbarEntry.updatePreviews(
                new java.awt.Color(ColorUtil.colorCorrect(firstColor(4))),
                new java.awt.Color(ColorUtil.colorCorrect(secondColor(4))));
        updateSongAndInstrScreens();
    }

    private void updateSpinners(int colorSetIndex, PaletteUIEntry entry) {
        Color backgroundColor = firstColor(colorSetIndex);
        Color foregroundColor = secondColor(colorSetIndex);
        entry.updateSpinnersFromColor(foregroundColor, backgroundColor);
    }

    private void updateAllSpinners() {
        updatingSpinners = true;
        updateSpinners(0, normalEntry);
        updateSpinners(1, shadedEntry);
        updateSpinners(2, alternativeEntry);
        updateSpinners(3, selectionEntry);
        updateSpinners(4, scrollbarEntry);
        updatingSpinners = false;
    }

    public void onSpinnerChanged() {
        if (!updatingSpinners) {
            updateRomFromSpinners();
            updatePreviewPanes();
        }
    }

    private void savePalette(String path) {
        Object selectedItem = paletteSelector.getSelectedItem();
        if (selectedItem == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Couldn't read the palette name.");
            return;
        }
        String paletteName = (String) selectedItem;
        assert paletteName.length() == 4;
        try {
            java.io.FileOutputStream f = new java.io.FileOutputStream(path);
            f.write(paletteName.charAt(0));
            f.write(paletteName.charAt(1));
            f.write(paletteName.charAt(2));
            f.write(paletteName.charAt(3));
            for (int i = selectedPaletteOffset(); i < selectedPaletteOffset() + RomUtilities.PALETTE_SIZE; ++i) {
                f.write(romImage[i]);
            }
            f.close();
        } catch (java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Save failed!");
        }
    }

    private void loadPalette(java.io.File file) {
        String name = "";
        try {
            java.io.RandomAccessFile f = new java.io.RandomAccessFile(file, "r");
            name += (char) f.read();
            name += (char) f.read();
            name += (char) f.read();
            name += (char) f.read();
            setPaletteName(paletteSelector.getSelectedIndex(), name);
            for (int i = selectedPaletteOffset(); i < selectedPaletteOffset() + RomUtilities.PALETTE_SIZE; ++i) {
                romImage[i] = (byte) f.read();
            }
            f.close();
        } catch (java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Load failed!");
        }
        int index = paletteSelector.getSelectedIndex();
        populatePaletteSelector();
        paletteSelector.setSelectedIndex(index);
    }

    private void showOpenDialog() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Load Palette", FileType.Lsdpal, FileOperation.Load);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            JFileChooserFactory.setBaseFolder(f.getParent());
            loadPalette(f);
        }
    }

    private void showSaveDialog() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Save Palette", FileType.Lsdpal, FileOperation.Save);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            JFileChooserFactory.setBaseFolder(f.getParent());
            String filename = f.toString();
            if (!filename.endsWith("lsdpal")) {
                filename += ".lsdpal";
            }
            savePalette(filename);
        }
    }

    private void copyPalette() {
	    try {
		    clipboard = java.io.File.createTempFile("lsdpatcher", "palette");
	    } catch (Exception e) {
		    e.printStackTrace();
	    }
	    savePalette(clipboard.getAbsolutePath());
	    menuItemPaste.setEnabled(true);
    }

    private boolean areDuplicateNames() {
	    int paletteCount = RomUtilities.getNumberOfPalettes(romImage);
	    for (int i = 0; i < paletteCount; ++i) {
		    for (int j = i + 1; j < paletteCount; ++j) {
			    if (paletteName(i).equals(paletteName(j))) {
				    return true;
			    }
		    }
	    }
	    return false;
    }

    private void addNumberToPaletteName(int paletteIndex) {
	    char[] name = paletteName(paletteIndex).toCharArray();
	    char lastChar = name[name.length - 1];
	    if (Character.isDigit(lastChar)) {
		    ++lastChar;
	    } else {
		    lastChar = '1';
	    }
	    name[name.length - 1] = lastChar;
	    setPaletteName(paletteIndex, new String(name));
    }

    private void onPaletteSelected() {
        if (paletteSelector.getSelectedIndex() != -1) {
            lastSelectedPaletteIndex = paletteSelector.getSelectedIndex();
            updatePreviewPanes();
            updateAllSpinners();
            normalEntry.selectBackground();
        }
    }

    private void onPaletteRenamed() {
        if (paletteSelector.getSelectedIndex() == -1) {
            setPaletteName(lastSelectedPaletteIndex, (String)paletteSelector.getSelectedItem());
            if (!populatingPaletteSelector) {
                populatePaletteSelector();
                paletteSelector.setSelectedIndex(lastSelectedPaletteIndex);
            }
        }
    }

	private void pastePalette() {
		int paletteIndex = paletteSelector.getSelectedIndex();
		loadPalette(clipboard);
		while (areDuplicateNames()) {
			addNumberToPaletteName(paletteIndex);
		}
		populatePaletteSelector();
		paletteSelector.setSelectedIndex(paletteIndex);
	}

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand().equals("comboBoxEdited")) {
            onPaletteRenamed();
        }
        else {
            assert false;
        }
    }
}
