// Copyright (C) 2020, Johan Kotlinski

package lsdpatch;

import Document.Document;
import net.miginfocom.swing.MigLayout;
import structures.LSDJFont;
import utils.RomUtilities;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

public class RomUpgradeTool extends JFrame {
    final String changeLogPath = "https://www.littlesounddj.com/lsd/latest/CHANGELOG.txt";
    final String licensePath = "https://www.littlesounddj.com/lsd/latest/rom_images/LICENSE.txt";
    final String developPath = "https://www.littlesounddj.com/lsd/latest/rom_images/develop/";
    final String stablePath = "https://www.littlesounddj.com/lsd/latest/rom_images/stable/";
    final String arduinoBoyPath = "https://www.littlesounddj.com/lsd/latest/rom_images/arduinoboy/";

    private final File localRomFile;
    private byte[] localRomImage;
    private final byte[] remoteRomImage;
    private final Document document;

    RomUpgradeTool(JFrame parent, Document document) {
        parent.setEnabled(false);

        this.document = document;
        localRomFile = document.romFile();
        localRomImage = document.romImage();
        remoteRomImage = new byte[localRomImage.length];

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new MigLayout("wrap"));

        panel.add(new JLabel("Upgrade ROM to latest:"));
        JButton upgradeStableButton = new JButton("Stable version (recommended!)");
        JButton upgradeDevelopButton = new JButton("Development version (experimental!)");
        JButton upgradeArduinoBoyButton = new JButton("ArduinoBoy version");
        JButton upgradeFromFileButton = new JButton("Select ROM file...");
        JButton viewChangeLogButton = new JButton("View Changelog");
        JButton viewLicenseButton = new JButton("View License Information");
        panel.add(upgradeStableButton, "growx");
        panel.add(upgradeDevelopButton, "growx");
        panel.add(upgradeArduinoBoyButton, "growx");
        panel.add(upgradeFromFileButton, "growx");
        panel.add(viewChangeLogButton, "growx, gaptop 10");
        panel.add(viewLicenseButton, "growx");
        pack();

        upgradeStableButton.addActionListener(e -> upgrade(stablePath));
        upgradeDevelopButton.addActionListener(e -> upgrade(developPath));
        upgradeArduinoBoyButton.addActionListener(e -> upgrade(arduinoBoyPath));
        upgradeFromFileButton.addActionListener(e -> upgradeFromSelectedFile());
        viewChangeLogButton.addActionListener(e -> WwwUtil.openInBrowser(changeLogPath));
        viewLicenseButton.addActionListener(e -> WwwUtil.openInBrowser(licensePath));

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                parent.setEnabled(true);
            }
        });
    }

    private boolean versionCompare(String localVersion, String remoteVersion) {
        assert(remoteVersion.startsWith("lsdj"));
        remoteVersion = remoteVersion.substring(4, 9).replace('_', '.');
        return remoteVersion.compareTo(localVersion) > 0;
    }

    private void upgrade(String basePath) {
        try {
            String localVersion = localVersion();
            String remoteVersion = fetchLatestRemoteVersion(basePath);
            if (localVersion == null || remoteVersion == null) {
                JOptionPane.showMessageDialog(null,
                        "Version information not found!",
                        "Update failed!",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!versionCompare(localVersion, remoteVersion)) {
                JOptionPane.showMessageDialog(this,
                        localRomFile.getName() + " is already updated.",
                        "No updates found!",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int reply = JOptionPane.showConfirmDialog(this,
                    "Current ROM version: " + localVersion() + '\n' +
                            "Upgrade to " + remoteVersion + '?',
                    "Upgrade?",
                    JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                return;
            }
            ZipInputStream zipInputStream = new ZipInputStream(new URL(basePath + remoteVersion).openStream());
            zipInputStream.getNextEntry();
            int dstIndex = 0;
            while (dstIndex != remoteRomImage.length) {
                dstIndex += zipInputStream.read(remoteRomImage, dstIndex, remoteRomImage.length - dstIndex);
            }
            importAll();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "Fetching new version failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String localVersion() {
        byte[] romImage = localRomImage;
        for (int i = 0; i < romImage.length; ++i) {
            if (romImage[i] == 'V' && romImage[i + 2] == '.' && romImage[i + 4] == '.') {
                String s = "";
                s += (char)romImage[i + 1];
                s += (char)romImage[i + 2];
                s += (char)romImage[i + 3];
                s += (char)romImage[i + 4];
                s += (char)romImage[i + 5];
                return s;
            }
        }
        return null;
    }

    private String fetchLatestRemoteVersion(String basePath) throws IOException {
        String page = WwwUtil.fetchWwwPage(new URL(basePath));
        Pattern p = Pattern.compile("lsdj\\d_\\d_[0-9A-Z][-a-zA-Z]*\\.zip");
        Matcher m = p.matcher(page);
        if (m.find()) {
            MatchResult matchResult = m.toMatchResult();
            return matchResult.group();
        } else {
            return null;
        }
    }

    private void importAll() {
        if (importKits() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Kit copy error.",
                    "Kit import result.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!importFonts()) {
            JOptionPane.showMessageDialog(this,
                    "Font copy error.",
                    "Font import result.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!importPalettes()) {
            JOptionPane.showMessageDialog(this,
                    "Palette copy error.",
                    "Palette import result.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        document.setRomImage(remoteRomImage);
        localRomImage = remoteRomImage;

        JOptionPane.showMessageDialog(this,
                "Upgraded to " + localVersion() + " successfully!",
                "ROM upgrade OK!",
                JOptionPane.INFORMATION_MESSAGE);

        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private boolean importPalettes() {
        boolean isOk = false;
        RandomAccessFile otherOpenRom = null;
        try {
            byte[] otherRomImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            otherOpenRom = new RandomAccessFile(localRomFile, "r");
            byte[] romImage = remoteRomImage;

            otherOpenRom.readFully(otherRomImage);
            otherOpenRom.close();

            if (!RomUtilities.validatePaletteData(remoteRomImage)) {
                throw new Exception("Could not read palette data from remote ROM image!");
            }
            if (!RomUtilities.validatePaletteData(otherRomImage)) {
                throw new Exception("Could not read palette data from local ROM image!");
            }

            int ownPaletteOffset = RomUtilities.findPaletteOffset(romImage);
            int ownPaletteNameOffset = RomUtilities.findPaletteNameOffset(romImage);

            int otherPaletteOffset = RomUtilities.findPaletteOffset(otherRomImage);
            int otherPaletteNameOffset = RomUtilities.findPaletteNameOffset(otherRomImage);

            if (RomUtilities.getNumberOfPalettes(otherRomImage) > RomUtilities.getNumberOfPalettes(romImage))
            {
                throw new Exception("Current file doesn't have enough palette slots to get the palettes imported to.");
            }

            System.arraycopy(otherRomImage, otherPaletteOffset, romImage, ownPaletteOffset, RomUtilities.PALETTE_SIZE * RomUtilities.getNumberOfPalettes(otherRomImage));
            System.arraycopy(otherRomImage, otherPaletteNameOffset, romImage, ownPaletteNameOffset, RomUtilities.PALETTE_NAME_SIZE * RomUtilities.getNumberOfPalettes(otherRomImage));

            isOk = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (otherOpenRom != null) {
                try {
                    otherOpenRom.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "File error (wth)",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return isOk;
    }

    private boolean importFonts() {
        boolean isOk = false;
        RandomAccessFile otherOpenRom = null;
        try {
            byte[] otherRomImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            otherOpenRom = new RandomAccessFile(localRomFile, "r");
            byte[] romImage = remoteRomImage;

            otherOpenRom.readFully(otherRomImage);
            otherOpenRom.close();

            int ownFontOffset = RomUtilities.findFontOffset(romImage);
            int otherFontOffset = RomUtilities.findFontOffset(otherRomImage);

            System.arraycopy(otherRomImage, otherFontOffset, romImage, ownFontOffset, LSDJFont.FONT_SIZE * LSDJFont.FONT_COUNT);

            int ownGfxOffset = RomUtilities.findGfxFontOffset(romImage);
            int otherGfxOffset = RomUtilities.findGfxFontOffset(otherRomImage);
            System.arraycopy(otherRomImage, otherGfxOffset, romImage, ownGfxOffset, LSDJFont.GFX_SIZE);

            for (int i = 0; i < LSDJFont.FONT_COUNT; ++i) {
                RomUtilities.setFontName(romImage, i, RomUtilities.getFontName(otherRomImage, i));
            }

            isOk = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (otherOpenRom != null) {
                try {
                    otherOpenRom.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "File error (wth)",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return isOk;
    }

    private boolean isKitBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = remoteRomImage[l_offset++];
        byte l_char_2 = remoteRomImage[l_offset];
        return (l_char_1 == 0x60 && l_char_2 == 0x40);
    }

    private boolean isEmptyKitBank(int a_bank) {
        int l_offset = (a_bank) * RomUtilities.BANK_SIZE;
        byte l_char_1 = remoteRomImage[l_offset++];
        byte l_char_2 = remoteRomImage[l_offset];
        return (l_char_1 == -1 && l_char_2 == -1);
    }

    private int importKits() {
        try {
            int outBank = 0;
            int copiedBankCount = 0;
            FileInputStream in = new FileInputStream(localRomFile.getAbsolutePath());
            while (in.available() > 0) {
                byte[] inBuf = new byte[RomUtilities.BANK_SIZE];
                int readBytes = in.read(inBuf);
                assert(readBytes == inBuf.length);
                if (inBuf[0] == 0x60 && inBuf[1] == 0x40) {
                    //is kit bank
                    outBank++;
                    while (!isKitBank(outBank) && !isEmptyKitBank(outBank)) {
                        outBank++;
                    }
                    int outPtr = outBank * RomUtilities.BANK_SIZE;
                    for (int i = 0; i < RomUtilities.BANK_SIZE; i++) {
                        remoteRomImage[outPtr++] = inBuf[i];
                    }
                    copiedBankCount++;
                }
            }
            in.close();
            return copiedBankCount;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

    private void upgradeFromSelectedFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select ROM File");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Game Boy ROM (*.gb)", "gb"));

            int result = fileChooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File customRomFile = fileChooser.getSelectedFile();
            if (!customRomFile.exists() || customRomFile.length() != remoteRomImage.length) {
                JOptionPane.showMessageDialog(this,
                        "Invalid ROM file! File must be " + remoteRomImage.length + " bytes.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int reply = JOptionPane.showConfirmDialog(this,
                    "Upgrade to " + customRomFile.getName() + "?\n\n" +
                    "This will copy all kits, fonts, and palettes from\n" +
                    localRomFile.getName() + " to the selected ROM.",
                    "Upgrade to Selected ROM?",
                    JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                return;
            }

            try (FileInputStream fis = new FileInputStream(customRomFile)) {
                int bytesRead = 0;
                while (bytesRead < remoteRomImage.length) {
                    int read = fis.read(remoteRomImage, bytesRead, remoteRomImage.length - bytesRead);
                    if (read == -1) {
                        throw new IOException("Unexpected end of file");
                    }
                    bytesRead += read;
                }
            }

            importAll();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error loading ROM",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}