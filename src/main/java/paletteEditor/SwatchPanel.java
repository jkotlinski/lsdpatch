package paletteEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class SwatchPanel extends JPanel implements SwatchPair.Listener {
    SwatchPair.Listener listener;

    private final LinkedList<SwatchPair> swatchPairs = new LinkedList<>();
    private final Random random = new Random();

    public final SwatchPair normalSwatchPair = new SwatchPair();
    public final SwatchPair shadedSwatchPair = new SwatchPair();
    public final SwatchPair alternateSwatchPair = new SwatchPair();
    public final SwatchPair cursorSwatchPair = new SwatchPair();
    public final SwatchPair scrollBarSwatchPair = new SwatchPair();

    private Swatch selectedSwatch;

    public SwatchPanel() {
        setLayout(new MigLayout());

        JButton randomizeButton = new JButton("Randomize all");
        randomizeButton.addActionListener((e) -> randomize());
        add(randomizeButton, "grow, span, wrap");

        JButton cloneButton = new JButton("Clone color");
        cloneButton.setPreferredSize(new Dimension(0, 0));
        cloneButton.addActionListener(e -> cloneStart());
        add(cloneButton, "grow, span, wrap");

        JButton swapButton = new JButton("Swap color");
        swapButton.setPreferredSize(new Dimension(0, 0));
        swapButton.addActionListener(e -> swapStart());
        add(swapButton, "grow, span, wrap");

        add(normalSwatchPair, "Normal");
        add(shadedSwatchPair, "Shaded");
        add(alternateSwatchPair, "Alternate");
        add(cursorSwatchPair, "Cursor");
        add(scrollBarSwatchPair, "Scroll Bar");
    }

    enum CommandState {
        OFF,
        SWAP,
        CLONE
    }
    CommandState commandState;
    private void swapStart() {
        if (selectedSwatch == null) {
            return;
        }
        commandState = CommandState.SWAP;
        updateCursor();
    }
    private void cloneStart() {
        if (selectedSwatch == null) {
            return;
        }
        commandState = CommandState.CLONE;
        updateCursor();
    }

    private void updateCursor() {
        setCursor(new Cursor(commandState == CommandState.OFF
                ? Cursor.DEFAULT_CURSOR
                : Cursor.HAND_CURSOR));
    }

    public void addListener(SwatchPair.Listener listener) {
        this.listener = listener;
    }

    public void add(SwatchPair swatchPair, String swatchPairName) {
        swatchPair.registerToPanel(this, swatchPairName);
        swatchPairs.add(swatchPair);
        swatchPair.addListener(this);
    }

    public void randomize() {
        for (SwatchPair swatchPair : swatchPairs) {
            swatchPair.randomize(random);
        }
    }

    private void handleClone(Swatch swatch) {
        if (commandState != CommandState.CLONE) {
            return;
        }
        swatch.setRGB(selectedSwatch.r(), selectedSwatch.g(), selectedSwatch.b());
        commandState = CommandState.OFF;
        updateCursor();
    }

    private void handleSwap(Swatch swatch) {
        if (commandState != CommandState.SWAP) {
            return;
        }
        int r = swatch.r();
        int g = swatch.g();
        int b = swatch.b();
        swatch.setRGB(selectedSwatch.r(), selectedSwatch.g(), selectedSwatch.b());
        selectedSwatch.setRGB(r, g, b);
        commandState = CommandState.OFF;
        updateCursor();
    }

    @Override
    public void swatchSelected(Swatch swatch) {
        handleSwap(swatch);
        handleClone(swatch);
        selectedSwatch = swatch;
        for (SwatchPair swatchPair : swatchPairs) {
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
