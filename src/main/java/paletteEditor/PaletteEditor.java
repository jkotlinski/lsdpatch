package paletteEditor;

import java.awt.*;

import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import Document.Document;
import net.miginfocom.swing.MigLayout;
import utils.EditorPreferences;
import utils.FileDialogLauncher;
import utils.RomUtilities;
import utils.StretchIcon;

import javax.swing.*;

public class PaletteEditor extends JFrame implements SwatchPair.Listener {
    private byte[] romImage = null;
    private int paletteOffset = -1;
    private int nameOffset = -1;
    private final int previewScale = 2;
    java.io.File clipboard;

    private final JLabel previewSongLabel = new JLabel();
    private final JLabel previewInstrLabel = new JLabel();

    private final ColorPicker colorPicker = new ColorPicker();

    private final SwatchPanel swatchPanel = new SwatchPanel();

    private final JComboBox<String> paletteSelector;

    JMenuItem menuItemPaste;

    private BufferedImage songImage;
    private BufferedImage instrImage;

    private int lastSelectedPaletteIndex = -1;

    private boolean updatingSwatches = false;
    private boolean populatingPaletteSelector = false;

    private final JToggleButton desaturateToggleButton = new javax.swing.JCheckBox("Desaturate");

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
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout());

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new MigLayout());

        JPanel topRowPanel = new JPanel();
        topRowPanel.setLayout(new MigLayout());

        paletteSelector = new JComboBox<>();
        paletteSelector.setEditable(true);
        paletteSelector.addActionListener(e -> onPaletteRenamed());
        paletteSelector.addItemListener(e -> onPaletteSelected());
        topRowPanel.add(paletteSelector);

        boolean rawScreen = EditorPreferences.getKey("raw", "0").equals("1");
        JToggleButton rawToggleButton = new JCheckBox("Raw");
        rawToggleButton.setSelected(rawScreen);
        ColorUtil.setRawScreen(rawScreen);
        rawToggleButton.addItemListener(e -> {
            boolean enabled = ColorUtil.toggleRawScreen();
            ColorUtil.setRawScreen(enabled);
            EditorPreferences.putKey("raw", enabled ? "1" : "0");
            updateSongAndInstrScreens();
            colorPicker.repaint();
            updateAllSwatches();
        });
        rawToggleButton.setToolTipText("Display palette with no color conversion."
                + " Poor Game Boy emulators can look like this.");
        topRowPanel.add(rawToggleButton);

        desaturateToggleButton.addItemListener(e -> updateSongAndInstrScreens());
        desaturateToggleButton.setToolTipText(
                "Making the palette work in grayscale is needed for the color-blind and good for everyone else.");
        topRowPanel.add(desaturateToggleButton);

        midPanel.add(topRowPanel, "grow, wrap");
        midPanel.add(colorPicker);

        swatchPanel.addListener(this);

        previewSongLabel.setPreferredSize(new Dimension(160 * previewScale, 144 * previewScale));
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
        contentPane.add(swatchPanel);

        previewInstrLabel.setPreferredSize(new Dimension(160 * previewScale, 144 * previewScale));
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

        try {
            songImage = javax.imageio.ImageIO.read(getClass().getResource("/song.bmp"));
            instrImage = javax.imageio.ImageIO.read(getClass().getResource("/instr.bmp"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        setRomImage(document.romImage());
        updateSongAndInstrScreens();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                document.setRomImage(romImage);
            }
        });

        pack();

        // Needs to be here for the swatchSelected callback.
        swatchPanel.normalSwatchPair.selectBackground();
    }

    private void songImagePressed(MouseEvent e) {
        selectColor(songImage.getRGB(e.getX() / previewScale, e.getY() / previewScale));
    }

    private void instrImagePressed(MouseEvent e) {
        selectColor(instrImage.getRGB(e.getX() / previewScale, e.getY() / previewScale));
    }

    private void selectColor(int rgb) {
        switch (rgb) {
            case ScreenShotColors.NORMAL_BG:
                swatchPanel.normalSwatchPair.selectBackground();
                break;
            case ScreenShotColors.NORMAL_MID:
            case ScreenShotColors.NORMAL_FG:
                swatchPanel.normalSwatchPair.selectForeground();
                break;
            case ScreenShotColors.SHADED_BG:
                swatchPanel.shadedSwatchPair.selectBackground();
                break;
            case ScreenShotColors.SHADED_MID:
            case ScreenShotColors.SHADED_FG:
                swatchPanel.shadedSwatchPair.selectForeground();
                break;
            case ScreenShotColors.ALT_BG:
                swatchPanel.alternateSwatchPair.selectBackground();
                break;
            case ScreenShotColors.ALT_MID:
            case ScreenShotColors.ALT_FG:
                swatchPanel.alternateSwatchPair.selectForeground();
                break;
            case ScreenShotColors.CUR_BG:
                swatchPanel.cursorSwatchPair.selectBackground();
                break;
            case ScreenShotColors.CUR_MID:
            case ScreenShotColors.CUR_FG:
                swatchPanel.cursorSwatchPair.selectForeground();
                break;
            case ScreenShotColors.SCROLL_BG:
                swatchPanel.scrollBarSwatchPair.selectBackground();
                break;
            case ScreenShotColors.SCROLL_MID:
            case ScreenShotColors.SCROLL_FG:
                swatchPanel.scrollBarSwatchPair.selectForeground();
                break;
        }
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

    // Returns color scaled to 0-0xff.
    private java.awt.Color color(int offset) {
        // gggrrrrr 0bbbbbgg
        int r = (romImage[offset] & 0x1f) << 3;
        int g = ((romImage[offset + 1] & 3) << 6) | ((romImage[offset] & 0xe0) >> 2);
        int b = (romImage[offset + 1] << 1) & 0xf8;
        r *= 0xff;
        g *= 0xff;
        b *= 0xff;
        r /= 0xf8;
        g /= 0xf8;
        b /= 0xf8;
        return new java.awt.Color(r, g, b);
    }

    private int selectedPaletteOffset() {
        return paletteOffset + selectedPalette() * RomUtilities.PALETTE_SIZE;
    }

    private void updateRomFromSwatches() {
        swatchPanel.writeToRom(romImage, selectedPaletteOffset());
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
        name = name.toUpperCase();
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

        int normalBg = ColorUtil.colorCorrect(firstColor(0));
        int normalMid = ColorUtil.colorCorrect(midColor(0));
        int normalFg = ColorUtil.colorCorrect(secondColor(0));
        int shadedBg = ColorUtil.colorCorrect(firstColor(1));
        int shadedMid = ColorUtil.colorCorrect(midColor(1));
        int shadedFg = ColorUtil.colorCorrect(secondColor(1));
        int alternateBg = ColorUtil.colorCorrect(firstColor(2));
        int alternateMid = ColorUtil.colorCorrect(midColor(2));
        int alternateFg = ColorUtil.colorCorrect(secondColor(2));
        int cursorBg = ColorUtil.colorCorrect(firstColor(3));
        int cursorMid = ColorUtil.colorCorrect(midColor(3));
        int cursorFg = ColorUtil.colorCorrect(secondColor(3));
        int scrollBg = ColorUtil.colorCorrect(firstColor(4));
        int scrollMid = ColorUtil.colorCorrect(midColor(4));
        int scrollFg = ColorUtil.colorCorrect(secondColor(4));

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                int rgb = srcImage.getRGB(x, y);
                int correctedRgb;
                switch (rgb) {
                    case ScreenShotColors.NORMAL_BG:
                        correctedRgb = normalBg;
                        break;
                    case ScreenShotColors.NORMAL_MID:
                        correctedRgb = normalMid;
                        break;
                    case ScreenShotColors.NORMAL_FG:
                        correctedRgb = normalFg;
                        break;
                    case ScreenShotColors.SHADED_BG:
                        correctedRgb = shadedBg;
                        break;
                    case ScreenShotColors.SHADED_MID:
                        correctedRgb = shadedMid;
                        break;
                    case ScreenShotColors.SHADED_FG:
                        correctedRgb = shadedFg;
                        break;
                    case ScreenShotColors.ALT_BG:
                        correctedRgb = alternateBg;
                        break;
                    case ScreenShotColors.ALT_MID:
                        correctedRgb = alternateMid;
                        break;
                    case ScreenShotColors.ALT_FG:
                        correctedRgb = alternateFg;
                        break;
                    case ScreenShotColors.CUR_BG:
                        correctedRgb = cursorBg;
                        break;
                    case ScreenShotColors.CUR_MID:
                        correctedRgb = cursorMid;
                        break;
                    case ScreenShotColors.CUR_FG:
                        correctedRgb = cursorFg;
                        break;
                    case ScreenShotColors.SCROLL_BG:
                        correctedRgb = scrollBg;
                        break;
                    case ScreenShotColors.SCROLL_MID:
                        correctedRgb = scrollMid;
                        break;
                    case ScreenShotColors.SCROLL_FG:
                        correctedRgb = scrollFg;
                        break;
                    default:
                        System.err.printf("%x%n", rgb);
                        correctedRgb = 0xff00ff;
                }
                dstImage.setRGB(x, y, correctedRgb);
            }
        }
        if (desaturateToggleButton.isSelected()) {
            ColorSpace colorSpace = ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY);
            return new java.awt.image.ColorConvertOp(colorSpace, null).filter(dstImage, dstImage);
        }
        return dstImage;
    }

    private void updateSongAndInstrScreens() {
        previewSongLabel.setIcon(new StretchIcon(modifyUsingPalette(songImage)));
        previewInstrLabel.setIcon(new StretchIcon(modifyUsingPalette(instrImage)));
    }

    private void updateSwatches(int colorSetIndex, SwatchPair swatchPair) {
        Color backgroundColor = firstColor(colorSetIndex);
        Color foregroundColor = secondColor(colorSetIndex);
        swatchPair.setColors(foregroundColor, backgroundColor);
    }

    private void updateAllSwatches() {
        updatingSwatches = true;
        updateSwatches(0, swatchPanel.normalSwatchPair);
        updateSwatches(1, swatchPanel.shadedSwatchPair);
        updateSwatches(2, swatchPanel.alternateSwatchPair);
        updateSwatches(3, swatchPanel.cursorSwatchPair);
        updateSwatches(4, swatchPanel.scrollBarSwatchPair);
        updatingSwatches = false;
        swatchChanged();
    }

    private Swatch selectedSwatch;

    @Override
    public void swatchSelected(Swatch swatch) {
        selectedSwatch = swatch;
        colorPicker.setColor(swatch.r(), swatch.g(), swatch.b());
        colorPicker.subscribe((r, g, b) -> {
                    updatingSwatches = true;
                    swatch.setRGB(r, g, b);
                    updateRomFromSwatches();
                    updateSongAndInstrScreens();
                    updatingSwatches = false;
                });
    }

    @Override
    public void swatchChanged() {
        if (!updatingSwatches) {
            updateRomFromSwatches();
            updateSongAndInstrScreens();
            if (selectedSwatch != null) {
                colorPicker.setColor(selectedSwatch.r(), selectedSwatch.g(), selectedSwatch.b());
            }
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
        while (areDuplicateNames()) {
            addNumberToPaletteName(index);
        }
        populatePaletteSelector();
        paletteSelector.setSelectedIndex(index);
    }

    private void showOpenDialog() {
        File f = FileDialogLauncher.load(this, "Load Palette", "lsdpal");
        if (f != null) {
            loadPalette(f);
        }
    }

    private void showSaveDialog() {
        File f = FileDialogLauncher.save(this, "Save Palette", "lsdpal");
        if (f != null) {
            savePalette(f.toString());
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
            updateSongAndInstrScreens();
            updateAllSwatches();
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
}
