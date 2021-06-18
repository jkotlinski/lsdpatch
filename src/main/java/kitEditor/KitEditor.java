package kitEditor;

import Document.Document;
import com.laszlosystems.libresample4j.Resampler;
import net.miginfocom.swing.MigLayout;
import utils.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class KitEditor extends JFrame implements SamplePicker.Listener {
    private final Document document;

    public interface Listener {
        void saveRom();
    }
    Listener listener;

    private static final long serialVersionUID = -3993608561466542956L;
    private JPanel contentPane;
    private final JComboBox<String> bankBox = new JComboBox<>();
    private final SamplePicker samplePicker = new SamplePicker();
    private final String SETTINGS_FILE_EXTENSION = ".settings";

    private static final int MAX_SAMPLES = 15;
    private static final int MAX_SAMPLE_SPACE = 0x3fa0;

    private final java.awt.event.ActionListener bankBoxListener = e -> bankBox_actionPerformed();

    private static final byte KIT_VERSION_1 = 1;

    private byte[] romImage;

    private final Sample[][] samples = new Sample[RomUtilities.BANK_COUNT][MAX_SAMPLES];

    private final JButton previousBankButton = new JButton("<");
    private final JButton nextBankButton = new JButton(">");

    private final JButton loadKitButton = new JButton();
    private final JButton saveKitButton = new JButton();
    private final JButton saveRomButton = new JButton();
    private final JButton exportSampleButton = new JButton();
    private final JButton clearKitButton = new JButton("Clear kit");
    private final JButton renameKitButton = new JButton();
    private final JTextField kitNameTextField = new JTextField();
    private final JButton reloadSampleButton = new JButton("Reload sample");
    private final JButton addSampleButton = new JButton("Add sample");
    private final JLabel kitSizeLabel = new JLabel();
    private final SampleView sampleView = new SampleView();
    private final JSpinner volumeSpinner = new JSpinner();
    private final JSpinner pitchSpinner = new JSpinner();
    private final JSpinner trimSpinner = new JSpinner();
    private final JCheckBox halfSpeed = new JCheckBox("Half-speed");
    private final JCheckBox dither = new JCheckBox("Dither", true);

    public KitEditor(JFrame parent, Document document, Listener listener) {
        parent.setEnabled(false);

        romImage = document.romImage();
        this.listener = listener;
        this.document = document;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        jbInit();
        addMenu();
        setListeners();
        setVisible(true);
        setTitle("Kit Editor");
        createSamplesFromRom();
        updateRomView();

        saveRomButton.addActionListener(e -> {
            document.setRomImage(romImage);
            listener.saveRom();
            romImage = document.romImage();
            updateButtonStates();
        });

        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        PadKeyHandler padKeyHandler = new PadKeyHandler();
        keyboardFocusManager.addKeyEventPostProcessor(padKeyHandler);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                keyboardFocusManager.removeKeyEventPostProcessor(padKeyHandler);
                document.setRomImage(romImage);
                parent.setEnabled(true);
            }
        });

        pack();

        samplePicker.grabFocus();

        setResizable(false);
    }

    private void addEnterHandler(JSpinner spinner) {
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    requestFocus();
                }
            }
        });
    }

    private void setListeners() {
        bankBox.addActionListener(bankBoxListener);
        samplePicker.addListSelectionListener(this);
        volumeSpinner.addChangeListener(e -> onSpinnerChanged());
        addEnterHandler(volumeSpinner);
        pitchSpinner.addChangeListener(e -> onSpinnerChanged());
        addEnterHandler(pitchSpinner);
        trimSpinner.addChangeListener(e -> onSpinnerChanged());
        halfSpeed.addActionListener(e -> onHalfSpeedChanged());
        dither.addActionListener(e -> onSpinnerChanged());

        Action previousBankAction = new AbstractAction("previous bank") {
            @Override
            public void actionPerformed(ActionEvent e) {
                bankBox.setSelectedIndex(bankBox.getSelectedIndex() - 1);
            }
        };
        previousBankAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
        previousBankButton.addActionListener(previousBankAction);
        previousBankButton.getActionMap().put("previousBankAction", previousBankAction);
        previousBankButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) previousBankAction.getValue(Action.ACCELERATOR_KEY), "previousBankAction");
        previousBankButton.setToolTipText("F1");

        Action nextBankAction = new AbstractAction("next bank") {
            @Override
            public void actionPerformed(ActionEvent e) {
                bankBox.setSelectedIndex(bankBox.getSelectedIndex() + 1);
            }
        };
        nextBankAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F2"));
        nextBankButton.addActionListener(nextBankAction);
        nextBankButton.getActionMap().put("nextBankAction", nextBankAction);
        nextBankButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) nextBankAction.getValue(Action.ACCELERATOR_KEY), "nextBankAction");
        nextBankButton.setToolTipText("F2");

        loadKitButton.addActionListener(e -> loadKit());
        saveKitButton.addActionListener(e -> saveKit());
        clearKitButton.addActionListener(e -> createKit());
        renameKitButton.addActionListener(e -> renameKit(kitNameTextField.getText()));
        kitNameTextField.addActionListener(e -> {
            renameKit(kitNameTextField.getText());
            requestFocus();
        });

        exportSampleButton.addActionListener(e -> exportSample());
        addSampleButton.addActionListener(e -> addSample());
        reloadSampleButton.addActionListener(e -> reloadSample());
        reloadSampleButton.setEnabled(false);
    }

    private void onHalfSpeedChanged() {
        // Update trim accordingly.
        for (Sample s : samples[selectedBank]) {
            if (s != null) {
                if (halfSpeed.isSelected()) {
                    s.setTrim((int)Math.ceil(s.getTrim() / 2.0)); // Round upwards.
                } else {
                    s.setTrim(s.getTrim() * 2);
                }
            }
        }
        reloadAllSamples();
    }
    
    private void reloadAllSamples() {
        int index = samplePicker.getSelectedIndex();
        try {
            for (Sample s : samples[selectedBank]) {
                if (s != null) {
                    s.reload(halfSpeed.isSelected());
                }
            }
        } catch (Exception e) {
            showFileErrorMessage(e);
            e.printStackTrace();
        }
        compileKit();
        updateRomView();
        samplePicker.setSelectedIndex(index);
    }

    private void reloadSample() {
        int index = samplePicker.getSelectedIndex();
        Sample sample = samples[selectedBank][index];
        if (sample != null) {
            try {
                sample.reload(halfSpeed.isSelected());
            } catch (Exception e) {
                showFileErrorMessage(e);
                e.printStackTrace();
            }
        }
        compileKit();
        updateRomView();
        playSample();
        samplePicker.setSelectedIndex(index);
    }

    static boolean handlingSpinnerChange = false;
    private void onSpinnerChanged() {
        if (handlingSpinnerChange) {
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
        handlingSpinnerChange = true;
        sample.setVolumeDb((int)volumeSpinner.getValue());
        if ((int)pitchSpinner.getValue() != sample.getPitchSemitones()) {
            sample.setPitchSemitones((int) pitchSpinner.getValue());
            try {
                sample.reload(halfSpeed.isSelected());
            } catch (UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
                showFileErrorMessage(e);
            }
        }
        sample.setTrim((int)trimSpinner.getValue());
        sample.processSamples(dither.isSelected());
        compileKit();
        if (bytesFree() < 0) {
            // Sample did not fit, likely due to increased volume. Trim to fit.
            int fixedTrim = sample.getTrim() - bytesFree() / 16;
            assert fixedTrim >= 0;
            trimSpinner.setValue(fixedTrim);
            sample.setTrim(fixedTrim);
            sample.processSamples(dither.isSelected());
            compileKit();
        }
        // Makes sure trim is in valid range.
        int maxTrim = maxTrim(sample);
        if ((int)trimSpinner.getValue() > maxTrim) {
            trimSpinner.setValue(maxTrim);
            sample.setTrim(maxTrim);
            sample.processSamples(dither.isSelected());
            compileKit();
        }
        samplePicker.setSelectedIndex(index);
        Sound.stopAll();
        playSample();
        handlingSpinnerChange = false;
        updateKitSizeLabel();
    }

    private double ask(String message, double value) {
        try {
            value = Double.parseDouble(JOptionPane.showInputDialog(message, value));
        } catch (NullPointerException | NumberFormatException ignored) {
        }
        return value;
    }

    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu preferences = new JMenu("Preferences");
        JMenuItem lpFilter = new JMenuItem("Low-Pass Filter...");
        lpFilter.addActionListener(e -> {
            Resampler.Beta = ask("Kaiser Window Beta", Resampler.Beta);
            Resampler.RollOff = ask( "Kaiser Window Roll-Off (0-1, 1=Nyquist)", Resampler.RollOff);
        });
        preferences.add(lpFilter);

        JMenu edit = new JMenu("Edit");
        JMenuItem trimAll = new JMenuItem("Trim all samples to fit");
        trimAll.addActionListener(e -> {
            trimAllSamples();
        });
        edit.add(trimAll);

        menuBar.add(preferences);        
        menuBar.add(edit);
        
        setJMenuBar(menuBar);
    }

    private void jbInit() {
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new MigLayout());

        createFileDrop();

        samplePicker.setBorder(BorderFactory.createEtchedBorder());

        JPanel kitContainer = new JPanel();
        kitContainer.setLayout(new MigLayout("", "[grow,fill]", ""));
        kitContainer.add(bankBox, "grow,split 3");
        kitContainer.add(previousBankButton);
        kitContainer.add(nextBankButton, "wrap");
        kitContainer.add(samplePicker, "grow,wrap");
        kitContainer.add(kitSizeLabel, "grow, split 2");
        kitContainer.add(saveRomButton, "grow");

        loadKitButton.setText("Load kit");
        saveKitButton.setText("Save kit");
        saveRomButton.setText("Save ROM");

        kitNameTextField.setBorder(BorderFactory.createLoweredBevelBorder());

        renameKitButton.setText("Rename kit");

        exportSampleButton.setEnabled(false);
        exportSampleButton.setText("Export sample");

        addSampleButton.setEnabled(false);
        volumeSpinner.setEnabled(false);
        pitchSpinner.setEnabled(false);
        trimSpinner.setEnabled(false);
        dither.setEnabled(false);

        contentPane.add(kitContainer, "grow, cell 0 0, spany");
        contentPane.add(loadKitButton, "grow, wrap");
        contentPane.add(saveKitButton, "grow, wrap, sg button");
        contentPane.add(clearKitButton, "gaptop 5, grow, wrap, sg button");
        contentPane.add(kitNameTextField, "grow, wmin 60, split 2");
        contentPane.add(renameKitButton, "wrap 10");

        contentPane.add(exportSampleButton, "grow, wrap, sg button");
        contentPane.add(addSampleButton, "grow, span 2, wrap, sg button");
        contentPane.add(reloadSampleButton, "grow, span 2, wrap, sg button");
        contentPane.add(halfSpeed, "split 2");
        contentPane.add(dither, "grow, wrap");
        contentPane.add(new JLabel("Volume (dB):"), "split 2");
        contentPane.add(volumeSpinner, "grow, wrap");
        contentPane.add(new JLabel("Pitch (semitone):"), "split 2");
        contentPane.add(pitchSpinner, "grow, wrap");
        contentPane.add(new JLabel("Trim (frames):"), "split 2");
        contentPane.add(trimSpinner, "grow, wrap");
        contentPane.add(sampleView, "grow, span 2, wmin 10, hmin 64");
        sampleView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                playSample();
            }
        });

        dither.setToolTipText("Removes 4-bit distortion by adding noise.");
        halfSpeed.setToolTipText("Half sample-rate, double kit length. Use with SPEED 0.5X kit setting.");
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

    private static void unSwizzle(byte[] packedNibbles) {
        assert(packedNibbles.length % 16 == 0);

        // Rotates the wave frame left and inverts the signal. Mirrors sbc.java.
        byte[] tmpBuf = new byte[packedNibbles.length * 2];
        for (int i = 0; i < packedNibbles.length; i += 16) {
            for (int j = 0; j < 16; ++j) {
                int b = packedNibbles[i + j];
                int dst = ((2 * j + 31) % 32) + (i * 2);
                tmpBuf[dst] = (byte) ((0xf0 - (b & 0xf0)) >> 4);
                dst = 2 * (i + j);
                tmpBuf[dst] = (byte) ((0xf - (b & 0xf)) << 4);
            }
        }

        for (int i = 0; i < packedNibbles.length; ++i) {
            packedNibbles[i] = (byte)(tmpBuf[i * 2] | tmpBuf[i * 2 + 1]);
        }
    }

    private byte[] getNibbles(int index) {
        if (index < 0) {
            return null;
        }
        final int romBank = getSelectedROMBank();
        int offset = romBank * RomUtilities.BANK_SIZE + index * 2;
        int start = (0xff & romImage[offset]) | ((0xff & romImage[offset + 1]) << 8);
        int stop = (0xff & romImage[offset + 2]) | ((0xff & romImage[offset + 3]) << 8);
        if (stop <= start) {
            return null;
        }
        byte[] arr = new byte[stop - start];
        System.arraycopy(romImage,
                (romBank - 1) * RomUtilities.BANK_SIZE + start,
                arr,
                0,
                stop - start);
        if (isBankSwizzled()) {
            unSwizzle(arr);
        }
        return arr;
    }

    private boolean isBankSwizzled() {
        return bankVersion() == KIT_VERSION_1;
    }

    private byte bankVersion() {
        // This version field is new for lsdpatcher 1.11.1 and lsdj 9.2.2.
        // Old kits may have preexisting data here: it cannot be
        // assumed that all old kits are set to version 0.
        return romImage[versionOffset()];
    }

    private int versionOffset() {
        return getROMOffsetForSelectedBank() + 0x5f;
    }

    @Override
    public void playSample() {
        byte[] nibbles = getNibbles(samplePicker.getSelectedIndex());
        if (nibbles == null) {
            return;
        }
        try {
            Sound.play(nibbles, halfSpeed.isSelected());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Audio error",
                    JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
        updateSampleView();
    }

    private void updateSampleView() {
        int sampleIndex = samplePicker.getSelectedIndex();
        byte[] nibbles = getNibbles(sampleIndex);
        if (nibbles == null) {
            return;
        }
        float duration = samples[selectedBank][sampleIndex].lengthInSamples();
        duration /= halfSpeed.isSelected() ? 5734 : 11468;
        sampleView.setBufferContent(nibbles, duration);
        sampleView.repaint();
    }

    private boolean isKitBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == 0x60 && l_char_2 == 0x40);
    }

    private boolean isUninitializedBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == -1 && l_char_2 == -1);
    }

    private String getKitName(int a_bank) {
        if (isUninitializedBank(a_bank)) {
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
            if (isKitBank(bankNo) || isUninitializedBank(bankNo)) {
                bankBox.addItem(Integer.toHexString(++l_ui_index).toUpperCase() + ". " + getKitName(bankNo));
            }
        }
        bankBox.setSelectedIndex(tmp == -1 ? 0 : tmp);
        bankBox.addActionListener(bankBoxListener);
        updateBankView();
        updateSampleView();
    }

    private int selectedBank;

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
            if (isKitBank(l_rom_bank) || isUninitializedBank(l_rom_bank)) {
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

    boolean kitTooBig() {
        return totalSampleSizeInBytes() > MAX_SAMPLE_SPACE;
    }

    private int bytesFree() {
        return MAX_SAMPLE_SPACE - totalSampleSizeInBytes();
    }

    private void updateKitSizeLabel() {
        int sampleRate = halfSpeed.isSelected() ? 5734 : 11468;
        float timeFree = (bytesFree() * 2.f) / sampleRate;
        kitSizeLabel.setText(String.format(Locale.US, "%.3f seconds free", timeFree));
        kitSizeLabel.setForeground(timeFree < 0 ? Color.red : Color.black);
    }

    private boolean isEmpty(Sample[] samples) {
        for (Sample s : samples) {
            if (s != null) {
                return false;
            }
        }
        return true;
    }

    private void bankBox_actionPerformed() {
        requestFocus();
        if (selectedBank == bankBox.getSelectedIndex()) {
            return;
        }
        selectedBank = bankBox.getSelectedIndex();
        if (isUninitializedBank(getSelectedROMBank())) {
            createKit();
        }
        if (isEmpty(samples[selectedBank])) {
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
        File f = FileDialogLauncher.save(this, "Save Kit", "kit");
        if (f == null) {
            return;
        }
        byte[] buf = new byte[RomUtilities.BANK_SIZE];
        int offset = getROMOffsetForSelectedBank();
        try {
            RandomAccessFile bankFile = new RandomAccessFile(f, "rw");
            for (int i = 0; i < buf.length; i++) {
                buf[i] = romImage[offset++];
            }
            bankFile.write(buf);
            bankFile.close();

            saveKitSettings(f);
        } catch (IOException e) {
            showFileErrorMessage(e);
        }
        updateRomView();
    }

    private void saveKitSettings(File kitFile) throws IOException {
        File kitSettingsFile = new File(kitFile.getAbsolutePath() + SETTINGS_FILE_EXTENSION);
        boolean hasFiles = false;
        for (Sample s : samples[selectedBank]) {
            if (s != null && s.getFile() != null) {
                hasFiles = true;
                break;
            }
        }
        if (!hasFiles) {
            return;
        }
        try (FileWriter fileWriter = new FileWriter(kitSettingsFile)) {
            for (Sample s : samples[selectedBank]) {
                if (s == null || s.getFile() == null) {
                    fileWriter.write("\n");
                    continue;
                }
                fileWriter.write(s.getFile().getAbsolutePath() +
                        "|" + s.getVolumeDb() +
                        "|" + s.getTrim() +
                        "|" + s.getPitchSemitones() + "\n");
            }
        }
    }

    private void loadKit() {
        File kitFile = FileDialogLauncher.load(this, "Load Sample Kit", "kit");
        if (kitFile != null) {
            loadKit(kitFile);
        }
    }

    private void loadKit(File kitFile) {
        createKit();
        renameKit(kitFile.getName());
        byte[] buf = new byte[RomUtilities.BANK_SIZE];
        int offset = getROMOffsetForSelectedBank();
        try {
            RandomAccessFile bankFile = new RandomAccessFile(kitFile, "r");
            bankFile.readFully(buf);
            if (buf[0] != 0x60 || buf[1] != 0x40) {
                JOptionPane.showMessageDialog(this,
                        "Malformed kit file!",
                        "File error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (byte aBuf : buf) {
                romImage[offset++] = aBuf;
            }
            bankFile.close();
            flushWavFiles();
            createSamplesFromRom();
            loadKitSettings(kitFile);
            updateBankView();
        } catch (IOException | UnsupportedAudioFileException e) {
            showFileErrorMessage(e);
        }
        updateRomView();
    }

    private void loadKitSettings(File kitFile) throws IOException, UnsupportedAudioFileException {
        File kitSettingsFile = new File(kitFile.getAbsolutePath() + SETTINGS_FILE_EXTENSION);
        if (!kitSettingsFile.exists()) {
            return;
        }
        try (BufferedReader fileReader = new BufferedReader(new FileReader(kitSettingsFile))) {
            for (int i = 0; i < MAX_SAMPLES; ++i) {
                String line = fileReader.readLine();
                if (line.isEmpty()) {
                    continue;
                }
                String[] chunks = line.split("\\|");
                File sampleFile = new File(chunks[0]);
                if (!sampleFile.exists()) {
                    continue;
                }
                int volume = Integer.parseInt(chunks[1]);
                int trim = 0;
                if (chunks.length > 2) {
                    trim = Integer.parseInt(chunks[2]);
                }
                int pitch = 0;
                if (chunks.length > 3) {
                    pitch = Integer.parseInt(chunks[3]);
                }
                samples[selectedBank][i] = Sample.createFromWav(
                        sampleFile,
                        dither.isSelected(),
                        halfSpeed.isSelected(),
                        volume,
                        trim,
                        pitch);
            }
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
        int offset = getROMOffsetForSelectedBank();
        int max_offset = getROMOffsetForSelectedBank() + RomUtilities.BANK_SIZE;
        while (offset < max_offset) {
            romImage[offset++] = -1;
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
        for (int i = 0; i < MAX_SAMPLES; ++i) {
            samples[selectedBank][i] = null;
        }
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
        if (firstFreeSampleSlot() == -1) {
            JOptionPane.showMessageDialog(contentPane,
                    "Can't add sample, kit is full!",
                    "Kit full",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String sampleName = dropExtension(wavFile).toUpperCase();
        Sample sample;
        try {
            sample = Sample.createFromWav(wavFile, dither.isSelected(), halfSpeed.isSelected(), 0, 0, 0);
            int bytesFreeAfterAdd = MAX_SAMPLE_SPACE - totalSampleSizeInBytes() - sample.lengthInBytes();
            if (bytesFreeAfterAdd < 0) {
                int trim = -bytesFreeAfterAdd / 16;
                sample.setTrim(trim);
                sample.reload(halfSpeed.isSelected());
                JOptionPane.showMessageDialog(this,
                        "Trimmed sample to fit.",
                        "Kit full!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            showFileErrorMessage(e);
            return;
        }

        int index = firstFreeSampleSlot();
        assert index != -1;
        renameSample(index, sampleName);
        samples[selectedBank][index] = sample;
        compileKit();
        updateRomView();
        samplePicker.setSelectedIndex(index);
        playSample();
        updateButtonStates();
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

    private void addSample() {
        File f = FileDialogLauncher.load(this, "Load Sample", "wav");
        if (f != null) {
            addSample(f);
        }
    }

    private void compileKit() {
        if (kitTooBig()) {
            return;
        }

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

        // Version number.
        romImage[versionOffset()] = KIT_VERSION_1;
    }

    private int totalSampleSizeInBytes() {
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
    }

    private int maxTrim(Sample sample) {
        int maxTrim = sample.untrimmedLengthInSamples() / 32 - 1;
        return Math.max(0, maxTrim);
    }

    private int modelMaxTrim;
    private void updateTrimModel(Sample sample) {
        if (sample == null) {
            return;
        }
        int maxTrim = maxTrim(sample);
        if (modelMaxTrim == maxTrim) {
            return;
        }
        int trim = sample.getTrim();
        assert trim >= 0;
        trim = Math.min(trim, maxTrim);
        trimSpinner.setModel(new SpinnerNumberModel(trim, 0, maxTrim, 1));
        addEnterHandler(trimSpinner);
        modelMaxTrim = maxTrim;
    }

    private void updateButtonStates() {
        int index = samplePicker.getSelectedIndex();
        exportSampleButton.setEnabled(getNibbles(index) != null);
        Sample sample = index >= 0 ? samples[selectedBank][index] : null;
        boolean enableVolume = sample != null && sample.canAdjustVolume();
        handlingSpinnerChange = true;
        dither.setEnabled(enableVolume);
        volumeSpinner.setEnabled(enableVolume);
        volumeSpinner.setValue(enableVolume ? sample.getVolumeDb() : 0);
        pitchSpinner.setEnabled(enableVolume);
        pitchSpinner.setValue(enableVolume ? sample.getPitchSemitones() : 0);
        trimSpinner.setEnabled(enableVolume && sample.untrimmedLengthInSamples() > 32);
        if (trimSpinner.isEnabled()) {
            updateTrimModel(sample);
            trimSpinner.setValue(enableVolume ? sample.getTrim() : 0);
        }
        handlingSpinnerChange = false;
        reloadSampleButton.setEnabled(enableVolume);
        addSampleButton.setEnabled(totalSampleSizeInBytes() < MAX_SAMPLE_SPACE);
        saveRomButton.setEnabled(!kitTooBig() &&
                (!Arrays.equals(document.romImage(), romImage) ||
                        document.isRomDirty()));

        previousBankButton.setEnabled(selectedBank > 0);
        nextBankButton.setEnabled(selectedBank + 1 < bankBox.getItemCount());
    }
    
    private void trimAllSamples() {
        if (firstFreeSampleSlot() == 0 || firstFreeSampleSlot() == 1) {
                JOptionPane.showMessageDialog(this,
                        "Add more than 1 sample",
                        "No samples to trim",
                        JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int numberOfSamples = firstFreeSampleSlot() > 1 ? firstFreeSampleSlot() : MAX_SAMPLES;
        int trimmableSamples = 0, usedBytes = 0;
        for (int sampleIt = 0; sampleIt < numberOfSamples; ++sampleIt) {
            Sample sample = samples[selectedBank][sampleIt];
            if (sample != null) {
                trimmableSamples = sample.canAdjustVolume() ? trimmableSamples + 1 : trimmableSamples;
                usedBytes = sample.canAdjustVolume() ? usedBytes : usedBytes + sample.untrimmedLengthInBytes();
            }
        }
        if (trimmableSamples < 1) {
            JOptionPane.showMessageDialog(this,
                "No trimmable samples",
                "No samples to trim",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int equalSampleLength = (int)((MAX_SAMPLE_SPACE - usedBytes)  / trimmableSamples);
        // first calculate if any samples are shorter than equal length and add
        int addLength = 0, sampleCount = 0;
        for (int sampleIt = 0; sampleIt < MAX_SAMPLES; ++sampleIt) {
            Sample sample = samples[selectedBank][sampleIt];
            if (sample != null && sample.canAdjustVolume()) {
                if (sample.untrimmedLengthInBytes() < equalSampleLength) {
                    addLength += equalSampleLength - sample.untrimmedLengthInBytes();
                    ++sampleCount;
                }
            }
        }
        int maxEqualSampleLength = sampleCount > 0 && trimmableSamples > sampleCount
            ? equalSampleLength + (int)addLength / (trimmableSamples - sampleCount)
            : equalSampleLength;
        for (int sampleIt = 0; sampleIt < MAX_SAMPLES; ++sampleIt) {
            Sample sample = samples[selectedBank][sampleIt];
            if (sample != null && sample.canAdjustVolume()) {
                int trim = sample.untrimmedLengthInBytes() > maxEqualSampleLength 
                    ? (int)Math.round((sample.untrimmedLengthInBytes() - maxEqualSampleLength) / 16.0)
                    : 0;
                sample.setTrim(trim);
            }
        }
        samplePicker.setSelectedIndex(numberOfSamples - 1);
        Sample sample = samples[selectedBank][numberOfSamples - 1];
        if (bytesFree() < 0) {
            // adjust last sample to account for rounding
            int fixedTrim = sample.getTrim() - bytesFree() / 16;
            assert fixedTrim > 0;
            trimSpinner.setValue(fixedTrim);
            sample.setTrim(fixedTrim);
            sample.processSamples(dither.isSelected());
            compileKit();
        }
        // Makes sure trim is in valid range.
        int maxTrim = maxTrim(sample);
        if ((int)trimSpinner.getValue() > maxTrim) {
            trimSpinner.setValue(maxTrim);
            sample.setTrim(maxTrim);
            sample.processSamples(dither.isSelected());
            compileKit();
        }
        updateButtonStates();
        reloadAllSamples();
        compileKit();
        updateRomView();
        JOptionPane.showMessageDialog(this,
                "Trimmed all samples to fit.",
                "Kit full",
                JOptionPane.INFORMATION_MESSAGE);
    }
    

    @Override
    public void deleteSample() {
        dropSample();
    }

    @Override
    public void replaceSample() {
        File wavFile = FileDialogLauncher.load(this, "Load Sample", "wav");
        if (wavFile == null) {
            return;
        }
        try {
            final int index = samplePicker.getSelectedIndex();
            Sample newSample = Sample.createFromWav(wavFile, dither.isSelected(), halfSpeed.isSelected(), 0, 0, 0);
            Sample existingSample = samples[selectedBank][index];
            int bytesFreeAfterAdd = bytesFree() - newSample.lengthInBytes() + existingSample.lengthInBytes();
            if (bytesFreeAfterAdd < 0) {
                int trim = -bytesFreeAfterAdd / 16;
                newSample.setTrim(trim);
                newSample.reload(halfSpeed.isSelected());
                JOptionPane.showMessageDialog(this,
                        "Trimmed sample to fit.",
                        "Kit full!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            samples[selectedBank][index] = newSample;
            renameSample(index, dropExtension(wavFile).toUpperCase());
            compileKit();
            updateRomView();
            playSample();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            showFileErrorMessage(e);
        }
    }

    @Override
    public void renameSample(String s) {
        renameSample(samplePicker.getSelectedIndex(), s);
        updateRomView();
    }

    private class PadKeyHandler implements KeyEventPostProcessor {
        @Override
        public boolean postProcessKeyEvent(KeyEvent e) {
            // Makes sure we do nothing if a text field has focus.
            if (e.getID() != KeyEvent.KEY_TYPED || e.isConsumed()) {
                return false;
            }
            if (e.getModifiersEx() != 0) {
                return false;
            }
            String playChars = "1234qwerasdfzxc";
            int index = playChars.indexOf(Character.toLowerCase(e.getKeyChar()));
            if (index == -1) {
                return false;
            }
            samplePicker.setSelectedIndex(index);
            playSample();
            return true;
        }
    }
}
