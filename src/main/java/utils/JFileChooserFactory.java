package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.text.MessageFormat;

public class JFileChooserFactory {
    public enum FileType {
        Gb, Lsdfnt, Lsdpal, Kit,
        // Misc
        Wav, Png;

        public String getExtension() {
            return name().toLowerCase();
        }
    }

    public enum FileOperation {
        Save, Load, MultipleLoad
    }

    private static boolean verifyAndValidateOverwrite(JFileChooser parent, FileType type)
    {
        String suffix = "." + type.getExtension();
        File f = parent.getSelectedFile();
        if(!f.getAbsolutePath().toLowerCase().endsWith(suffix)){
            f = new File(parent.getSelectedFile() + suffix);
            parent.setSelectedFile(f);
        }
        if ( f.exists() ) {
            String msg = "The file \"{0}\" already exists!\nDo you want to overwrite it?";
            msg = MessageFormat.format(msg, f.getName());
            String title = parent.getDialogTitle();
            int option = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return option == JOptionPane.OK_OPTION;
        }
        return true;
    }

    public static JFileChooser createChooser(String windowTitle, FileType type, FileOperation operation) {
        JFileChooser chooser;
        if (operation == FileOperation.Save)
        {
            chooser = new JFileChooser()
            {
                @Override
                public void approveSelection() {
                    if (verifyAndValidateOverwrite(this, type))
                        super.approveSelection();
                }
            };
            chooser.setApproveButtonMnemonic(JFileChooser.SAVE_DIALOG);
        }
        else
        {
            chooser = new JFileChooser();
            chooser.setApproveButtonMnemonic(JFileChooser.OPEN_DIALOG);
        }
        chooser.setDialogTitle(windowTitle);
        FileNameExtensionFilter filter;
        switch (type) {
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
            case Gb:
            default:
                filter = new FileNameExtensionFilter("Game Boy ROM (*.gb)", "gb");
                break;
        }
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        chooser.setCurrentDirectory(new File(EditorPreferences.lastPath(filter.getExtensions()[0])));

        chooser.setMultiSelectionEnabled(operation == FileOperation.MultipleLoad);

        return chooser;
    }
}
