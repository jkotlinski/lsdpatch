package songManager;

import Document.Document;
import Document.LSDSavFile;
import net.miginfocom.swing.MigLayout;
import utils.EditorPreferences;
import utils.FileDialogLauncher;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

public class SongManager extends JFrame implements ListSelectionListener {
    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList<String> songList = new JList<>( new String[] { " " } );
    JScrollPane songs = new JScrollPane(songList);

    byte[] romImage;

    LSDSavFile savFile;
    
    public SongManager(JFrame parent, Document document) {
        parent.setEnabled(false);

        romImage = document.romImage();
        savFile = document.savFile();

        addLsdSngButton.setText("Add songs...");
        addLsdSngButton.addActionListener(e -> addLsdSngButton_actionPerformed());
        clearSlotButton.setEnabled(false);
        clearSlotButton.setText("Remove songs");
        clearSlotButton.addActionListener(e -> clearSlotButton_actionPerformed());
        exportLsdSngButton.setEnabled(false);
        exportLsdSngButton.setToolTipText("Export song to .lsdprj");
        exportLsdSngButton.setText("Export songs...");
        exportLsdSngButton.addActionListener(e -> exportLsdSngButton_actionPerformed());
        songList.addListSelectionListener(this);

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
        panel.add(addLsdSngButton, "cell 1 0 1 1, growx");
        panel.add(exportLsdSngButton, "cell 1 1 1 1, growx");
        panel.add(clearSlotButton, "cell 1 2 1 1, growx, gaptop 10, aligny top");

        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                document.setSavFile(savFile);
                document.setRomImage(romImage);
                parent.setEnabled(true);
            }
        });

        savFile.populateSongList(songList);
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
            File f = FileDialogLauncher.save(this, "Export Song", "lsdprj");
            if (f == null) {
                return;
            }
            savFile.exportSongToFile(songs[0], f.getAbsolutePath(), romImage);
        } else if (songs.length > 1) {
            JFileChooser fileChooser = new JFileChooser(EditorPreferences.lastDirectory("lsdprj"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(
                    "Batch export selected songs to .lsdprj files");
            int ret_val = fileChooser.showDialog(null, "Choose Directory");
            
            if (JFileChooser.APPROVE_OPTION == ret_val) {
                String directory = fileChooser.getSelectedFile().getAbsolutePath();

                for (int song : songs) {
                    String filename = savFile.getFileName(song).toLowerCase()
                            + "-" + savFile.version(song) + ".lsdprj";
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
                "Load Songs",
                FileDialog.LOAD);
        fileDialog.setDirectory(EditorPreferences.lastDirectory("lsdprj"));
        fileDialog.setFile("*.lsdsng;*.lsdprj");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);

        File[] files = fileDialog.getFiles();
        if (files.length == 0) {
            return;
        }

        try {
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".lsdsng") ||
                        f.getName().toLowerCase().endsWith(".lsdprj")) {
                    savFile.addSongFromFile(f.getAbsoluteFile().toString(), romImage);
                    EditorPreferences.setLastPath("lsdprj", f.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Unknown file extension: " + f.getName(),
                            "Song add failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
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
