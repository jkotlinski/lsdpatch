package paletteEditor;

import java.util.LinkedList;
import java.util.Random;

public class RGB555 {
    public interface Listener {
        void colorChanged();
    };
    private int r = -1;
    private int g = -1;
    private int b = -1;

    final LinkedList<Listener> listeners = new LinkedList<>();

    public void setR(int r) {
        assert(r >= 0 && r < 32);
        if (this.r == r) {
            return;
        }
        this.r = r;
        for (Listener listener : listeners) {
            listener.colorChanged();
        }
    }

    public void setG(int g) {
        assert(g >= 0 && g < 32);
        if (this.g == g) {
            return;
        }
        this.g = g;
        for (Listener listener : listeners) {
            listener.colorChanged();
        }
    }

    public void setB(int b) {
        assert(b >= 0 && b < 32);
        if (this.b == b) {
            return;
        }
        this.b = b;
        for (Listener listener : listeners) {
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
        listeners.add(listener);
    }
}