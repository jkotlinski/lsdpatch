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

    public final SwatchPair normalSwatchPair = new SwatchPair();
    public final SwatchPair shadedSwatchPair = new SwatchPair();
    public final SwatchPair alternateSwatchPair = new SwatchPair();
    public final SwatchPair cursorSwatchPair = new SwatchPair();
    public final SwatchPair scrollBarSwatchPair = new SwatchPair();

    public SwatchPanel() {
        setLayout(new MigLayout());

        add(normalSwatchPair, "Normal");
        add(shadedSwatchPair, "Shaded");
        add(alternateSwatchPair, "Alternate");
        add(cursorSwatchPair, "Cursor");
        add(scrollBarSwatchPair, "Scroll Bar");

        normalSwatchPair.selectBackground();
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
        if (listener != null) {
            listener.swatchSelected(swatch);
        }
    }

    @Override
    public void swatchChanged() {
        if (listener != null) {
            listener.swatchChanged();
        }
    }

    public void writeToRom(byte[] romImage, int selectedPaletteOffset) {
        normalSwatchPair.writeToRom(romImage, selectedPaletteOffset);
        shadedSwatchPair.writeToRom(romImage, selectedPaletteOffset + 8);
        alternateSwatchPair.writeToRom(romImage, selectedPaletteOffset + 16);
        cursorSwatchPair.writeToRom(romImage, selectedPaletteOffset + 24);
        scrollBarSwatchPair.writeToRom(romImage, selectedPaletteOffset + 32);
    }
}
