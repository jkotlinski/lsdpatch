package paletteEditor;

public class ColorUtil {
    public static int colorCorrect(java.awt.Color c) {
        int r = ((c.getRed() >> 3) * 255) / 0xf8;
        int g = ((c.getGreen() >> 3) * 255) / 0xf8;
        int b = ((c.getBlue() >> 3) * 255) / 0xf8;

        // Matrix conversion from Gambatte.
        return (((r * 13 + g * 2 + b) >> 1) << 16)
                | ((g * 3 + b) << 9)
                | ((r * 3 + g * 2 + b * 11) >> 1);
    }
}
