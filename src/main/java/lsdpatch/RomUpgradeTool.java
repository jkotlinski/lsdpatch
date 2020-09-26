// Copyright (C) 2020, Johan Kotlinski

package lsdpatch;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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

    private byte[] romImage;

    RomUpgradeTool(byte[] romImage) {
        assert (romImage != null);
        this.romImage = romImage;

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new MigLayout("wrap"));

        panel.add(new JLabel("Upgrade ROM to latest:"));
        JButton upgradeStableButton = new JButton("Stable version (recommended!)");
        JButton upgradeDevelopButton = new JButton("Development version (experimental!)");
        JButton upgradeArduinoBoyButton = new JButton("ArduinoBoy version");
        JButton viewChangeLogButton = new JButton("View Changelog");
        JButton viewLicenseButton = new JButton("View License Information");
        panel.add(upgradeStableButton, "growx");
        panel.add(upgradeDevelopButton, "growx");
        panel.add(upgradeArduinoBoyButton, "growx");
        panel.add(viewChangeLogButton, "growx, gaptop 10");
        panel.add(viewLicenseButton, "growx");
        pack();

        upgradeStableButton.addActionListener(e -> upgrade(stablePath));
        upgradeDevelopButton.addActionListener(e -> upgrade(developPath));
        upgradeArduinoBoyButton.addActionListener(e -> upgrade(arduinoBoyPath));
        viewChangeLogButton.addActionListener(e -> openInBrowser(changeLogPath));
        viewLicenseButton.addActionListener(e -> openInBrowser(licensePath));
    }

    private void openInBrowser(String path) {
        try {
            Desktop.getDesktop().browse(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upgrade(String basePath) {
        try {
            String wwwVersion = fetchLatestRemoteVersion(basePath);
            int reply = JOptionPane.showConfirmDialog(this,
                    "Current ROM version: " + version(romImage) + '\n' +
                            "Upgrade to " + wwwVersion + '?',
                    "Upgrade?",
                    JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                return;
            }
            ZipInputStream zipInputStream = new ZipInputStream(new URL(basePath + wwwVersion).openStream());
            zipInputStream.getNextEntry();
            byte[] newRomImage = new byte[64 * 0x4000];
            int dstIndex = 0;
            while (dstIndex != newRomImage.length) {
                dstIndex += zipInputStream.read(newRomImage, dstIndex, newRomImage.length - dstIndex);
            }
            // TODO: migrate kits, palettes, fonts
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "Fetching new version failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String version(byte[] romImage) {
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
        String page = fetchWwwPage(new URL(basePath));
        Pattern p = Pattern.compile("lsdj\\d_\\d_\\d[-a-zA-Z]*\\.zip");
        Matcher m = p.matcher(page);
        if (m.find()) {
            MatchResult matchResult = m.toMatchResult();
            return matchResult.group();
        } else {
            return null;
        }
    }

    private String fetchWwwPage(URL url) throws IOException {
        InputStream is;
        BufferedReader br;
        String line;
        StringBuilder lines = new StringBuilder();

        is = url.openStream();
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            lines.append(line);
        }
        is.close();
        return lines.toString();
    }
}
