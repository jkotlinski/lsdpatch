package paletteEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class SwatchPanel extends JPanel implements SwatchPair.Listener {
    SwatchPair.Listener listener;

    private final HashMap<String, SwatchPair> swatchPairs = new HashMap<>();
    private final Random random = new Random();

    public SwatchPanel() {
        setLayout(new MigLayout());
    }

    public void addListener(SwatchPair.Listener listener) {
        this.listener = listener;
    }

    public void add(SwatchPair swatchPair, String swatchPairName) {
        swatchPair.registerToPanel(this, swatchPairName);
        swatchPairs.put(swatchPairName, swatchPair);
        swatchPair.addListener(this);
    }

    public void randomize() {
        for (SwatchPair swatchPair : swatchPairs.values()) {
            swatchPair.randomize(random);
        }
    }

    @Override
    public void swatchSelected(Swatch swatch) {
        for (SwatchPair swatchPair : swatchPairs.values()) {
            swatchPair.deselect();
        }
        final int w = 3;
        swatch.setBorder(BorderFactory.createMatteBorder(w, w, w, w, Color.magenta));
        listener.swatchSelected(swatch);
    }

    @Override
    public void swatchChanged() {
        listener.swatchChanged();
    }
}
