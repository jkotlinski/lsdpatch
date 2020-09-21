package songManager;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.io.File;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;
import java.util.prefs.*;

public class SongManager extends JFrame implements ListSelectionListener {
    
    LSDSavFile file;
    String latestSavPath = "\\";
    String latestSngPath = "\\";
    
    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JButton openSavButton = new JButton();
    JButton saveButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList<String> songList = new JList<>( new String[] { " " } );
    JScrollPane songs = new JScrollPane(songList);
    JButton importV2SavButton = new JButton();
    JButton exportV2SavButton = new JButton();
    JLabel workMemLabel = new JLabel();
    
    Preferences preferences;
    private static final String LATEST_SAV_PATH = "latest_sav_path";
    private static final String LATEST_SNG_PATH = "latest_sng_path";
    private static final long serialVersionUID = 1279298060794170168L;

    public SongManager(String savPath) {
        file = new LSDSavFile();

        preferences = Preferences.userNodeForPackage(SongManager.class);
        latestSavPath = preferences.get(LATEST_SAV_PATH, latestSavPath);
        latestSngPath = preferences.get(LATEST_SNG_PATH, latestSngPath);

        addLsdSngButton.setEnabled(false);
        addLsdSngButton.setToolTipText(
                "Add compressed .lsdsng to file memory");
        addLsdSngButton.setText("Add .lsdsng...");
        addLsdSngButton.addActionListener(e -> addLsdSngButton_actionPerformed());
        clearSlotButton.setEnabled(false);
        clearSlotButton.setToolTipText("Clear file memory slot");
        clearSlotButton.setText("Clear Slot");
        clearSlotButton.addActionListener(e -> clearSlotButton_actionPerformed());
        exportLsdSngButton.setEnabled(false);
        exportLsdSngButton.setToolTipText("Export compressed .lsdsng from file memory");
        exportLsdSngButton.setText("Export .lsdsng...");
        exportLsdSngButton.addActionListener(e -> exportLsdSngButton_actionPerformed());
        songList.addListSelectionListener(this);

        openSavButton.setText("Open V3+ .SAV...");
        openSavButton.addActionListener(e -> openSavButton_actionPerformed());

        saveButton.setEnabled(false);
        saveButton.setText("Save V3+ .SAV as...");
        saveButton.addActionListener(e -> saveButton_actionPerformed());

        jRamUsageIndicator.setString("");
        jRamUsageIndicator.setStringPainted(true);

        this.setResizable(false);
        this.setTitle("Song Manager");

        importV2SavButton.setEnabled(false);
        importV2SavButton.setToolTipText(
                "Import 32 kByte V2 .SAV file to work memory "
                        + "(will overwrite what's already there!!)");
        importV2SavButton.setActionCommand("Import V2 SAV...");
        importV2SavButton.setText("Import V2 .SAV...");
        importV2SavButton.addActionListener(e -> importV2SavButton_actionPerformed());
        exportV2SavButton.setEnabled(false);
        exportV2SavButton.setToolTipText(
                "Export work memory to 32 kByte V2 .SAV");
        exportV2SavButton.setActionCommand("Export to V2 SAV...");
        exportV2SavButton.setText("Export V2 .SAV...");
        exportV2SavButton.addActionListener(e -> exportV2SavButton_actionPerformed());
        workMemLabel.setText("Work memory empty.");

        java.awt.Container panel = this.getContentPane();
        MigLayout layout = new MigLayout("wrap", "[]8[]");
        panel.setLayout(layout);
        panel.add(workMemLabel, "cell 0 0 1 1");
        panel.add(songs, "cell 0 1 1 8, growx, growy");
        panel.add(jRamUsageIndicator, "cell 0 9 1 1, growx");
        panel.add(openSavButton, "cell 1 0 1 1, growx");
        panel.add(saveButton, "cell 1 1 1 1, growx");
        panel.add(addLsdSngButton, "cell 1 2 1 1, growx, gaptop 10");
        panel.add(exportLsdSngButton, "cell 1 3 1 1, growx");
        panel.add(importV2SavButton, "cell 1 4 1 1, growx, gaptop 10");
        panel.add(exportV2SavButton, "cell 1 5 1 1, growx");
        panel.add(clearSlotButton, "cell 1 6 1 1, growx, gaptop 10");

        pack();
        loadSav(savPath);
        setVisible(true);
    }

    private void saveButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Save 128kByte V3+ .sav file",
                FileDialog.SAVE);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sav")) {
            return;
        }

        fileName = fileDialog.getDirectory() + fileName;
        if (!fileName.toUpperCase().endsWith(".SAV")) {
            fileName += ".sav";
        }
        file.saveAs(fileName);
    }

    public void openSavButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Open 128kByte V3+ .sav",
                FileDialog.LOAD);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sav")) {
            return;
        }

        latestSavPath = fileDialog.getDirectory();
        if (latestSngPath.equals("\\")) {
            latestSngPath = latestSavPath;
        }
        loadSav(latestSavPath + fileName);
    }

    private void loadSav(String savPath) {
        if (file.loadFromSav(savPath)) {
            file.populateSlotList(songList);
            workMemLabel.setText("Loaded work+file memory.");
            enableAllButtons();
            savePreferences();
        } else {
            workMemLabel.setText("File is not valid 128kB .SAV!");
        }
    }

    private void enableAllButtons() {
        exportV2SavButton.setEnabled(true);
        openSavButton.setEnabled(true);
        saveButton.setEnabled(true);
        addLsdSngButton.setEnabled(true);
        importV2SavButton.setEnabled(true);

        updateRamUsageIndicator();
    }

    public void clearSlotButton_actionPerformed() {

        if (songList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a song!",
                    "No song selected!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] slots = songList.getSelectedIndices();
        
        for (int slot : slots)
            file.clearSlot(slot);
        file.populateSlotList(songList);
        updateRamUsageIndicator();
    }

    private void updateRamUsageIndicator() {
        jRamUsageIndicator.setMaximum(file.totalBlockCount());
        jRamUsageIndicator.setValue(file.usedBlockCount());
        jRamUsageIndicator.setString("File mem. used: "
                + file.usedBlockCount() + "/" + file.totalBlockCount());
    }

    public void exportLsdSngButton_actionPerformed() {
        if (songList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a song!",
                    "No song selected!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int[] slots = songList.getSelectedIndices();

        if (slots.length == 1) {
            FileDialog fileDialog = new FileDialog(this,
                    "Export selected slot to .lsdsng",
                    FileDialog.SAVE);
            fileDialog.setDirectory(latestSngPath);
            fileDialog.setFile("*.lsdsng");
            fileDialog.setVisible(true);
            String fileName = fileDialog.getFile();
            if (fileName == null || !fileName.toLowerCase().endsWith(".lsdsng")) {
                return;
            }

            latestSngPath = fileDialog.getDirectory();
            String filePath = latestSngPath + fileName;
            if (!filePath.toUpperCase().endsWith(".LSDSNG")) {
                filePath += ".lsdsng";
            }
            file.exportSongToFile(slots[0], filePath);
            savePreferences();
        } else if (slots.length > 1) {
            JFileChooser fileChooser = new JFileChooser(latestSngPath);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(
                    "Batch export selected slots to compressed .lsdsng files");
            int ret_val = fileChooser.showDialog(null, "Choose Directory");
            
            if (JFileChooser.APPROVE_OPTION == ret_val) {
                latestSngPath = fileChooser.getSelectedFile()
                        .getAbsolutePath();
                savePreferences();

                for (int slot : slots) {
                    String filename = file.getFileName(slot).toLowerCase()
                            + "-" + file.version(slot) + ".lsdsng";
                    String path = latestSngPath + File.separator + filename;
                    String[] options = { "Yes", "No", "Cancel" };
                    File f = new File(path);
                    if (f.exists()) {
                        int overWrite = JOptionPane.showOptionDialog(
                                this, "File \"" 
                                + filename
                                + "\" already exists.\n"
                                + "Overwrite existing file?", "Warning",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, options,
                                options[1]);

                        if (overWrite == JOptionPane.YES_OPTION) {
                            boolean deleted;
                            try {
                                deleted = f.delete();
                            } catch (Exception fileInUse) {
                                deleted = false;
                            }
                            if (!deleted) {
                                JOptionPane.showMessageDialog(this,
                                        "Could not delete file.");
                                continue;
                            }
                        } else if (overWrite == JOptionPane.NO_OPTION) {
                            continue;
                        } else if (overWrite == JOptionPane.CANCEL_OPTION)
                            return;
                    }
                    if (file.getBlocksUsed(slot) > 0) {
                        file.exportSongToFile(slot, path);
                    }
                }
            }
        }
    }

    public void addLsdSngButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Add .lsdsng to file memory",
                FileDialog.LOAD);
        fileDialog.setDirectory(latestSngPath);
        fileDialog.setFile("*.lsdsng");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);

        File[] files = fileDialog.getFiles();
        if (files.length == 0) {
            return;
        }

        boolean success = true;
        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(".lsdsng")) {
                success &= file.addSongFromFile(f.getAbsoluteFile().toString());
                file.populateSlotList(songList);
            }
        }
        latestSngPath = files[0].getAbsoluteFile().getParent();
        updateRamUsageIndicator();
        if (success) {
            savePreferences();
        }
    }

    public void importV2SavButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Import 32kByte .sav file to work memory",
                FileDialog.LOAD);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sav")) {
            return;
        }

        file.import32KbSavToWorkRam(fileDialog.getDirectory() + fileName);
        workMemLabel.setText("Work memory updated.");
    }

    public void exportV2SavButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
            "Export work memory to 32kByte v2 .sav file",
            FileDialog.SAVE);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sav")) {
            return;
        }
        String filePath = fileDialog.getDirectory() + fileName;
        if (!filePath.toUpperCase().endsWith(".SAV")) {
            filePath += ".sav";
        }
        file.saveWorkMemoryAs(filePath);
    }

    public void savePreferences() {
        preferences.put(LATEST_SAV_PATH, latestSavPath);
        preferences.put(LATEST_SNG_PATH, latestSngPath);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean enable = !songList.isSelectionEmpty();
        clearSlotButton.setEnabled(enable);

        int[] slots = songList.getSelectedIndices();
        if (slots.length == 1) {
            enable = file.isValid(slots[0]);
        }
        exportLsdSngButton.setEnabled(enable);
    }
}