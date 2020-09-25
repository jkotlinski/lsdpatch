package lsdpatch;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RomUpgradeTool extends JFrame {
    final String developPath = "https://www.littlesounddj.com/lsd/latest/rom_images/develop/";
    final String stablePath = "https://www.littlesounddj.com/lsd/latest/rom_images/stable/";
    final String arduinoBoyPath = "https://www.littlesounddj.com/lsd/latest/rom_images/arduinoboy/";

    RomUpgradeTool(byte[] romImage) throws IOException {
        String currentRomVersion = version(romImage);
        String stableVersion = fetchVersion(stablePath);
        String developVersion = fetchVersion(developPath);
        String arduinoBoyVersion = fetchVersion(arduinoBoyPath);
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

    private String fetchVersion(String basePath) throws IOException {
        String page = fetchVersion(new URL(basePath));
        Pattern p = Pattern.compile("lsdj\\d_\\d_\\d[-a-zA-Z]*\\.zip");
        Matcher m = p.matcher(page);
        if (m.find()) {
            MatchResult matchResult = m.toMatchResult();
            return matchResult.group();
        } else {
            return null;
        }
    }

    private String fetchVersion(URL url) throws IOException {
        InputStream is;
        BufferedReader br;
        String line;
        StringBuilder lines = new StringBuilder();

        is = url.openStream();  // throws an IOException
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            lines.append(line);
        }
        is.close();
        return lines.toString();
    }
}
