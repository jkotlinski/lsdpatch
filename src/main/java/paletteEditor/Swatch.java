package paletteEditor;

import javax.swing.*;
import java.util.Random;

public class Swatch extends JPanel {
    private final RGB555 rgb555 = new RGB555();

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
        rgb555.setR(r);
        rgb555.setG(g);
        rgb555.setB(b);
    }

    public void randomize(Random rand) {
        rgb555.setR(rand.nextInt(32));
        rgb555.setG(rand.nextInt(32));
        rgb555.setB(rand.nextInt(32));
    }

    public void addChangeListener(RGB555.Listener listener) {
        rgb555.addChangeListener(listener);
    }
}
