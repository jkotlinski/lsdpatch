package utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileDialogLauncher {

    public static File load(JFrame parent, String title, String fileExtension) {
        return open(parent, title, fileExtension, FileDialog.LOAD);
    }

    public static File save(JFrame parent, String title, String fileExtension) {
        File f = open(parent, title, fileExtension, FileDialog.SAVE);
        if (f == null) {
            return null;
        }
        String fileName = f.getName();
        if (!fileName.endsWith("." + fileExtension)) {
            fileName += "." + fileExtension;
        }
        return new File(f.getParent(), fileName);
    }

    private static File open(JFrame parent, String title, String fileExtension, int mode) {
        FileDialog fileDialog = new FileDialog(parent, title, mode);
        fileDialog.setDirectory(EditorPreferences.lastDirectory(fileExtension));
        fileDialog.setFile("*." + fileExtension);
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        String path = directory + fileName;
        EditorPreferences.setLastPath(fileExtension, path);
        return new File(path);
    }
}