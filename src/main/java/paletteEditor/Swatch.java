package paletteEditor;

import javax.swing.*;

public class Swatch extends JPanel {
    private RGB555 rgb555;

    Swatch(RGB555 rgb555) {
        this.rgb555 = rgb555;
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
        rgb555.setR(r);
        rgb555.setG(g);
        rgb555.setB(b);
    }
}
