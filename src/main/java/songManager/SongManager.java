package songManager;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.io.*;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;
import java.util.prefs.*;

public class SongManager extends JFrame implements ListSelectionListener {
    
    LSDSavFile savFile;
    String latestSavPath = "\\";
    String latestSngPath = "\\";
    
    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JButton saveButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList<String> songList = new JList<>( new String[] { " " } );
    JScrollPane songs = new JScrollPane(songList);
    JLabel workMemLabel = new JLabel();

    byte[] romImage;
    
    Preferences preferences;
    private static final String LATEST_SAV_PATH = "latest_sav_path";
    private static final String LATEST_SNG_PATH = "latest_sng_path";
    private static final long serialVersionUID = 1279298060794170168L;

    public SongManager(String romPath, String savPath) {
        savFile = new LSDSavFile();

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

        saveButton.setEnabled(false);
        saveButton.setText("Save ROM+SAV...");
        saveButton.addActionListener(e -> saveButton_actionPerformed());

        jRamUsageIndicator.setString("");
        jRamUsageIndicator.setStringPainted(true);

        this.setResizable(false);
        this.setTitle("Song Manager");

        workMemLabel.setText("Work memory empty.");
        songs.setMinimumSize(new Dimension(0, 180));

        java.awt.Container panel = this.getContentPane();
        MigLayout layout = new MigLayout("wrap", "[]8[]");
        panel.setLayout(layout);
        panel.add(workMemLabel, "cell 0 0 1 1");
        panel.add(songs, "cell 0 1 1 5, growx, growy");
        panel.add(jRamUsageIndicator, "cell 0 6 1 1, growx");
        panel.add(saveButton, "cell 1 1 1 1, growx");
        panel.add(addLsdSngButton, "cell 1 2 1 1, growx, gaptop 10");
        panel.add(exportLsdSngButton, "cell 1 3 1 1, growx");
        panel.add(clearSlotButton, "cell 1 4 1 1, growx, gaptop 10, aligny top");

        pack();
        loadSav(savPath);
        loadRom(romPath);
        setVisible(true);
    }

    private void loadRom(String romPath) {
        romImage = new byte[0x100000];
        try {
            RandomAccessFile f = new RandomAccessFile(romPath, "r");
            f.readFully(romImage);
            f.close();
        } catch (IOException e) {
            romImage = null;
            JOptionPane.showMessageDialog(this,
                    e.getLocalizedMessage(),
                    "ROM read failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Save .gb file",
                FileDialog.SAVE);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.gb");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.toLowerCase().endsWith(".gb")) {
            return;
        }

        fileName = fileDialog.getDirectory() + fileName;
        if (!fileName.toUpperCase().endsWith(".GB")) {
            fileName += ".gb";
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(romImage);
            fileOutputStream.close();

            savFile.saveAs(fileName.replace(".gb", ".sav"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getLocalizedMessage(),
                    "File save failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSav(String savPath) {
        if (savFile.loadFromSav(savPath)) {
            savFile.populateSongList(songList);
            workMemLabel.setText("Loaded .gb and .sav files.");
            enableAllButtons();
            savePreferences();
        } else {
            workMemLabel.setText(".sav file is not valid 128kB .sav!");
        }
    }

    private void enableAllButtons() {
        saveButton.setEnabled(true);
        addLsdSngButton.setEnabled(true);

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
            savFile.clearSong(slot);
        savFile.populateSongList(songList);
        updateRamUsageIndicator();
    }

    private void updateRamUsageIndicator() {
        jRamUsageIndicator.setMaximum(savFile.totalBlockCount());
        jRamUsageIndicator.setValue(savFile.usedBlockCount());
        jRamUsageIndicator.setString("File mem. used: "
                + savFile.usedBlockCount() + "/" + savFile.totalBlockCount());
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
            savFile.exportSongToFile(slots[0], filePath, romImage);
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
                    String filename = savFile.getFileName(slot).toLowerCase()
                            + "-" + savFile.version(slot) + ".lsdsng";
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
                    if (savFile.getBlocksUsed(slot) > 0) {
                        savFile.exportSongToFile(slot, path, romImage);
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
                try {
                    savFile.addSongFromFile(f.getAbsoluteFile().toString(), romImage);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            e.getLocalizedMessage(),
                            "Song add failed",
                            JOptionPane.ERROR_MESSAGE);
                    success = false;
                }
            }
        }
        savFile.populateSongList(songList);
        latestSngPath = files[0].getAbsoluteFile().getParent();
        updateRamUsageIndicator();
        if (success) {
            savePreferences();
        }
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
            enable = savFile.isValid(slots[0]);
        }
        exportLsdSngButton.setEnabled(enable);
    }
}