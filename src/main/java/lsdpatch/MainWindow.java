// Copyright (C) 2020, Johan Kotlinski

package lsdpatch;

import Document.*;
import fontEditor.FontEditor;
import kitEditor.KitEditor;
import net.miginfocom.swing.MigLayout;
import paletteEditor.PaletteEditor;
import songManager.SongManager;
import utils.EditorPreferences;
import utils.FileDialogLauncher;
import utils.RomUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainWindow extends JFrame implements IDocumentListener, KitEditor.Listener {
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

        upgradeRomButton.addActionListener(e -> openRomUpgradeTool());
        panel.add(upgradeRomButton);

        songManagerButton.addActionListener(e -> openSongManager());
        panel.add(songManagerButton);

        editKitsButton.addActionListener(e ->
                new KitEditor(this, document, this).setLocationRelativeTo(this));
        panel.add(editKitsButton);

        editFontsButton.addActionListener(e -> openFontEditor());
        panel.add(editFontsButton);

        editPalettesButton.addActionListener(e -> openPaletteEditor());
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

        setResizable(false);

        NewVersionChecker.checkGithub(this);
    }

    private void openRomUpgradeTool() {
        RomUpgradeTool romUpgradeTool = new RomUpgradeTool(this, document);
        romUpgradeTool.setLocationRelativeTo(this);
        romUpgradeTool.setVisible(true);
    }

    private void openSongManager() {
        SongManager savManager = new SongManager(this, document);
        savManager.setLocationRelativeTo(this);
        savManager.setVisible(true);
    }

    private void openPaletteEditor() {
        PaletteEditor editor = new PaletteEditor(this, document);
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
    }

    private void openFontEditor() {
        FontEditor fontEditor = new FontEditor(this, document);
        fontEditor.setLocationRelativeTo(this);
        fontEditor.setVisible(true);
    }

    final Document document = new Document();

    private void addSelectors(JPanel panel) {
        romTextField.setMinimumSize(new Dimension(300, 0));
        romTextField.setText(EditorPreferences.lastPath("gb"));
        romTextField.setEditable(false);
        panel.add(romTextField, "span 4, grow x");

        JButton browseRomButton = new JButton("Browse...");
        browseRomButton.addActionListener(e -> onBrowseRomButtonPress());
        panel.add(browseRomButton);

        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> onSave(true));
        panel.add(saveButton, "span 1 4, grow y");

        savTextField.setMinimumSize(new Dimension(300, 0));
        savTextField.setEditable(false);
        savTextField.setText(EditorPreferences.lastPath("sav"));
        panel.add(savTextField, "span 4, grow x");

        JButton browseSavButton = new JButton("Browse...");
        browseSavButton.addActionListener(e -> onBrowseSavButtonPress());
        panel.add(browseSavButton);

        try {
            document.loadRomImage(EditorPreferences.lastPath("gb"));
        } catch (IOException e) {
            resetRomTextField();
        }
        try {
            document.loadSavFile(EditorPreferences.lastPath("sav"));
        } catch (IOException e) {
            resetSavTextField();
        }
        updateButtonsFromTextFields();
    }

    private void resetRomTextField() {
        romTextField.setText("Select LSDj ROM file -->");
    }

    private void resetSavTextField() {
        savTextField.setText("Select LSDj .sav file -->");
    }

    private void onBrowseRomButtonPress() {
        if (document.isDirty() && JOptionPane.showConfirmDialog(null,
                "Load new ROM without saving changes?",
                "Unsaved changes",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
            return;
        }

        File romFile = FileDialogLauncher.load(this,
                "Select LSDj ROM Image",
                new String[]{ "gb", "gbc" });
        if (romFile == null) {
            return;
        }

        String romPath = romFile.getAbsolutePath();
        try {
            document.loadRomImage(romPath);
            romTextField.setText(romPath);
        } catch (IOException e) {
            resetRomTextField();
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    e.getMessage() == null ? "Could not load " + romPath : e.getMessage(),
                    "ROM load failed!",
                    JOptionPane.ERROR_MESSAGE);
        }

        String savPath = romPath
                .replaceFirst(".gbc$", ".sav")
                .replaceFirst(".gb$", ".sav");
        try {
            document.loadSavFile(savPath);
            savTextField.setText(savPath);
            EditorPreferences.setLastPath("sav", savPath);
        } catch (IOException e) {
            resetSavTextField();
            e.printStackTrace();
        }
        updateButtonsFromTextFields();
    }

    private void onBrowseSavButtonPress() {
        if (document.isSavDirty() && JOptionPane.showConfirmDialog(null,
                "Load new .sav without saving changes?",
                "Unsaved changes",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
            return;
        }

        File savFile = FileDialogLauncher.load(this, "Load Save File", "sav");
        if (savFile == null) {
            return;
        }
        try {
            document.loadSavFile(savFile.getAbsolutePath());
            savTextField.setText(savFile.getAbsolutePath());
        } catch (IOException e) {
            resetSavTextField();
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    ".sav load failed!",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        updateButtonsFromTextFields();
    }

    void updateButtonsFromTextFields() {
        byte[] romImage = document.romImage();
        boolean romOk = romImage != null;
        String savPath = savTextField.getText();
        boolean savPathOk = savPath.endsWith(".sav") && new File(savPath).exists();
        boolean foundPalettes = romOk && RomUtilities.validatePaletteData(romImage);

        romTextField.setBackground(romOk ? Color.white : Color.pink);
        savTextField.setBackground(savPathOk ? Color.white : Color.pink);

        editKitsButton.setEnabled(romOk);
        editFontsButton.setEnabled(romOk && foundPalettes);
        editPalettesButton.setEnabled(romOk && foundPalettes);
        upgradeRomButton.setEnabled(romOk && foundPalettes);
        songManagerButton.setEnabled(savPathOk && romOk);
    }

    public void onDocumentDirty(boolean dirty) {
        updateTitle();
        upgradeRomButton.setEnabled(!dirty);
        saveButton.setEnabled(dirty);
    }

    private void updateTitle() {
        String title = "LSDPatcher v" + NewVersionChecker.getCurrentVersion();
        if (document.romImage() != null) {
            title = title + " - " + document.romFile().getName();
            if (document.isDirty()) {
                title = title + '*';
            }
        }
        setTitle(title);
    }

    private void onSave(boolean saveSavFile) {
        File f = FileDialogLauncher.save(this,
                "Save ROM Image",
                new String[]{ "gb", "gbc" });
        if (f == null) {
            return;
        }
        String romPath = f.getAbsolutePath();

        try (FileOutputStream fileOutputStream = new FileOutputStream(romPath)) {
            byte[] romImage = document.romImage();
            RomUtilities.fixChecksum(romImage);
            fileOutputStream.write(romImage);
            fileOutputStream.close();
            if (document.savFile() != null && saveSavFile) {
                String savPath = romPath
                        .replace(".gbc", ".sav")
                        .replace(".gb", ".sav");
                document.savFile().saveAs(savPath);
                savTextField.setText(savPath);
                document.loadSavFile(savPath);
                document.clearSavDirty();
                EditorPreferences.setLastPath("sav", savPath);
            }
            romTextField.setText(romPath);
            document.setRomFile(new File(romPath));
            document.clearRomDirty();
            EditorPreferences.setLastPath("gb", romPath);
            saveButton.setEnabled(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "File save failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void saveRom() {
        onSave(false);
    }
}
