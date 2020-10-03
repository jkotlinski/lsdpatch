package paletteEditor;

import java.util.Random;

public class RGB555 {
    public interface Listener {
        void colorChanged();
    };
    private int r;
    private int g;
    private int b;

    Listener listener;

    public void setR(int r) {
        assert(r >= 0 && r < 32);
        if (this.r == r) {
            return;
        }
        this.r = r;
        if (listener != null) {
            listener.colorChanged();
        }
    }

    public void setG(int g) {
        assert(g >= 0 && g < 32);
        if (this.g == g) {
            return;
        }
        this.g = g;
        if (listener != null) {
            listener.colorChanged();
        }
    }

    public void setB(int b) {
        assert(b >= 0 && b < 32);
        if (this.b == b) {
            return;
        }
        this.b = b;
        if (listener != null) {
            listener.colorChanged();
        }
    }

    public int r() {
        return r;
    }

    public int g() {
        return g;
    }

    public int b() {
        return b;
    }

    public void randomize(Random rand) {
        setR(rand.nextInt(32));
        setG(rand.nextInt(32));
        setB(rand.nextInt(32));
    }

    public void addChangeListener(Listener listener) {
        assert(listener == null);
        this.listener = listener;
    }
}