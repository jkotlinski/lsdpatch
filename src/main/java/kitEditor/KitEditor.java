package kitEditor;

import Document.Document;
import lsdpatch.LSDPatcher;
import net.miginfocom.swing.MigLayout;
import utils.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class KitEditor extends JFrame implements SamplePicker.Listener {
    private final Document document;

    public interface Listener {
        void saveRom();
    }
    Listener listener;

    private static final long serialVersionUID = -3993608561466542956L;
    private JPanel contentPane;
    private int prevBankBoxIndex = -1;
    private final JComboBox<String> bankBox = new JComboBox<>();
    private final SamplePicker samplePicker = new SamplePicker();

    private static final int MAX_SAMPLES = 15;
    private static final int MAX_SAMPLE_SPACE = 0x3fa0;

    private final java.awt.event.ActionListener bankBoxListener = e -> bankBox_actionPerformed();

    private byte[] romImage;

    private Sample[][] samples = new Sample[RomUtilities.BANK_COUNT][MAX_SAMPLES];

    private final JButton loadKitButton = new JButton();
    private final JButton saveKitButton = new JButton();
    private final JButton saveRomButton = new JButton();
    private final JButton exportSampleButton = new JButton();
    private final JButton exportAllSamplesButton = new JButton();
    private final JButton renameKitButton = new JButton();
    private final JTextArea kitTextArea = new JTextArea();
    private final JButton reloadSamplesButton = new JButton("Reload samples");
    private final JButton addSampleButton = new JButton("Add sample");
    private final JButton dropSampleButton = new JButton("Drop sample(s)");
    private final JLabel kitSizeLabel = new JLabel();
    private final SampleView sampleView = new SampleView();
    private final JSpinner volumeSpinner = new JSpinner();

    private final JCheckBox playSampleToggle = new JCheckBox("Play sample on click", true);
    private final JCheckBox playSpeedToggle = new JCheckBox("Play samples in half-speed");

    private void emptyInstrList() {
        String[] listData = {
                "---", "---", "---", "---",
                "---", "---", "---", "---",
                "---", "---", "---", "---",
                "---", "---", "---"
        };
        samplePicker.setListData(listData);
    }

    public KitEditor(Document document, Listener listener) {
        this.listener = listener;
        this.document = document;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        jbInit();
        setListeners();
        romImage = document.romImage();
        setVisible(true);
        setTitle("Kit Editor");
        updateRomView();
        createSamplesFromRom();

        saveRomButton.addActionListener(e -> {
            document.setRomImage(romImage);
            listener.saveRom();
            romImage = document.romImage();
            saveRomButton.setEnabled(false);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                document.setRomImage(romImage);
            }
        });

        pack();

        samplePicker.grabFocus();
    }

    private void setListeners() {
        bankBox.addActionListener(bankBoxListener);
        samplePicker.addListSelectionListener(this);
        volumeSpinner.addChangeListener(e -> onVolumeChanged());

        loadKitButton.addActionListener(e -> loadKit());
        saveKitButton.addActionListener(e -> saveKit());
        renameKitButton.addActionListener(e1 -> renameKit(kitTextArea.getText()));

        exportSampleButton.addActionListener(e -> exportSample());
        exportAllSamplesButton.addActionListener(e -> exportAllSamplesFromKit());
        addSampleButton.addActionListener(e -> selectSampleToAdd());
        reloadSamplesButton.addActionListener(e -> reloadSamples());
        reloadSamplesButton.setEnabled(false);
        dropSampleButton.addActionListener(e -> dropSample());
    }

    private void reloadSamples() {
        int index = samplePicker.getSelectedIndex();
        try {
            for (Sample s : samples[selectedBank]) {
                if (s != null) {
                    s.reload();
                }
            }
        } catch (Exception e) {
            showFileErrorMessage(e);
            e.printStackTrace();
        }
        compileKit();
        updateRomView();
        samplePicker.setSelectedIndex(index);
        playSample();
    }

    static boolean updatingVolume = false;
    private void onVolumeChanged() {
        if (updatingVolume) {
            return;
        }
        int index = samplePicker.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Sample sample = samples[selectedBank][index];
        if (sample == null || !sample.canAdjustVolume()) {
            return;
        }
        updatingVolume = true;
        sample.setVolumeDb((int)volumeSpinner.getValue());
        sample.processSamples(true);
        compileKit();
        samplePicker.setSelectedIndex(index);
        playSample();
        updatingVolume = false;
    }

    private void jbInit() {
        setTitle("LSDPatcher v" + LSDPatcher.getVersion());
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new MigLayout());

        createFileDrop();

        samplePicker.setBorder(BorderFactory.createEtchedBorder());

        JPanel kitContainer = new JPanel();
        TitledBorder kitContainerBorder = new TitledBorder(BorderFactory.createEtchedBorder(), "ROM Image");
        kitContainer.setBorder(kitContainerBorder);
        kitContainer.setLayout(new MigLayout("", "[grow,fill]", ""));
        kitContainer.add(bankBox, "grow,wrap");
        kitContainer.add(samplePicker, "grow,wrap");
        kitContainer.add(kitSizeLabel, "grow, split 2");
        kitContainer.add(saveRomButton, "grow");

        loadKitButton.setText("Import kit");
        saveKitButton.setText("Export kit");
        saveRomButton.setText("Save ROM");

        kitTextArea.setBorder(BorderFactory.createEtchedBorder());

        renameKitButton.setText("Rename kit");

        exportSampleButton.setEnabled(false);
        exportSampleButton.setText("Export sample");

        exportAllSamplesButton.setText("Export all samples");

        addSampleButton.setEnabled(false);
        dropSampleButton.setEnabled(false);
        volumeSpinner.setEnabled(false);

        contentPane.add(kitContainer, "grow, cell 0 0, spany");
        contentPane.add(loadKitButton, "grow, wrap");
        contentPane.add(saveKitButton, "grow, wrap, sg button");
        contentPane.add(kitTextArea, "grow,split 2");
        contentPane.add(renameKitButton, "wrap 10");

        contentPane.add(exportSampleButton, "grow, wrap, sg button");
        contentPane.add(exportAllSamplesButton, "grow, wrap, sg button");
        contentPane.add(addSampleButton, "grow, span 2, wrap, sg button");
        contentPane.add(reloadSamplesButton, "grow, span 2, wrap, sg button");
        contentPane.add(dropSampleButton, "grow, span 2,wrap 10, sg button");
        contentPane.add(playSampleToggle, "wrap");
        contentPane.add(playSpeedToggle, "wrap");
        contentPane.add(new JLabel("Volume (dB):"), "split 2");
        contentPane.add(volumeSpinner, "grow, wrap");
        contentPane.add(sampleView, "grow, span 2, wmin 10, hmin 64");
        sampleView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                playSample();
            }
        });
    }

    private void createFileDrop() {
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
    }

    private byte[] getNibbles(int index) {
        if (index < 0) {
            return null;
        }
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

    private void playSample() {
        if (!playSampleToggle.isSelected()) {
            return;
        }
        byte[] nibbles = getNibbles(samplePicker.getSelectedIndex());
        if (nibbles == null) {
            return;
        }
        try {
            Sound.play(nibbles, playSpeedToggle.isSelected());
            sampleView.setBufferContent(nibbles);
            sampleView.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Audio error",
                    JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
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

        byte[] buf = new byte[6];
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

    private int selectedBank = -1;

    private int getSelectedUiBank() {
        if (bankBox.getSelectedIndex() > -1) {
            selectedBank = bankBox.getSelectedIndex();
        }
        return selectedBank;
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

        byte[] buf = new byte[3];
        String[] s = new String[15];

        int bankOffset = getROMOffsetForSelectedBank();
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
            s[instrNo] = new String(buf);
        }
        samplePicker.setListData(s);

        updateKitSizeLabel();
        addSampleButton.setEnabled(firstFreeSampleSlot() != -1);

        updateButtonStates();
    }

    private void updateKitSizeLabel() {
        int sampleSize = totalSampleSize();
        kitSizeLabel.setText(Integer.toHexString(sampleSize) + "/3fa0 bytes used");
        boolean tooFull = sampleSize > 0x3fa0;

        Color c = tooFull ? Color.red : Color.black;
        kitSizeLabel.setForeground(c);
        samplePicker.setForeground(c);
    }

    private void bankBox_actionPerformed() {
        int index = bankBox.getSelectedIndex();
        if (prevBankBoxIndex == index) {
            return;
        }
        // Switched bank.
        prevBankBoxIndex = index;
        if (samples[selectedBank] == null) {
            flushWavFiles();
            createSamplesFromRom();
        }
        updateBankView();
        samplePicker.setSelectedIndex(-1);
        selectionChanged();
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
            if (samples[selectedBank][sampleIt] != null) {
                continue;
            }
            byte[] nibbles = getNibbles(sampleIt);

            if (nibbles != null) {
                String name = getRomSampleName(sampleIt);
                samples[selectedBank][sampleIt] = Sample.createFromNibbles(nibbles, name);
            } else {
                samples[selectedBank][sampleIt] = null;
            }
        }
    }

    private void showFileErrorMessage(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "File error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void saveKit() {
        File f = FileDialogLauncher.save(this, "Export Kit", "kit");
        if (f == null) {
            return;
        }
        try {
            KitArchive.save(samples[selectedBank], f);
        } catch (IOException e) {
            showFileErrorMessage(e);
        }
        updateRomView();
    }

    private void loadKit() {
        File kitFile = FileDialogLauncher.load(this, "Load Sample Kit", "kit");
        if (kitFile != null) {
            loadKit(kitFile);
        }
    }

    private void loadKit(File kitFile) {
        createKit();
        try {
            if (kitFile.length() == RomUtilities.BANK_SIZE) {
                loadKitV1(kitFile);
            } else {
                loadKitV2(kitFile);
            }
            createSamplesFromRom();
            updateBankView();
        } catch (Exception e) {
            showFileErrorMessage(e);
        }
        updateRomView();
    }

    private void loadKitV2(File kitFile) throws IOException {
        KitArchive.load(kitFile, samples[selectedBank]);
        renameKit(kitFile.getName().split("\\.")[0]);
        for (int i = 0; i < MAX_SAMPLES; ++i) {
            Sample sample = samples[selectedBank][i];
            if (sample != null) {
                renameSample(i, sample.getName());
            }
        }
    }

    private void loadKitV1(File kitFile) throws IOException {
        byte[] buf = new byte[RomUtilities.BANK_SIZE];
        int offset = getROMOffsetForSelectedBank();
        RandomAccessFile bankFile = new RandomAccessFile(kitFile, "r");
        bankFile.readFully(buf);
        for (byte aBuf : buf) {
            romImage[offset++] = aBuf;
        }
        bankFile.close();
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
        samples = new Sample[RomUtilities.BANK_COUNT][MAX_SAMPLES];
    }

    private void renameKit(String s) {
        s = s.toUpperCase();
        int offset = getROMOffsetForSelectedBank() + 0x52;
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
            if (samples[selectedBank][sampleIt] == null) {
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
        String s = dropExtension(wavFile).toUpperCase();
        renameSample(firstFreeSampleSlot(), s);

        Sample sample;
        try {
            sample = Sample.createFromWav(wavFile, true);
        } catch (Exception e) {
            showFileErrorMessage(e);
            return;
        }

        if (sample.lengthInBytes() > MAX_SAMPLE_SPACE - totalSampleSize()) {
            JOptionPane.showMessageDialog(this,
                    "Free up some space and try again!",
                    "Kit full!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int index = firstFreeSampleSlot();
        assert index != -1;
        samples[selectedBank][index] = sample;
        compileKit();
        updateRomView();
        samplePicker.setSelectedIndex(index);
        playSample();
        reloadSamplesButton.setEnabled(true);
    }

    private void renameSample(int sampleIndex, String sampleName) {
        int offset = getROMOffsetForSelectedBank() + 0x22 + sampleIndex * 3;
        for (int i = 0; i < 3; ++i) {
            if (i < sampleName.length()) {
                romImage[offset] = (byte) sampleName.toUpperCase().charAt(i);
            } else {
                romImage[offset] = '-';
            }
            offset++;
        }
    }

    private void selectSampleToAdd() {
        File f = FileDialogLauncher.load(this, "Load Sample", "wav");
        if (f != null) {
            addSample(f);
        }
    }

    private void compileKit() {
        if (totalSampleSize() > 0x3fa0) {
            kitSizeLabel.setText(Integer.toHexString(totalSampleSize()) + "/3fa0 bytes used");
            return;
        }
        kitSizeLabel.setText(Integer.toHexString(totalSampleSize()) + " bytes written");

        byte[] newSamples = new byte[RomUtilities.BANK_SIZE];
        int[] lengths = new int[15];
        sbc.compile(newSamples, samples[selectedBank], lengths);

        //copy sampledata to ROM image
        int offset = getROMOffsetForSelectedBank() + 0x60;
        int offset2 = 0x60;
        System.arraycopy(newSamples, offset2, romImage, offset, RomUtilities.BANK_SIZE - offset2);

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

    private int totalSampleSize() {
        int total = 0;
        for (Sample s : samples[selectedBank]) {
            total += s == null ? 0 : s.lengthInBytes();
        }
        return total;
    }

    private void dropSample() {
        ArrayList<Integer> indices = samplePicker.getSelectedIndices();
        for (int indexIt = 0; indexIt < indices.size(); ++indexIt) {
            // Assumes that indices are sorted...
            int index = indices.get(indexIt);

            // Moves up samples.
            if (14 - index >= 0) {
                System.arraycopy(samples[selectedBank],
                        index + 1,
                        samples[selectedBank],
                        index,
                        14 - index);
            }
            samples[selectedBank][14] = null;

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
            for (int indexIt2 = indexIt + 1; indexIt2 < indices.size(); ++indexIt2) {
                indices.set(indexIt2, indices.get(indexIt2) - 1);
            }
        }
        compileKit();
        updateBankView();
    }

    // TODO : put this in a factory eventually
    private String selectAFolder() {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(new File(EditorPreferences.lastDirectory("wav")));
        chooser.setDialogTitle("Export all samples to directory");

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toString();
        }
        return null;
    }

    private String getKitName() {
        String kitName = (String)bankBox.getSelectedItem();
        assert kitName != null;
        return kitName.substring(kitName.indexOf(' ') + 1);
    }

    private void exportAllSamplesFromKit() {
        String directory = selectAFolder();
        if (directory == null) {
            return;
        }

        int index = 0;
        String kitName = getKitName();
        if (kitName.length() == 0) {
            kitName = String.format("Untitled-%02d", bankBox.getSelectedIndex());
        }

        for (Sample s : samples[selectedBank]) {
            if (s == null) {
                continue;
            }

            String sampleName = s.getName();
            if (sampleName.length() == 0) {
                sampleName = "[untitled]";
            }
            File exportedFile = new File(directory,
                    String.format("%s - %02d - %s.wav", kitName, index + 1, sampleName));
            try {
                WaveFile.write(s.workSampleData(), exportedFile);
            } catch (IOException e) {
                showFileErrorMessage(e);
                EditorPreferences.setLastPath("wav", exportedFile.getAbsolutePath());
            }
            index++;
        }

        JOptionPane.showMessageDialog(this,
                "Saved " + index + " wave files!",
                "Export OK!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportSample() {
        File f = FileDialogLauncher.save(this, "Save Sample", "wav");
        if (f != null) {
            try {
                WaveFile.write(samples[selectedBank][samplePicker.getSelectedIndex()].workSampleData(), f);
            } catch (IOException e) {
                showFileErrorMessage(e);
            }
        }
    }

    @Override
    public void selectionChanged() {
        updateButtonStates();
        playSample();
    }

    private void updateButtonStates() {
        int index = samplePicker.getSelectedIndex();
        dropSampleButton.setEnabled(index >= 0 && samples[selectedBank][index] != null);
        exportSampleButton.setEnabled(getNibbles(index) != null);
        Sample sample = index >= 0 ? samples[selectedBank][index] : null;
        boolean enableVolume = sample != null && sample.canAdjustVolume();
        updatingVolume = true;
        volumeSpinner.setEnabled(enableVolume);
        volumeSpinner.setValue(enableVolume ? sample.volumeDb() : 0);
        updatingVolume = false;
        reloadSamplesButton.setEnabled(false);
        for (Sample s : samples[selectedBank]) {
            if (s != null && s.localPath() != null) {
                reloadSamplesButton.setEnabled(true);
            }
        }
        saveRomButton.setEnabled(!Arrays.equals(document.romImage(), romImage));
    }

    @Override
    public void delete() {
        dropSample();
    }
}