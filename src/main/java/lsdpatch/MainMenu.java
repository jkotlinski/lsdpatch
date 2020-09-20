package lsdpatch;

import fontEditor.FontEditor;
import kitEditor.KitEditor;
import net.miginfocom.swing.MigLayout;
import utils.JFileChooserFactory;
import utils.RomUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MainMenu extends JFrame {
    JTextField romTextField = new JTextField();
    JTextField savTextField = new JTextField();

    JButton editKitsButton = new JButton("Edit Kits");
    JButton editSongsButton = new JButton("Edit Songs");
    JButton editFontsButton = new JButton("Edit Fonts");
    JButton editPalettesButton = new JButton("Edit Palettes");

    MainMenu() {
        setTitle("LSDPatcher v" + LSDPatcher.getVersion());
        JPanel rootPanel = new JPanel();
        getContentPane().add(rootPanel);
        MigLayout rootLayout = new MigLayout("wrap");
        rootPanel.setLayout(rootLayout);

        rootPanel.add(selectorPanel());

        rootPanel.add(editPanel());
    }

    private JPanel editPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

        editKitsButton.addActionListener(e -> {
            new KitEditor(romTextField.getText()).setLocationRelativeTo(this);
        });
        editKitsButton.setEnabled(false);
        panel.add(editKitsButton);

        editFontsButton.addActionListener(e -> {
            FontEditor fontEditor = new FontEditor();
            fontEditor.setLocationRelativeTo(this);
            byte[] romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];

            try {
                RandomAccessFile romFile = new RandomAccessFile(romTextField.getText(), "r");
                romFile.readFully(romImage);
                romFile.close();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        ioe.getLocalizedMessage(),
                        "Could not read .gb file",
                        JOptionPane.ERROR_MESSAGE);
            }
            fontEditor.setRomImage(romImage);
            fontEditor.setVisible(true);
        });
        editFontsButton.setEnabled(false);
        panel.add(editFontsButton);

        return panel;
    }

    private JPanel selectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 3"));

        panel.add(new JLabel("ROM:"));

        romTextField.setMinimumSize(new Dimension(300, 0));
        romTextField.addActionListener(e -> updateButtonsFromTextFields());
        romTextField.setEditable(false);
        panel.add(romTextField, "growx");

        JButton browseRomButton = new JButton("Browse...");
        browseRomButton.addActionListener(e -> onBrowseRomButtonPress());
        panel.add(browseRomButton);

        panel.add(new JLabel("SAV:"));

        savTextField.setMinimumSize(new Dimension(300, 0));
        savTextField.addActionListener(e -> updateButtonsFromTextFields());
        savTextField.setEditable(false);
        panel.add(savTextField, "growx");

        JButton browseSavButton = new JButton("Browse...");
        browseSavButton.addActionListener(e -> onBrowseSavButtonPress());
        panel.add(browseSavButton);

        return panel;
    }

    private void onBrowseRomButtonPress() {
        FileDialog fileDialog = new FileDialog(this,
                "Select LSDj ROM Image",
                FileDialog.LOAD);
        fileDialog.setDirectory(JFileChooserFactory.baseFolder());
        fileDialog.setFile("*.gb");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return;
        }

        String romPath = fileDialog.getDirectory() + fileName;
        romTextField.setText(romPath);
        String savPath = romPath.replaceFirst(".gb", ".sav");
        savTextField.setText(savPath);
        updateButtonsFromTextFields();
    }

    private void onBrowseSavButtonPress() {
        FileDialog fileDialog = new FileDialog(this,
                "Select LSDj .sav file",
                FileDialog.LOAD);
        fileDialog.setDirectory(JFileChooserFactory.baseFolder());
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return;
        }

        String savPath = fileDialog.getDirectory() + fileName;
        savTextField.setText(savPath);
        updateButtonsFromTextFields();
    }

    void updateButtonsFromTextFields() {
        String romPath = romTextField.getText();
        String savPath = savTextField.getText();

        boolean romPathOk = romPath.endsWith(".gb") && new File(romPath).exists();
        boolean savPathOk = savPath.endsWith(".sav") && new File(savPath).exists();

        romTextField.setBackground(romPathOk ? Color.white : Color.pink);
        savTextField.setBackground(savPathOk ? Color.white : Color.pink);

        editKitsButton.setEnabled(romPathOk);
        editFontsButton.setEnabled(romPathOk);
    }
}
