package lsdpatch;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WwwUtil {
    static String fetchWwwPage(URL url) throws IOException {
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

    static void openInBrowser(String path) {
        try {
            Desktop.getDesktop().browse(new URI(path));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
