package paletteEditor;

public class RGB555 {
    private int r = -1;
    private int g = -1;
    private int b = -1;

    public void setR(int r) {
        assert(r >= 0 && r < 32);
        this.r = r;
    }

    public void setG(int g) {
        assert(g >= 0 && g < 32);
        this.g = g;
    }

    public void setB(int b) {
        assert(b >= 0 && b < 32);
        this.b = b;
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
}