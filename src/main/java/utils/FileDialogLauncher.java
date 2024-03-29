package utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

class CustomFileFilter implements java.io.FilenameFilter {
    CustomFileFilter(String[] fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    @Override
    public boolean accept(File dir, String name) {
        for (String fileExtension : fileExtensions) {
            if (name.endsWith(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    String[] fileExtensions;
}

public class FileDialogLauncher {
    public static File load(JFrame parent, String title, String fileExtension) {
        return open(parent, title, new String[] { fileExtension }, FileDialog.LOAD);
    }

    public static File load(JFrame parent, String title, String[] fileExtensions) {
        return open(parent, title, fileExtensions, FileDialog.LOAD);
    }

    public static File save(JFrame parent, String title, String fileExtension) {
        return open(parent, title, new String[] { fileExtension }, FileDialog.SAVE);
    }

    public static File save(JFrame parent, String title, String[] fileExtensions) {
        return open(parent, title, fileExtensions, FileDialog.SAVE);
    }

    private static void setFilenameFilter(FileDialog fileDialog, String[] fileExtensions) {
        if (System.getProperty("os.name").contains("Windows")) {
            // Works on Windows only.
            StringBuilder fileMatch = new StringBuilder("*." + fileExtensions[0]);
            for (int i = 1; i < fileExtensions.length; ++i) {
                fileMatch.append(";*.").append(fileExtensions[i]);
            }
            fileDialog.setFile(fileMatch.toString());
        } else {
            // Does not work on Windows.
            fileDialog.setFilenameFilter(new CustomFileFilter(fileExtensions));
        }
    }

    private static File open(JFrame parent, String title, String[] fileExtensions, int mode) {
        FileDialog fileDialog = new FileDialog(parent, title, mode);
        fileDialog.setDirectory(EditorPreferences.lastDirectory(fileExtensions[0]));
        setFilenameFilter(fileDialog, fileExtensions);
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }

        boolean filenameMatchesExtension = false;
        String selectedExtension = null;
        for (String fileExtension : fileExtensions) {
            if (fileName.toLowerCase().endsWith("." + fileExtension)) {
                filenameMatchesExtension = true;
                selectedExtension = fileExtension;
                break;
            }
        }
        if (!filenameMatchesExtension) {
            selectedExtension = fileExtensions[0];
            fileName += "." + fileExtensions[0];
            if (mode == FileDialog.SAVE &&
                    new File(directory + fileName).exists() &&
                    JOptionPane.showConfirmDialog(parent,
                            fileName + " already exists.\n" +
                                    "Do you want to replace it?",
                            "Confirm Save As",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                return null;
            }
        }
        String path = directory + fileName;
        assert selectedExtension != null;
        EditorPreferences.setLastPath(selectedExtension, path);
        return new File(path);
    }
}
