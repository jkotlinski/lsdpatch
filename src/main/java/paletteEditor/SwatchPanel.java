package paletteEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Random;

public class SwatchPanel extends JPanel {
    private final HashMap<String, SwatchPair> swatchPairs = new HashMap<>();
    private final Random random = new Random();

    public SwatchPanel() {
        setLayout(new MigLayout());
    }

    public void add(SwatchPair swatchPair, String swatchPairName) {
        swatchPair.registerToPanel(this, swatchPairName);
        swatchPairs.put(swatchPairName, swatchPair);
    }

    public void randomize() {
        for (SwatchPair swatchPair : swatchPairs.values()) {
            swatchPair.randomize(random);
        }
    }
}
