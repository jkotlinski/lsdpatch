package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class Swatch extends JPanel {
    public RGB555 rgb() {
        return rgb555;
    }

    public interface Listener {
        void swatchChanged();
    }
    final LinkedList<Listener> listeners = new LinkedList<>();
    private final RGB555 rgb555 = new RGB555();

    public Swatch() {
        setPreferredSize(new Dimension(50, 37));
        setBorder(BorderFactory.createLoweredBevelBorder());
    }

    public int r() {
        return rgb555.r();
    }

    public int g() {
        return rgb555.g();
    }

    public int b() {
        return rgb555.b();
    }

    public void setRGB(int r, int g, int b) {
        boolean changed = r != rgb555.r() || g != rgb555.g() || b != rgb555.b();
        rgb555.setR(r);
        rgb555.setG(g);
        rgb555.setB(b);
        if (changed) {
            for (Listener listener : listeners) {
                listener.swatchChanged();
            }
        }
        setBackground(new Color(ColorUtil.colorCorrect(new Color(r << 3, g << 3, b << 3))));
    }

    public void randomize(Random rand) {
        setRGB(rand.nextInt(32), rand.nextInt(32), rand.nextInt(32));
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void deselect() {
        setBorder(BorderFactory.createLoweredBevelBorder());
    }
}
