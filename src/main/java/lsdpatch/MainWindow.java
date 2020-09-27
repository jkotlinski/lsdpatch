// Copyright (C) 2020, Johan Kotlinski

package lsdpatch;

import Document.*;
import fontEditor.FontEditor;
import kitEditor.KitEditor;
import net.miginfocom.swing.MigLayout;
import paletteEditor.PaletteEditor;
import songManager.SongManager;
import utils.JFileChooserFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainWindow extends JFrame implements IDocumentListener {
    JTextField romTextField = new JTextField();
    JTextField savTextField = new JTextField();

    JButton upgradeRomButton = new JButton("Upgrade ROM");
    JButton songManagerButton = new JButton("Songs");
    JButton editKitsButton = new JButton("Sample Kits");
    JButton editFontsButton = new JButton("Fonts");
    JButton editPalettesButton = new JButton("Palettes");
    JButton saveButton = new JButton("Save...");

    MainWindow() {
        document.subscribe(this);

        updateTitle();
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        MigLayout rootLayout = new MigLayout("wrap 6");
        panel.setLayout(rootLayout);

        addSelectors(panel);

        panel.add(new JSeparator(), "span 5");

        upgradeRomButton.setEnabled(false);
        upgradeRomButton.addActionListener(e -> openRomUpgradeTool());
        panel.add(upgradeRomButton);

        songManagerButton.setEnabled(false);
        songManagerButton.addActionListener(e -> openSongManager());
        panel.add(songManagerButton);

        editKitsButton.addActionListener(e -> new KitEditor(document).setLocationRelativeTo(this));
        editKitsButton.setEnabled(false);
        panel.add(editKitsButton);
        editFontsButton.addActionListener(e -> openKitEditor());
        editFontsButton.setEnabled(false);
        panel.add(editFontsButton);

        editPalettesButton.addActionListener(e -> openPaletteEditor());
        editPalettesButton.setEnabled(false);
        panel.add(editPalettesButton, "grow x");

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!document.isDirty() || JOptionPane.showConfirmDialog(null,
                        "Quit without saving changes?",
                        "Unsaved changes",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                    setDefaultCloseOperation(EXIT_ON_CLOSE);
                }
                super.windowClosing(e);
            }
        });
    }

    private void openRomUpgradeTool() {
        RomUpgradeTool romUpgradeTool = new RomUpgradeTool(document);
        romUpgradeTool.setLocationRelativeTo(this);
        romUpgradeTool.setVisible(true);
    }

    private void openSongManager() {
        SongManager savManager = new SongManager(romTextField.getText(), savTextField.getText());
        savManager.setLocationRelativeTo(this);
        savManager.setVisible(true);
    }

    private void openPaletteEditor() {
        PaletteEditor editor = new PaletteEditor(document);
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
    }

    private void openKitEditor() {
        FontEditor fontEditor = new FontEditor(document);
        fontEditor.setLocationRelativeTo(this);
        fontEditor.setVisible(true);
    }

    final Document document = new Document();

    private void addSelectors(JPanel panel) {
        romTextField.setMinimumSize(new Dimension(300, 0));
        romTextField.setText("Select LSDj ROM file --->");
        romTextField.setEditable(false);
        panel.add(romTextField, "span 4, grow x");

        JButton browseRomButton = new JButton("Browse...");
        browseRomButton.addActionListener(e -> onBrowseRomButtonPress());
        panel.add(browseRomButton);

        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> onSave());
        panel.add(saveButton, "span 1 4, grow y");

        savTextField.setMinimumSize(new Dimension(300, 0));
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
        document.loadRomImage(romPath);
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
        boolean romOk = document.romImage() != null;
        String savPath = savTextField.getText();
        boolean savPathOk = savPath.endsWith(".sav") && new File(savPath).exists();

        romTextField.setBackground(romOk ? Color.white : Color.pink);
        savTextField.setBackground(savPathOk ? Color.white : Color.pink);

        editKitsButton.setEnabled(romOk);
        editFontsButton.setEnabled(romOk);
        editPalettesButton.setEnabled(romOk);
        upgradeRomButton.setEnabled(romOk);
        songManagerButton.setEnabled(savPathOk && romOk);
    }

    public void onRomDirty(boolean dirty) {
        updateTitle();
        if (dirty) {
            saveButton.setEnabled(true);
        }
    }

    public void onSavDirty(boolean dirty) {
        updateTitle();
        if (dirty) {
            saveButton.setEnabled(true);
        }
    }

    private void updateTitle() {
        String title = "LSDPatcher v" + LSDPatcher.getVersion();
        if (document.romImage() != null) {
            title = title + " - " + document.romFile().getName();
            if (document.isRomDirty()) {
                title = title + '*';
            }
        }
        setTitle(title);
    }

    private void onSave() {
        // TODO
    }

}
