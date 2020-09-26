package songManager;

import net.miginfocom.swing.MigLayout;
import utils.GlobalHolder;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JList;
import java.io.*;
import java.util.prefs.Preferences;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

public class SongManager extends JFrame implements ListSelectionListener {
    LSDSavFile savFile;

    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JButton saveButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList<String> songList = new JList<>( new String[] { " " } );
    JScrollPane songs = new JScrollPane(songList);

    byte[] romImage;
    
    public SongManager(String romPath, String savPath) {
        savFile = new LSDSavFile();

        addLsdSngButton.setEnabled(false);
        addLsdSngButton.setText("Add songs...");
        addLsdSngButton.addActionListener(e -> addLsdSngButton_actionPerformed());
        clearSlotButton.setEnabled(false);
        clearSlotButton.setText("Remove songs");
        clearSlotButton.addActionListener(e -> clearSlotButton_actionPerformed());
        exportLsdSngButton.setEnabled(false);
        exportLsdSngButton.setToolTipText("Export song to .lsf");
        exportLsdSngButton.setText("Export songs...");
        exportLsdSngButton.addActionListener(e -> exportLsdSngButton_actionPerformed());
        songList.addListSelectionListener(this);

        saveButton.setEnabled(false);
        saveButton.setText("Save ROM+SAV...");
        saveButton.addActionListener(e -> saveButton_actionPerformed());

        jRamUsageIndicator.setString("");
        jRamUsageIndicator.setStringPainted(true);

        this.setResizable(false);
        this.setTitle("Song Manager");

        songs.setMinimumSize(new Dimension(0, 180));

        java.awt.Container panel = this.getContentPane();
        MigLayout layout = new MigLayout("wrap", "[]8[]");
        panel.setLayout(layout);
        panel.add(songs, "cell 0 0 1 6, growx, growy");
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

    private String savePath() {
        return GlobalHolder.get(Preferences.class).get("path", System.getProperty("user.dir"));
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
        fileDialog.setDirectory(savePath());
        fileDialog.setFile("*.gb");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null) {
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
        try {
            savFile.loadFromSav(savPath);
            savFile.populateSongList(songList);
            enableAllButtons();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    e.getLocalizedMessage(),
                    ".sav load failed!",
                    JOptionPane.ERROR_MESSAGE);
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
        int[] songs = songList.getSelectedIndices();
        
        for (int song : songs)
            savFile.clearSong(song);
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
        
        int[] songs = songList.getSelectedIndices();

        if (songs.length == 1) {
            FileDialog fileDialog = new FileDialog(this,
                    "Export song to .lsf",
                    FileDialog.SAVE);
            fileDialog.setDirectory(savePath());
            fileDialog.setFile("*.lsf");
            fileDialog.setVisible(true);
            String fileName = fileDialog.getFile();
            if (fileName == null) {
                return;
            }
            String filePath = fileDialog.getDirectory() + fileName;
            if (!filePath.toUpperCase().endsWith(".LSF")) {
                filePath += ".lsf";
            }
            savFile.exportSongToFile(songs[0], filePath, romImage);
        } else if (songs.length > 1) {
            JFileChooser fileChooser = new JFileChooser(savePath());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(
                    "Batch export selected songs to .lsf files");
            int ret_val = fileChooser.showDialog(null, "Choose Directory");
            
            if (JFileChooser.APPROVE_OPTION == ret_val) {
                String directory = fileChooser.getSelectedFile().getAbsolutePath();

                for (int song : songs) {
                    String filename = savFile.getFileName(song).toLowerCase()
                            + "-" + savFile.version(song) + ".lsf";
                    String path = directory + File.separator + filename;
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
                    if (savFile.getBlocksUsed(song) > 0) {
                        savFile.exportSongToFile(song, path, romImage);
                    }
                }
            }
        }
    }

    public void addLsdSngButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Add song(s)...",
                FileDialog.LOAD);
        fileDialog.setDirectory(savePath());
        fileDialog.setFile("*.lsdsng;*.lsf");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);

        File[] files = fileDialog.getFiles();
        if (files.length == 0) {
            return;
        }

        try {
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".lsdsng") ||
                        f.getName().toLowerCase().endsWith(".lsf")) {
                    savFile.addSongFromFile(f.getAbsoluteFile().toString(), romImage);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Unknown file extension: " + f.getName(),
                            "Song add failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getLocalizedMessage(),
                    "Song add failed",
                    JOptionPane.ERROR_MESSAGE);
        }
        savFile.populateSongList(songList);
        updateRamUsageIndicator();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean enable = !songList.isSelectionEmpty();
        clearSlotButton.setEnabled(enable);

        int[] songs = songList.getSelectedIndices();
        if (songs.length == 1) {
            enable = savFile.isValid(songs[0]);
        }
        exportLsdSngButton.setEnabled(enable);
    }
}