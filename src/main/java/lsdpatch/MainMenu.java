// Copyright (C) 2020, Johan Kotlinski

package lsdpatch;

import fontEditor.FontEditor;
import kitEditor.KitEditor;
import net.miginfocom.swing.MigLayout;
import paletteEditor.PaletteEditor;
import songManager.SongManager;
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

    JButton upgradeRomButton = new JButton("Upgrade ROM");
    JButton songManagerButton = new JButton("Songs");
    JButton editKitsButton = new JButton("Sample Kits");
    JButton editFontsButton = new JButton("Fonts");
    JButton editPalettesButton = new JButton("Palettes");

    MainMenu() {
        setTitle("LSDPatcher v" + LSDPatcher.getVersion());
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        MigLayout rootLayout = new MigLayout("wrap 5");
        panel.setLayout(rootLayout);

        addSelectors(panel);

        panel.add(new JSeparator(), "span 5");

        upgradeRomButton.setEnabled(false);
        upgradeRomButton.addActionListener(e -> openRomUpgradeTool());
        panel.add(upgradeRomButton);

        songManagerButton.setEnabled(false);
        songManagerButton.addActionListener(e -> openSongManager());
        panel.add(songManagerButton);

        editKitsButton.addActionListener(e -> {
            new KitEditor(romTextField.getText()).setLocationRelativeTo(this);
        });
        editKitsButton.setEnabled(false);
        panel.add(editKitsButton);
        editFontsButton.addActionListener(e -> openKitEditor());
        editFontsButton.setEnabled(false);
        panel.add(editFontsButton);

        editPalettesButton.addActionListener(e -> openPaletteEditor());
        editPalettesButton.setEnabled(false);
        panel.add(editPalettesButton, "grow x");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void openRomUpgradeTool() {
        if (!loadRomImage()) {
            return;
        }
        try {
            RomUpgradeTool romUpgradeTool = new RomUpgradeTool(romImage);
            romUpgradeTool.setLocationRelativeTo(this);
            romUpgradeTool.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    e.getLocalizedMessage(),
                    "Could not download new version information!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSongManager() {
        SongManager savManager = new SongManager(romTextField.getText(), savTextField.getText());
        savManager.setLocationRelativeTo(this);
        savManager.setVisible(true);
    }

    private void openPaletteEditor() {
        PaletteEditor editor = new PaletteEditor();
        editor.setLocationRelativeTo(this);
        editor.setRomImage(romImage);
        editor.setVisible(true);
    }

    private void openKitEditor() {
        FontEditor fontEditor = new FontEditor();
        fontEditor.setLocationRelativeTo(this);
        fontEditor.setRomImage(romImage);
        fontEditor.setVisible(true);
    }

    byte[] romImage;

    boolean loadRomImage() {
        romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
        try {
            RandomAccessFile romFile = new RandomAccessFile(romTextField.getText(), "r");
            romFile.readFully(romImage);
            romFile.close();
        } catch (IOException ioe)
        {
            JOptionPane.showMessageDialog(this,
                    ioe.getLocalizedMessage(),
                    "Could not read .gb file",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void addSelectors(JPanel panel) {
        romTextField.setMinimumSize(new Dimension(300, 0));
        romTextField.addActionListener(e -> updateButtonsFromTextFields());
        romTextField.setText("Select LSDj ROM file --->");
        romTextField.setEditable(false);
        panel.add(romTextField, "span 4, grow x");

        JButton browseRomButton = new JButton("Browse...");
        browseRomButton.addActionListener(e -> onBrowseRomButtonPress());
        panel.add(browseRomButton);

        savTextField.setMinimumSize(new Dimension(300, 0));
        savTextField.addActionListener(e -> updateButtonsFromTextFields());
        savTextField.setEditable(false);
        savTextField.setText("Select LSDj save file --->");
        panel.add(savTextField, "span 4, grow x");

        JButton browseSavButton = new JButton("Browse...");
        browseSavButton.addActionListener(e -> onBrowseSavButtonPress());
        panel.add(browseSavButton);
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

        boolean romPathOk = romPath.endsWith(".gb") && new File(romPath).exists() && loadRomImage();
        boolean savPathOk = savPath.endsWith(".sav") && new File(savPath).exists();

        romTextField.setBackground(romPathOk ? Color.white : Color.pink);
        savTextField.setBackground(savPathOk ? Color.white : Color.pink);

        editKitsButton.setEnabled(romPathOk);
        editFontsButton.setEnabled(romPathOk);
        editPalettesButton.setEnabled(romPathOk);
        upgradeRomButton.setEnabled(romPathOk);
        songManagerButton.setEnabled(savPathOk && romPathOk);
    }
}
