package utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileDialogLauncher {

    public static File load(JFrame parent, String title, String fileExtension) {
        return open(parent, title, fileExtension, FileDialog.LOAD);
    }

    public static File save(JFrame parent, String title, String fileExtension) {
        return open(parent, title, fileExtension, FileDialog.SAVE);
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

        if (!fileName.endsWith("." + fileExtension)) {
            fileName += "." + fileExtension;
        }
        String path = directory + fileName;
        EditorPreferences.setLastPath(fileExtension, path);
        return new File(path);
    }
}