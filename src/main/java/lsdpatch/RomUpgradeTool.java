package lsdpatch;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RomUpgradeTool extends JFrame {
    final String developPath = "https://www.littlesounddj.com/lsd/latest/rom_images/develop/";
    final String stablePath = "https://www.littlesounddj.com/lsd/latest/rom_images/stable/";
    final String arduinoBoyPath = "https://www.littlesounddj.com/lsd/latest/rom_images/arduinoboy/";

    RomUpgradeTool(String romPath) throws IOException {
        String stableVersion = fetchVersion(stablePath);
        String developVersion = fetchVersion(developPath);
        String arduinoBoyVersion = fetchVersion(arduinoBoyPath);
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
        InputStream is = null;
        BufferedReader br;
        String line;
        String lines = new String();

        is = url.openStream();  // throws an IOException
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            lines = lines + line;
        }
        is.close();
        return lines;
    }
}
