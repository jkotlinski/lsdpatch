package paletteEditor;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class PaletteEditor
        extends JFrame
        implements java.awt.event.ItemListener, ChangeListener, java.awt.event.ActionListener {
    private static final long serialVersionUID = 5286120830758415869L;

    private byte[] romImage = null;
    private int paletteOffset = -1;
    private int nameOffset = -1;

    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    private final JLabel previewSongLabel = new JLabel();
    private final JLabel previewInstrLabel = new JLabel();

    private final JSpinner[] normalBackgroundSpinners = new JSpinner[3];
    private final JSpinner[] normalForegroundSpinners = new JSpinner[3];
    private final JPanel[] normalPreview = new JPanel[2];

    private final JSpinner[] shadedBackgroundSpinners = new JSpinner[3];
    private final JSpinner[] shadedForegroundSpinners = new JSpinner[3];
    private final JPanel[] shadedPreview = new JPanel[2];

    private final JSpinner[] alternativeBackgroundSpinners = new JSpinner[3];
    private final JSpinner[] alternativeForegroundSpinners = new JSpinner[3];
    private final JPanel[] alternativePreview = new JPanel[2];

    private final JSpinner[] selectionBackgroundSpinners = new JSpinner[3];
    private final JSpinner[] selectionForegroundSpinners = new JSpinner[3];
    private final JPanel[] selectionPreview = new JPanel[2];

    private final JSpinner[] scrollbarBackgroundSpinners = new JSpinner[3];
    private final JSpinner[] scrollbarForegroundSpinners = new JSpinner[3];
    private final JPanel[] scrollbarPreview = new JPanel[2];

    private final JComboBox<String> paletteSelector;

    JMenuItem mntmPaste;

    private BufferedImage songImage;
    private BufferedImage instrImage;

    private int previousSelectedPalette = -1;

    private boolean updatingSpinners = false;
    private boolean populatingPaletteSelector = false;

    private final JButton randomizeButton = new JButton("Randomize colors");
    private final JCheckBox desaturateButton = new javax.swing.JCheckBox("Desaturate preview");

    private void createSpinners(JSpinner[] spinners) {
        for (int i = 0; i < spinners.length; ++i) {
            spinners[i] = new JSpinner(new SpinnerNumberModel(0,0,31,1));
        }
    }

    private void createPreviews(JPanel[] previews) {
        for (int i = 0; i < previews.length; ++i) {
            previews[i] = new JPanel();
            previews[i].setBorder(previewLabelBorder);
        }
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(this);
        mnFile.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save...");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(this);
        mnFile.add(saveMenuItem);

        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setMnemonic(KeyEvent.VK_E);
        menuBar.add(mnEdit);

        JMenuItem mntmCopy = new JMenuItem("Copy Palette");
        mntmCopy.addActionListener(this);
        mntmCopy.setMnemonic(KeyEvent.VK_C);
        mnEdit.add(mntmCopy);

        mntmPaste = new JMenuItem("Paste Palette");
        mntmPaste.setMnemonic(KeyEvent.VK_P);
        mntmPaste.addActionListener(this);
        mntmPaste.setEnabled(false);
        mnEdit.add(mntmPaste);
    }

    private void addSpinnerEntry(JPanel panel, String name, JSpinner[] background, JSpinner[] foreground, JPanel[] previews) {
        panel.add(new JLabel(name), "span, split 3");
        for (int i = 0; i < previews.length; ++i) {
            panel.add(previews[i], i == previews.length - 1 ? "grow, wrap" : "grow");
        }
        for (int i = 0; i < background.length; ++i) {
            panel.add(background[i], i == background.length - 1 ? "grow, wrap" : "grow");
        }
        for (int i = 0; i < foreground.length; ++i) {
            panel.add(foreground[i], i == foreground.length - 1 ? "grow, wrap 10" : "grow");
        }
    }

    public PaletteEditor(Document document) {
        setupMenuBar();

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setResizable(true);
        setTitle("Palette Editor");
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout());

        paletteSelector = new JComboBox<>();
        paletteSelector.setEditable(true);
        paletteSelector.addItemListener(this);
        paletteSelector.addActionListener(this);
        contentPane.add(paletteSelector, "cell 0 0, grow, span, wrap 10");

        createSpinners(normalBackgroundSpinners);
        createSpinners(normalForegroundSpinners);
        createSpinners(shadedBackgroundSpinners);
        createSpinners(shadedForegroundSpinners);
        createSpinners(alternativeBackgroundSpinners);
        createSpinners(alternativeForegroundSpinners);
        createSpinners(selectionBackgroundSpinners);
        createSpinners(selectionForegroundSpinners);
        createSpinners(scrollbarBackgroundSpinners);
        createSpinners(scrollbarForegroundSpinners);

        createPreviews(normalPreview);
        addSpinnerEntry(contentPane, "Normal", normalBackgroundSpinners, normalForegroundSpinners, normalPreview);

        createPreviews(shadedPreview);
        addSpinnerEntry(contentPane, "Shaded", shadedBackgroundSpinners, shadedForegroundSpinners, shadedPreview);

        createPreviews(alternativePreview);
        addSpinnerEntry(contentPane, "Altenative", alternativeBackgroundSpinners, alternativeForegroundSpinners, alternativePreview);

        createPreviews(selectionPreview);
        addSpinnerEntry(contentPane, "Selection", selectionBackgroundSpinners, selectionForegroundSpinners, selectionPreview);

        createPreviews(scrollbarPreview);
        addSpinnerEntry(contentPane, "Scrollbar", scrollbarBackgroundSpinners, scrollbarForegroundSpinners, scrollbarPreview);

        randomizeButton.addActionListener((e) -> randomizeColors());

        contentPane.add(randomizeButton, "span, grow, wrap");

        desaturateButton.addItemListener(this);
        contentPane.add(desaturateButton, "span, grow, wrap");

        JPanel previewSection = new JPanel();
        previewSection.setLayout(new GridLayout(2, 1));
        previewSongLabel.setMinimumSize(new Dimension(160 * 2, 144 * 2));
        previewInstrLabel.setMinimumSize(new Dimension(160 * 2, 144 * 2));
        previewSection.add(previewSongLabel);
        previewSection.add(previewInstrLabel);
        contentPane.add(previewSection, "dock east, gap 10");

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
    }

    private void addSelfAsListenerToAllSpinners(JSpinner[] spinners) {
        for(JSpinner spinner : spinners) {
            spinner.addChangeListener(this);
        }
    }
    private void listenToSpinners() {
        addSelfAsListenerToAllSpinners(normalBackgroundSpinners);
        addSelfAsListenerToAllSpinners(normalForegroundSpinners);
        addSelfAsListenerToAllSpinners(shadedBackgroundSpinners);
        addSelfAsListenerToAllSpinners(shadedForegroundSpinners);
        addSelfAsListenerToAllSpinners(alternativeBackgroundSpinners);
        addSelfAsListenerToAllSpinners(alternativeForegroundSpinners);
        addSelfAsListenerToAllSpinners(selectionBackgroundSpinners);
        addSelfAsListenerToAllSpinners(selectionForegroundSpinners);
        addSelfAsListenerToAllSpinners(scrollbarBackgroundSpinners);
        addSelfAsListenerToAllSpinners(scrollbarForegroundSpinners);
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

    private void randomizeSpinnerGroup(Random rand, JSpinner[] backgroundSpinners, JSpinner[] foregroundSpinners) {
        for(JSpinner spinner : backgroundSpinners) {
            spinner.setValue(rand.nextInt(32));
        }
        for(JSpinner spinner : foregroundSpinners) {
            spinner.setValue(rand.nextInt(32));
        }

    }

    // Shoutout to Defense Mechanism
    private void randomizeColors() {
        updatingSpinners = true;
        Random rand = new Random();
        randomizeSpinnerGroup(rand, normalBackgroundSpinners, normalForegroundSpinners);
        randomizeSpinnerGroup(rand, shadedBackgroundSpinners, shadedForegroundSpinners);
        randomizeSpinnerGroup(rand, alternativeBackgroundSpinners, alternativeForegroundSpinners);
        randomizeSpinnerGroup(rand, selectionBackgroundSpinners, selectionForegroundSpinners);
        randomizeSpinnerGroup(rand, scrollbarBackgroundSpinners, scrollbarForegroundSpinners);
        updatingSpinners = false;
        stateChanged(null);
    }

    private void updateRom(int offset, JSpinner[] backgroundSpinners, JSpinner[] foregroundSpinners) {
        int r1 = (Integer) backgroundSpinners[0].getValue();
        int g1 = (Integer) backgroundSpinners[1].getValue();
        int b1 = (Integer) backgroundSpinners[2].getValue();
        // gggrrrrr 0bbbbbgg
        romImage[offset] = (byte) (r1 | (g1 << 5));
        romImage[offset + 1] = (byte) ((g1 >> 3) | (b1 << 2));

        int r2 = (Integer) foregroundSpinners[0].getValue();
        int g2 = (Integer) foregroundSpinners[1].getValue();
        int b2 = (Integer) foregroundSpinners[2].getValue();
        romImage[offset + 6] = (byte) (r2 | (g2 << 5));
        romImage[offset + 7] = (byte) ((g2 >> 3) | (b2 << 2));

        // Generating antialiasing colors.
        int rMid = (r1 + r2) / 2;
        int gMid = (g1 + g2) / 2;
        int bMid = (b1 + b2) / 2;
        romImage[offset + 2] = (byte) (rMid | (gMid << 5));
        romImage[offset + 3] = (byte) ((gMid >> 3) | (bMid << 2));
        romImage[offset + 4] = romImage[offset + 2];
        romImage[offset + 5] = romImage[offset + 3];
    }

    private int selectedPaletteOffset() {
        return paletteOffset + selectedPalette() * RomUtilities.PALETTE_SIZE;
    }

    private void updateRomFromSpinners() {
        updateRom(selectedPaletteOffset(), normalBackgroundSpinners, normalForegroundSpinners);
        updateRom(selectedPaletteOffset() + 8, shadedBackgroundSpinners, shadedForegroundSpinners);
        updateRom(selectedPaletteOffset() + 16, alternativeBackgroundSpinners, alternativeForegroundSpinners);
        updateRom(selectedPaletteOffset() + 24, selectionBackgroundSpinners, selectionForegroundSpinners);
        updateRom(selectedPaletteOffset() + 32, scrollbarBackgroundSpinners, scrollbarForegroundSpinners);
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

    private int colorCorrect(java.awt.Color c) {
        int r = ((c.getRed() >> 3) * 255) / 0xf8;
        int g = ((c.getGreen() >> 3) * 255) / 0xf8;
        int b = ((c.getBlue() >> 3) * 255) / 0xf8;

        // Matrix conversion from Gambatte.
        return (((r * 13 + g * 2 + b) >> 1) << 16)
                | ((g * 3 + b) << 9)
                | ((r * 3 + g * 2 + b * 11) >> 1);
    }

    private java.awt.image.BufferedImage modifyUsingPalette(java.awt.image.BufferedImage srcImage) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();
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
                dstImage.setRGB(x, y, colorCorrect(c));
            }
        }
	if (desaturateButton.isSelected()) {
		return new java.awt.image.ColorConvertOp(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY), null).filter(dstImage, dstImage);
	}
        return dstImage;
    }

    private void updateSongAndInstrScreens() {
        previewSongLabel.setIcon(new StretchIcon(modifyUsingPalette(songImage)));
        previewInstrLabel.setIcon(new StretchIcon(modifyUsingPalette(instrImage)));
    }

    private void setPreviewBackground(int colorSetIndex, JPanel[] previews) {
        previews[0].setBackground(new java.awt.Color(colorCorrect(firstColor(colorSetIndex))));
        previews[1].setBackground(new java.awt.Color(colorCorrect(secondColor(colorSetIndex))));

    }

    private void updatePreviewPanes() {
        setPreviewBackground(0, normalPreview);
        setPreviewBackground(1, shadedPreview);
        setPreviewBackground(2, alternativePreview);
        setPreviewBackground(3, selectionPreview);
        setPreviewBackground(4, scrollbarPreview);

        updateSongAndInstrScreens();
    }

    private void updateSpinners(int colorSetIndex, JSpinner[] backgroundSpinners, JSpinner[] foregroundSpinners) {
        updatingSpinners = true;
        Color firstColor = firstColor(colorSetIndex);
        Color secondColor = secondColor(colorSetIndex);

        backgroundSpinners[0].setValue(firstColor.getRed()>>3);
        backgroundSpinners[1].setValue(firstColor.getGreen()>>3);
        backgroundSpinners[2].setValue(firstColor.getBlue()>>3);

        foregroundSpinners[0].setValue(secondColor.getRed()>>3);
        foregroundSpinners[1].setValue(secondColor.getGreen()>>3);
        foregroundSpinners[2].setValue(secondColor.getBlue()>>3);
        updatingSpinners = false;
    }

    private void updateAllSpinners() {
        updateSpinners(0, normalBackgroundSpinners, normalForegroundSpinners);
        updateSpinners(1, shadedBackgroundSpinners, shadedForegroundSpinners);
        updateSpinners(2, alternativeBackgroundSpinners, alternativeForegroundSpinners);
        updateSpinners(3, selectionBackgroundSpinners, selectionForegroundSpinners);
        updateSpinners(4, scrollbarBackgroundSpinners, scrollbarForegroundSpinners);
    }

    public void itemStateChanged(java.awt.event.ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == paletteSelector && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            // Palette changed.
            if (paletteSelector.getSelectedIndex() != -1) {
                updatePreviewPanes();
                updateAllSpinners();
            }
        } else if (source == desaturateButton) {
            updatePreviewPanes();
        }
    }

    public void stateChanged(ChangeEvent e) {
        // Spinner changed.
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

    java.io.File clipboard;

    private void copyPalette() {
	    try {
		    clipboard = java.io.File.createTempFile("lsdpatcher", "palette");
	    } catch (Exception e) {
		    e.printStackTrace();
	    }
	    savePalette(clipboard.getAbsolutePath());
	    mntmPaste.setEnabled(true);
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
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "Open...":
                showOpenDialog();
                break;
            case "Save...":
                showSaveDialog();
                break;
            case "comboBoxChanged": {
                if (paletteSelector.getSelectedIndex() != -1) {
                    previousSelectedPalette = paletteSelector.getSelectedIndex();
                }
                break;
            }
            case "comboBoxEdited": {
                if (paletteSelector.getSelectedIndex() == -1) {
                    setPaletteName(previousSelectedPalette, (String)paletteSelector.getSelectedItem());
                    if (!populatingPaletteSelector) {
                        populatePaletteSelector();
                        paletteSelector.setSelectedIndex(previousSelectedPalette);
                    }
                } else {
                    previousSelectedPalette = paletteSelector.getSelectedIndex();
                }
                break;
            }
	    case "Copy Palette":
		copyPalette();
		break;
	    case "Paste Palette":
		pastePalette();
		break;
	    default:
		assert false;
        }
    }
}
