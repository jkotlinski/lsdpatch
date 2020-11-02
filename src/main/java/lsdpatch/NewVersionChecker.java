package lsdpatch;

import utils.GlobalHolder;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

public class NewVersionChecker {
    public static String getCurrentVersion() {
        String version = GlobalHolder.class.getPackage().getImplementationVersion();
        if (version == null) {
            return "DEV";
        }
        return version;
    }

    public static void checkGithub(JFrame parent) {
        String currentVersion = getCurrentVersion();
        if (currentVersion.equals("DEV")) {
            return;
        }
        String response;
        try {
            String apiPath = "https://api.github.com/repos/jkotlinski/lsdpatch/releases";
            response = WwwUtil.fetchWwwPage(new URL(apiPath));
        } catch (IOException e) {
            return;
        }
        if (response.contains('"' + 'v' + currentVersion + '"')) {
            return;
        }
        if (JOptionPane.showConfirmDialog(parent,
                "A new LSDPatcher release is available. Do you want to see it?",
                "Version upgrade",
               JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            WwwUtil.openInBrowser("https://github.com/jkotlinski/lsdpatch/releases");
        }
    }
}