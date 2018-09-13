/* Copyright (C) 2001-2011 by Johan Kotlinski

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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import fontEditor.FontEditor;
import structures.LSDJFont;
import utils.JFileChooserFactory;
import utils.JFileChooserFactory.FileOperation;
import utils.JFileChooserFactory.FileType;
import utils.RomUtilities;
import utils.SampleCanvas;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = -3993608561466542956L;
    private JPanel contentPane;
    private final JPanel jPanel1 = new JPanel();
    //JLabel fileNameLabel = new JLabel();
    private int prevBankBoxIndex = -1;
    private final JComboBox<String> bankBox = new JComboBox<>();
    private final JList<String> instrList = new JList<>();
    private final PaletteEditor paletteEditor = new PaletteEditor();
    private final FontEditor fontEditor = new FontEditor();

    private static final int MAX_SAMPLES = 15;

    private final java.awt.event.ActionListener bankBoxListener =
            e -> bankBox_actionPerformed();

    private RandomAccessFile romFile;
    private int totSampleSize = 0;

    private byte[] romImage = null;

    private Sample[] samples = new Sample[MAX_SAMPLES];

    private JMenuItem saveROMItem;
    private JMenuItem importKitsItem;
    private JMenuItem importFontsItem;
    private JMenuItem importPalettesItem;
    private JMenuItem importAllItem;
    private final JButton loadKitButton = new JButton();
    private final JButton exportKitButton = new JButton();
    private final JButton exportSampleButton = new JButton();
    private final JButton renameKitButton = new JButton();
    private final JTextArea kitTextArea = new JTextArea();
    private final JButton addSampleButton = new JButton();
    private final JButton dropSampleButton = new JButton();
    private final JButton saveROMButton = new JButton();
    private final JLabel kitSizeLabel = new JLabel();
    private final JPanel jPanel2 = new JPanel();
    private final SampleCanvas sampleView = new SampleCanvas();
    private final JSlider ditherSlider = new JSlider();
    private final GridLayout gridLayout1 = new GridLayout();

    private final JMenuBar menuBar = new JMenuBar();

    class KitFileFilter implements java.io.FilenameFilter {
        public boolean accept(java.io.File dir, String name) {
            return name.toLowerCase().endsWith(".kit");
        }
    }

    class WavFileFilter implements java.io.FilenameFilter {
        public boolean accept(java.io.File dir, String name) {
            return name.toLowerCase().endsWith(".wav");
        }
    }

    private void emptyInstrList() {
        String listData[] = {"1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "11.",
                "12.", "13.", "14.", "15."};
        instrList.setListData(listData);
    }

    MainWindow() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        emptyInstrList();

        selectRomToLoad();
    }

    private void buildMenus() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem("Open ROM...", KeyEvent.VK_O);
        menuItem.addActionListener(e -> selectRomToLoad());
        fileMenu.add(menuItem);

        fileMenu.addSeparator();

        importKitsItem = new JMenuItem("Import kits from ROM...", KeyEvent.VK_I);
        importKitsItem.setEnabled(false);
        importKitsItem.addActionListener(e3 -> importKits_actionPerformed());

        fileMenu.add(importKitsItem);

        importFontsItem = new JMenuItem("Import fonts from ROM...", KeyEvent.VK_I);
        importFontsItem.setEnabled(false);
        importFontsItem.addActionListener(e2 -> importFonts_actionPerformed());
        fileMenu.add(importFontsItem);

        importPalettesItem = new JMenuItem("Import palettes from ROM...", KeyEvent.VK_I);
        importPalettesItem.setEnabled(false);
        importPalettesItem.addActionListener(e1 -> importPalettes_actionPerformed());
        fileMenu.add(importPalettesItem);

        importAllItem = new JMenuItem("Import all from ROM...", KeyEvent.VK_A);
        importAllItem.setEnabled(false);
        importAllItem.addActionListener(e1 -> importAll_actionPerformed());
        fileMenu.add(importAllItem);

        fileMenu.addSeparator();

        saveROMItem = new JMenuItem("Save ROM...", KeyEvent.VK_S);
        saveROMItem.setEnabled(false);
        saveROMItem.addActionListener(e -> saveROMButton_actionPerformed());
        fileMenu.add(saveROMItem);

        // -----

        JMenu paletteMenu = new JMenu("Palette");
        paletteMenu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(paletteMenu);
        JMenuItem editPaletteItem = new JMenuItem("Edit Palettes...", KeyEvent.VK_P);
        editPaletteItem.addActionListener(e -> paletteEditor.setVisible(true));
        paletteMenu.add(editPaletteItem);

        JMenu fontMenu = new JMenu("Font");
        fontMenu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(fontMenu);
        JMenuItem editFontItem = new JMenuItem("Edit Fonts...", KeyEvent.VK_F);
        editFontItem.addActionListener(e -> fontEditor.setVisible(true));
        fontMenu.add(editFontItem);

        //help menu
        /*
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menuItem = new JMenuItem("Documentation", KeyEvent.VK_D);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                openDoc_actionPerformed(e);
                }
                });
        menu.add(menuItem);
        */

        setJMenuBar(menuBar);
    }

    class DitherListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            compileKit();
        }
    }

    /**
     * Component initialization
     */
    private void jbInit() {
        //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Your Icon]")));
        contentPane = (JPanel) this.getContentPane();
        TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(164, 164, 159)), "ROM Image");
        // TitledBorder titledBorder2 = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), "rename kit");
        TitledBorder titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(164, 164, 159)), "Dithering (0-16)");
        contentPane.setLayout(null);
        this.setSize(new Dimension(400, 464));
        this.setResizable(false);
        this.setTitle("LSDPatcher");

        new FileDrop(contentPane, files -> {
            for (File file : files) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".wav")) {
                    if (romImage == null) {
                        JOptionPane.showMessageDialog(contentPane,
                                "Open .gb file before adding samples.",
                                "Can't add sample!",
                                JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    addSample(file);
                } else if (fileName.endsWith(".gb")) {
                    loadRom(file);
                } else if (fileName.endsWith(".kit")) {
                    if (romImage == null) {
                        JOptionPane.showMessageDialog(contentPane,
                                "Open .gb file before adding samples.",
                                "Can't add sample!",
                                JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    loadKit(file);
                } else {
                    JOptionPane.showMessageDialog(contentPane,
                            "Unknown file type!",
                            "File error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        });

        jPanel1.setBorder(titledBorder1);
        jPanel1.setBounds(new Rectangle(8, 6, 193, 375 - 17));
        jPanel1.setLayout(null);
        //fileNameLabel.setText("No file opened!");
        //fileNameLabel.setBounds(new Rectangle(9, 18, 174, 17));
        bankBox.setBounds(new Rectangle(8, 18, 176, 25));
        bankBox.addActionListener(bankBoxListener);
        instrList.setBorder(BorderFactory.createEtchedBorder());
        instrList.setBounds(new Rectangle(8, 64 - 17, 176, 280));
        instrList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (romImage != null) {
                    int index = instrList.locationToIndex(e.getPoint());

                    boolean hasIndex = (index > -1);
                    if (hasIndex) {
                        playSample(index);
                    }
                    dropSampleButton.setEnabled(hasIndex);
                    exportSampleButton.setEnabled(hasIndex);
                }
            }
        });

        contentPane.setMinimumSize(new Dimension(1, 1));
        contentPane.setPreferredSize(new Dimension(400, 414));
        loadKitButton.setEnabled(false);
        loadKitButton.setText("load kit");
        loadKitButton.setBounds(new Rectangle(212, 78 - 72, 170, 28));
        loadKitButton.addActionListener(e -> loadKitButton_actionPerformed());
        exportKitButton.addActionListener(e -> exportKitButton_actionPerformed());
        exportKitButton.setBounds(new Rectangle(212, 110 - 72, 170, 28));
        exportKitButton.setEnabled(false);
        exportKitButton.setText("export kit");

        exportSampleButton.setEnabled(false);
        exportSampleButton.setText("export sample");
        exportSampleButton.setBounds(new Rectangle(212, 184 - 82, 170, 28));
        exportSampleButton.addActionListener(e -> exportSample());

        renameKitButton.addActionListener(e1 -> renameKitButton_actionPerformed());
        renameKitButton.setEnabled(false);
        renameKitButton.setText("rename kit");
        renameKitButton.setBounds(new Rectangle(287, 143 - 72, 95, 27));
        kitTextArea.setBorder(BorderFactory.createEtchedBorder());
        kitTextArea.setBounds(new Rectangle(212, 146 - 72, 67, 21));
        addSampleButton.addActionListener(e -> selectSampleToAdd());
        addSampleButton.setBounds(new Rectangle(212, 216 - 72, 170, 28));
        addSampleButton.setEnabled(false);
        addSampleButton.setText("add sample");
        dropSampleButton.setText("drop sample");
        dropSampleButton.setEnabled(false);
        dropSampleButton.setBounds(new Rectangle(212, 248 - 72, 170, 28));
        dropSampleButton.addActionListener(e -> dropSample());
        saveROMButton.addActionListener(e -> saveROMButton_actionPerformed());
        saveROMButton.setBounds(new Rectangle(212, 280 - 64, 170, 28));
        saveROMButton.setEnabled(false);
        saveROMButton.setText("save rom");
        kitSizeLabel.setToolTipText("");
        kitSizeLabel.setText("0/3fa0 bytes used");
        kitSizeLabel.setBounds(new Rectangle(8, 346 - 17, 169, 22));
        jPanel2.setBorder(titledBorder3);
        jPanel2.setBounds(new Rectangle(210, 315 - 64, 174, 66));
        jPanel2.setLayout(gridLayout1);
        ditherSlider.setMajorTickSpacing(4);
        ditherSlider.setMaximum(16);
        ditherSlider.setMinorTickSpacing(1);
        ditherSlider.setPaintLabels(true);
        ditherSlider.setPaintTicks(true);
        ditherSlider.setToolTipText("");
        ditherSlider.setValue(0);
        ditherSlider.setEnabled(false);
        ditherSlider.addChangeListener(new DitherListener());
        contentPane.add(jPanel1, null);
        jPanel1.add(bankBox, null);
        jPanel1.add(instrList, null);
        jPanel1.add(kitSizeLabel, null);
        contentPane.add(loadKitButton, null);
        contentPane.add(exportKitButton, null);
        contentPane.add(renameKitButton, null);
        contentPane.add(kitTextArea, null);
        contentPane.add(exportSampleButton, null);
        contentPane.add(addSampleButton, null);
        contentPane.add(dropSampleButton, null);
        contentPane.add(saveROMButton, null);
//        contentPane.add(jPanel2, null);
        String versionString = "LSDPatcher Redux WIP " + LSDPatcher.VERSION;
        JLabel versionLabel = new JLabel(versionString, SwingConstants.RIGHT);
        versionLabel.setBounds(new Rectangle(210, 335, 174, 26));
        contentPane.add(versionLabel);
        jPanel2.add(ditherSlider, null);

        sampleView.setBounds(new Rectangle(10, 370, 380, 40));
        contentPane.add(sampleView);

        buildMenus();
    }

    /**
     * Overridden so we can exit when window is closed
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }

    private byte[] get4BitSamples(int index) {
        int offset = getSelectedROMBank() * RomUtilities.BANK_SIZE + index * 2;
        int start = (0xff & romImage[offset]) | ((0xff & romImage[offset + 1]) << 8);
        int stop = (0xff & romImage[offset + 2]) | ((0xff & romImage[offset + 3]) << 8);
        if (stop <= start) {
            return null;
        }
        byte[] arr = new byte[stop - start];
        for (int i = start; i < stop; ++i) {
            arr[i - start] = romImage[getSelectedROMBank() * RomUtilities.BANK_SIZE - RomUtilities.BANK_SIZE + i];
        }
        return arr;
    }

    private void playSample(int index) {
        byte[] nibbles = get4BitSamples(index);
        if (nibbles == null) {
            return;
        }
        try {
            sampleView.setBufferContent(Sound.play(nibbles));
            sampleView.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Audio error",
                    JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadRom(File gbFile) {
        try {
            romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            setTitle(gbFile.getAbsoluteFile().toString());
            romFile = new RandomAccessFile(gbFile, "r");
            romFile.readFully(romImage);
            romFile.close();
            fontEditor.setRomImage(romImage);
            paletteEditor.setRomImage(romImage);
            saveROMItem.setEnabled(true);
            saveROMButton.setEnabled(true);
            importKitsItem.setEnabled(true);
            importFontsItem.setEnabled(true);
            importPalettesItem.setEnabled(true);
            importAllItem.setEnabled(true);
            loadKitButton.setEnabled(true);
            exportKitButton.setEnabled(true);
            renameKitButton.setEnabled(true);
            flushWavFiles();
            updateRomView();
            bankBox.setSelectedIndex(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectRomToLoad() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Load ROM Image", FileType.Gb, FileOperation.Load);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                loadRom(f);
                JFileChooserFactory.recordNewBaseFolder(f.getParent());
            }
        }
    }

    private boolean isKitBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == 0x60 && l_char_2 == 0x40);
    }

    private boolean isEmptyKitBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == -1 && l_char_2 == -1);
    }

    private String getKitName(int a_bank) {
        if (isEmptyKitBank(a_bank)) {
            return "Empty";
        }

        byte buf[] = new byte[6];
        int offset = (a_bank) * RomUtilities.BANK_SIZE + 0x52;
        for (int i = 0; i < 6; i++) {
            buf[i] = romImage[offset++];
        }
        return new String(buf);
    }

    private void updateRomView() {
        int tmp = bankBox.getSelectedIndex();
        bankBox.removeActionListener(bankBoxListener);
        bankBox.removeAllItems();

        int l_ui_index = 0;
        for (int bankNo = 0; bankNo < RomUtilities.BANK_COUNT; bankNo++) {
            if (isKitBank(bankNo) || isEmptyKitBank(bankNo)) {
                bankBox.addItem(Integer.toHexString(++l_ui_index).toUpperCase() + ". " + getKitName(bankNo));
            }
        }
        bankBox.setSelectedIndex(tmp == -1 ? 0 : tmp);
        bankBox.addActionListener(bankBoxListener);
        updateBankView();
    }

    private int m_selected = -1;

    private int getSelectedUiBank() {
        if (bankBox.getSelectedIndex() > -1) {
            m_selected = bankBox.getSelectedIndex();
        }
        return m_selected;
    }

    private int getSelectedROMBank() {
        int l_rom_bank = 0;
        int l_ui_bank = 0;

        for (; ; ) {
            if (isKitBank(l_rom_bank) || isEmptyKitBank(l_rom_bank)) {
                if (getSelectedUiBank() == l_ui_bank) {
                    return l_rom_bank;
                }
                l_ui_bank++;
            }
            l_rom_bank++;
        }

    }

    private int getROMOffsetForSelectedBank() {
        return getSelectedROMBank() * RomUtilities.BANK_SIZE;
    }

    private void updateBankView() {
        if (isEmptyKitBank(getSelectedROMBank())) {
            emptyInstrList();
            return;
        }

        byte buf[] = new byte[3];
        String s[] = new String[15];

        totSampleSize = 0;

        int bankOffset = getROMOffsetForSelectedBank();
        instrList.removeAll();
        //do banks

        //update names
        int offset = bankOffset + 0x22;
        for (int instrNo = 0; instrNo < MAX_SAMPLES; instrNo++) {
            boolean isNull = false;
            for (int i = 0; i < 3; i++) {
                buf[i] = romImage[offset++];
                if (isNull) {
                    buf[i] = '-';
                } else {
                    if (buf[i] == 0) {
                        buf[i] = '-';
                        isNull = true;
                    }
                }
            }
            s[instrNo] = (instrNo + 1) + ". " + new String(buf);
            Sample sample = samples[instrNo];
            if (sample != null) {
                int sampleLength = (sample.length() / 2 - sample.length() / 2 % 0x10);
                totSampleSize += sampleLength;
                s[instrNo] += " (" + Integer.toHexString(sampleLength) + ")";
            }
        }
        instrList.setListData(s);

        updateKitSizeLabel();
        addSampleButton.setEnabled(firstFreeSampleSlot() != -1);
        ditherSlider.setEnabled(false);  // TODO: Should be individual per sample.
    }

    private void updateKitSizeLabel() {
        int sampleSize = totSampleSize;
        kitSizeLabel.setText(Integer.toHexString(sampleSize) + "/3fa0 bytes used");
        boolean tooFull = sampleSize > 0x3fa0;

        Color c = tooFull ? Color.red : Color.black;
        kitSizeLabel.setForeground(c);
        instrList.setForeground(c);
    }

    private void bankBox_actionPerformed() {
        int index = bankBox.getSelectedIndex();
        if (prevBankBoxIndex == index) {
            return;
        }
        // Switched bank.
        prevBankBoxIndex = index;
        flushWavFiles();
        createSamplesFromRom();
        updateBankView();
    }

    private String getRomSampleName(int index) {
        int offset = getROMOffsetForSelectedBank() + 0x22 + index * 3;
        String name = "";
        name += (char) romImage[offset++];
        name += (char) romImage[offset++];
        name += (char) romImage[offset];
        return name;
    }

    private void createSamplesFromRom() {
        for (int sampleIt = 0; sampleIt < MAX_SAMPLES; ++sampleIt) {
            if (samples[sampleIt] != null) {
                continue;
            }
            byte[] nibbles = get4BitSamples(sampleIt);

            if (nibbles != null) {
                String name = getRomSampleName(sampleIt);
                samples[sampleIt] = Sample.createFromNibbles(nibbles, name);
            } else {
                samples[sampleIt] = null;
            }
        }
    }

    private boolean importPalettes(File f) {
        boolean isOk = false;
        RandomAccessFile otherOpenRom = null;
        try {
            byte[] otherRomImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            otherOpenRom = new RandomAccessFile(f, "r");

            otherOpenRom.readFully(otherRomImage);
            otherOpenRom.close();

            int ownPaletteOffset = RomUtilities.findPaletteOffset(romImage);
            int ownPaletteNameOffset = RomUtilities.findPaletteNameOffset(romImage);

            int otherPaletteOffset = RomUtilities.findPaletteOffset(otherRomImage);
            int otherPaletteNameOffset = RomUtilities.findPaletteNameOffset(otherRomImage);

            System.arraycopy(otherRomImage, otherPaletteOffset, romImage, ownPaletteOffset, RomUtilities.PALETTE_SIZE * RomUtilities.NUM_PALETTES);

            System.arraycopy(otherRomImage, otherPaletteNameOffset, romImage, ownPaletteNameOffset, RomUtilities.PALETTE_NAME_SIZE * RomUtilities.NUM_PALETTES);

            paletteEditor.setRomImage(romImage);

            isOk = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (otherOpenRom != null) {
                try {
                    otherOpenRom.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "File error (wth)",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return isOk;
    }

    private boolean importFonts(File f) {
        boolean isOk = false;
        RandomAccessFile otherOpenRom = null;
        try {
            byte[] otherRomImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            otherOpenRom = new RandomAccessFile(f, "r");

            otherOpenRom.readFully(otherRomImage);
            otherOpenRom.close();

            int ownFontOffset = RomUtilities.findFontOffset(romImage);
            int otherFontOffset = RomUtilities.findFontOffset(otherRomImage);

            System.arraycopy(otherRomImage, otherFontOffset, romImage, ownFontOffset, LSDJFont.FONT_SIZE * LSDJFont.FONT_COUNT);

            for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
                RomUtilities.setFontName(romImage, i, RomUtilities.getFontName(otherRomImage, i));
            }

            fontEditor.setRomImage(romImage);
            isOk = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (otherOpenRom != null) {
                try {
                    otherOpenRom.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "File error (wth)",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return isOk;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private int importKits(File f) {
        try {
            int outBank = 0;
            int copiedBankCount = 0;
            FileInputStream in = new FileInputStream(f.getAbsolutePath());
            while (in.available() > 0) {
                byte[] inBuf = new byte[RomUtilities.BANK_SIZE];
                in.read(inBuf);
                if (inBuf[0] == 0x60 && inBuf[1] == 0x40) {
                    //is kit bank
                    outBank++;
                    while (!isKitBank(outBank) && !isEmptyKitBank(outBank)) {
                        outBank++;
                    }
                    int outPtr = outBank * RomUtilities.BANK_SIZE;
                    for (int i = 0; i < RomUtilities.BANK_SIZE; i++) {
                        romImage[outPtr++] = inBuf[i];
                    }
                    copiedBankCount++;
                }
            }
            updateRomView();
            in.close();
            return copiedBankCount;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }


    private File importRomSelect() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Select ROM to import from", FileType.Gb, FileOperation.Load);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {

            File f = chooser.getSelectedFile();
            JFileChooserFactory.recordNewBaseFolder(f.getParent());
            return f;
        }
        return null;
    }

    private void importKits_actionPerformed() {
        File f = importRomSelect();
        if (f != null) {
            int amountOfCopiedKits = importKits(f);
            JOptionPane.showMessageDialog(this,
                    "Imported " + amountOfCopiedKits + " kits!",
                    "Kit import result.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void importFonts_actionPerformed() {
        File f = importRomSelect();
        if (f != null) {
            if (importFonts(f)) {
                JOptionPane.showMessageDialog(this,
                        "Font copied!",
                        "Font import result.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Font copy error.",
                        "Font import result.", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    }

    private void importPalettes_actionPerformed() {
        File f = importRomSelect();
        if (f != null) {
            if (importPalettes(f)) {
                JOptionPane.showMessageDialog(this,
                        "Palettes copied!",
                        "Palette import result.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Palette copy error.",
                        "Palette import result.", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private void importAll_actionPerformed() {
        File f = importRomSelect();
        boolean isOk = true;
        if (f != null) {
            // TODO : factorize message dialogs.
            if (importKits(f) == 0) {
                JOptionPane.showMessageDialog(this,
                        "Palette copy error.",
                        "Palette import result.", JOptionPane.INFORMATION_MESSAGE);
                isOk = false;
            }
            if (!importFonts(f)) {
                JOptionPane.showMessageDialog(this,
                        "Font copy error.",
                        "Font import result.", JOptionPane.INFORMATION_MESSAGE);
                isOk = false;
            }
            if (!importPalettes(f)) {
                JOptionPane.showMessageDialog(this,
                        "Palette copy error.",
                        "Palette import result.", JOptionPane.INFORMATION_MESSAGE);
                isOk = false;
            }
        }

        if (isOk) {
            JOptionPane.showMessageDialog(this,
                    "Everything copied!",
                    "All import result.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveROMButton_actionPerformed() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Save ROM image", FileType.Gb, FileOperation.Save);

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                JFileChooserFactory.recordNewBaseFolder(f.getParent());

                RomUtilities.fixChecksum(romImage);
                romFile = new RandomAccessFile(f, "rw");
                romFile.write(romImage);
                romFile.close();
                setTitle(f.getAbsoluteFile().toString());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportKitButton_actionPerformed() {
        JFileChooser chooser = JFileChooserFactory.createChooser("Export kit", FileType.Kit, FileOperation.Save);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {

            try {
                File f = chooser.getSelectedFile();
                JFileChooserFactory.recordNewBaseFolder(f.getParent());
                byte buf[] = new byte[RomUtilities.BANK_SIZE];
                int offset = getROMOffsetForSelectedBank();
                RandomAccessFile bankFile = new RandomAccessFile(f, "rw");

                for (int i = 0; i < buf.length; i++) {
                    buf[i] = romImage[offset++];
                }
                bankFile.write(buf);
                bankFile.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                        JOptionPane.ERROR_MESSAGE);
            }
            updateRomView();
        }

    }

    private void loadKit(File kitFile) {
        try {
            byte buf[] = new byte[RomUtilities.BANK_SIZE];
            int offset = getROMOffsetForSelectedBank();
            RandomAccessFile bankFile = new RandomAccessFile(kitFile, "r");
            bankFile.readFully(buf);

            for (byte aBuf : buf) {
                romImage[offset++] = aBuf;
            }
            bankFile.close();
            flushWavFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateRomView();
    }

    private void loadKitButton_actionPerformed() {
        FileDialog dialog = new FileDialog(this, "Load kit", FileDialog.LOAD);
        dialog.setFilenameFilter(new KitFileFilter());
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            loadKit(new File(dialog.getDirectory(), dialog.getFile()));
        }
    }

    private String dropExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(0, i);
        }
        return ext;
    }

    private void createKit() {
        //clear all bank
        int offset = getROMOffsetForSelectedBank() + 2;
        int max_offset = getROMOffsetForSelectedBank() + RomUtilities.BANK_SIZE;
        while (offset < max_offset) {
            romImage[offset++] = 0;
        }

        //clear kit name
        offset = getROMOffsetForSelectedBank() + 0x52;
        for (int i = 0; i < 6; i++) {
            romImage[offset++] = ' ';
        }

        //clear instrument names
        offset = getROMOffsetForSelectedBank() + 0x22;
        for (int i = 0; i < 15; i++) {
            romImage[offset++] = 0;
            romImage[offset++] = '-';
            romImage[offset++] = '-';
        }

        flushWavFiles();

        updateRomView();
    }

    private void flushWavFiles() {
        samples = new Sample[MAX_SAMPLES];
    }

    private void renameKitButton_actionPerformed() {
        int offset = getROMOffsetForSelectedBank() + 0x52;
        String s = kitTextArea.getText().toUpperCase();
        for (int i = 0; i < 6; i++) {
            if (i < s.length()) {
                romImage[offset++] = (byte) s.charAt(i);
            } else {
                romImage[offset++] = ' ';
            }
        }
        compileKit();
        updateRomView();
    }

    private int firstFreeSampleSlot() {
        for (int sampleIt = 0; sampleIt < MAX_SAMPLES; ++sampleIt) {
            if (samples[sampleIt] == null) {
                return sampleIt;
            }
        }
        return -1;
    }

    private void addSample(File wavFile) {
        if (isEmptyKitBank(getSelectedROMBank())) {
            createKit();
        }
        if (firstFreeSampleSlot() == -1) {
            JOptionPane.showMessageDialog(contentPane,
                    "Can't add sample, kit is full!",
                    "Kit full",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int offset = getROMOffsetForSelectedBank() + 0x22 +
                firstFreeSampleSlot() * 3;
        String s = dropExtension(wavFile).toUpperCase();

        for (int i = 0; i < 3; ++i) {
            if (s.length() > 0) {
                romImage[offset] = (byte) s.charAt(i);
            } else {
                romImage[offset] = '-';
            }

            offset++;
        }

        Sample sample = Sample.createFromWav(wavFile);
        if (sample == null) {
            return;
        }
        samples[firstFreeSampleSlot()] = sample;

        compileKit();
        updateRomView();
    }

    private void selectSampleToAdd() {
        FileDialog dialog = new FileDialog(this, "Load sample",
                FileDialog.LOAD);
        dialog.setFilenameFilter(new WavFileFilter());
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            addSample(new File(dialog.getDirectory(), dialog.getFile()));
        }
    }

    private void compileKit() {
        updateBankView();
        if (totSampleSize > 0x3fa0) {
            kitSizeLabel.setText(Integer.toHexString(totSampleSize) + "/3fa0 bytes used");
            return;
        }
        kitSizeLabel.setText(Integer.toHexString(totSampleSize) + " bytes written");
        sbc.DITHER_VAL = ditherSlider.getValue();

        byte newSamples[] = new byte[RomUtilities.BANK_SIZE];
        int lengths[] = new int[15];
        sbc.handle(newSamples, samples, lengths);

        // Checks if at least one sample has been added.
        boolean hasAnySample = false;
        for (int i = 0; i < 15; ++i) {
            if (lengths[i] > 0) {
                hasAnySample = true;
                break;
            }
        }
        if (!hasAnySample) {
            // Not allowed to create kits without samples!
            return;
        }

        //copy sampledata to ROM image
        int offset = getROMOffsetForSelectedBank() + 0x60;
        int offset2 = 0x60;
        do {
            romImage[offset++] = newSamples[offset2++];
        } while (offset2 != RomUtilities.BANK_SIZE);

        //update samplelength info in rom image
        int bankOffset = 0x4060;
        offset = getROMOffsetForSelectedBank();
        romImage[offset++] = 0x60;
        romImage[offset++] = 0x40;

        for (int i = 0; i < 15; i++) {
            bankOffset += lengths[i];
            if (lengths[i] != 0) {
                romImage[offset++] = (byte) (bankOffset & 0xff);
                romImage[offset++] = (byte) (bankOffset >> 8);
            } else {
                romImage[offset++] = 0;
                romImage[offset++] = 0;
            }
        }

        // Resets forced loop data.
        romImage[getROMOffsetForSelectedBank() + 0x5c] = 0;
        romImage[getROMOffsetForSelectedBank() + 0x5d] = 0;
    }

    private void dropSample() {
        int[] indices = instrList.getSelectedIndices();

        for (int indexIt = 0; indexIt < indices.length; ++indexIt) {
            // Assumes that indices are sorted...
            int index = indices[indexIt];

            // Moves up samples.
            if (14 - index >= 0) System.arraycopy(samples, index + 1, samples, index, 14 - index);
            samples[14] = null;

            // Moves up instr names.
            int offset = getROMOffsetForSelectedBank() + 0x22 + index * 3;
            int i;
            for (i = offset; i < getROMOffsetForSelectedBank() + 0x22 + 14 * 3; i += 3) {
                romImage[i] = romImage[i + 3];
                romImage[i + 1] = romImage[i + 4];
                romImage[i + 2] = romImage[i + 5];
            }
            romImage[i] = 0;
            romImage[i + 1] = '-';
            romImage[i + 2] = '-';

            // Adjusts indices.
            for (int indexIt2 = indexIt + 1; indexIt2 < indices.length; ++indexIt2) {
                --indices[indexIt2];
            }
        }

        compileKit();
        updateBankView();
    }

    private void exportSample() {
        Sample s = samples[instrList.getSelectedIndex()];
        if (s == null) {
            return;
        }
        FileDialog dialog = new FileDialog(this, "Save sample as .wav",
                FileDialog.SAVE);
        dialog.setFilenameFilter(new WavFileFilter());
        dialog.setFile(s.getName() + ".wav");
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        File f = new File(dialog.getDirectory(), dialog.getFile());
        if (!f.toString().toUpperCase().endsWith(".WAV")) {
            f = new File(f.getAbsoluteFile().toString() + ".wav");
        }
        s.writeToWav(f);
    }
}
