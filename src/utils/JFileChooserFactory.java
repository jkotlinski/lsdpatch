package utils;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JFileChooserFactory {
    public enum FileType {
        Gb, Lsdfnt, Lsdpal, Kit,
        // Misc
        Wav, Png

    }

    public enum FileOperation {
        Save, Load, MultipleLoad
    }

    public static JFileChooser createChooser(String windowTitle, FileType type, FileOperation operation) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(windowTitle);
        FileFilter filter;
        switch (type) {
            case Gb:
                filter = new FileNameExtensionFilter("Game Boy ROM (*.gb)", "gb");
                break;
            case Lsdfnt:
                filter = new FileNameExtensionFilter("LSDJ Font (*.lsdfnt)", "lsdfnt");
                break;
            case Lsdpal:
                filter = new FileNameExtensionFilter("LSDJ Palette (*.lsdpal)", "lsdpal");
                break;
            case Kit:
                filter = new FileNameExtensionFilter("LSDJ Kit (*.kit)", "kit");
                break;
            case Wav:
                filter = new FileNameExtensionFilter("Wave file (*.wav)", "wav");
                break;
            case Png:
                filter = new FileNameExtensionFilter("PNG / Portable Network Graphics (*.png)", "png");
                break;
            default:
                filter = new FileNameExtensionFilter("Game Boy ROM (*.gb)", "gb");
                break;
        }
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        chooser.setCurrentDirectory(new File(GlobalHolder.get(Preferences.class).get("path", System.getProperty("user.dir"))));

        if (operation == FileOperation.Save)
            chooser.setApproveButtonMnemonic(JFileChooser.SAVE_DIALOG);
        else
            chooser.setApproveButtonMnemonic(JFileChooser.OPEN_DIALOG);

        chooser.setMultiSelectionEnabled(operation == FileOperation.MultipleLoad);

        return chooser;
    }

    public static void recordNewBaseFolder(String newPath) {
        GlobalHolder.get(Preferences.class).put("path", newPath);
    }
}
