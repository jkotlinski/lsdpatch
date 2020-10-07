package paletteEditor;

public class ColorUtil {
    private static final int[] scaleChannelWithCurve = {
            0, 5, 8, 11, 16, 22, 28, 36, 43, 51, 59, 67, 77, 87, 97, 107,
            119, 130, 141, 153, 166, 177, 188, 200, 209, 221, 230, 238, 245, 249, 252, 255
    };

    static public boolean rawScreen;

    public static void setRawScreen(boolean enabled) {
        rawScreen = enabled;
    }

    public static boolean toggleRawScreen() {
        rawScreen = !rawScreen;
        return rawScreen;
    }

    public static int to8bit(int color) {
        assert(color >= 0);
        assert(color < 32);
        color <<= 3;
        color *= 0xff;
        return color / 0xf8;
    }

    // From Sameboy.
    public static int colorCorrect(java.awt.Color c) {
        return colorCorrect(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int colorCorrect(int r, int g, int b) {
        if (rawScreen) {
            r = (((r >> 3) << 3) * 0xff) / 0xf8;
            g = (((g >> 3) << 3) * 0xff) / 0xf8;
            b = (((b >> 3) << 3) * 0xff) / 0xf8;
            return (r << 16) | (g << 8) | b;
        }

        r >>= 3;
        g >>= 3;
        b >>= 3;

        r = scaleChannelWithCurve[r];
        g = scaleChannelWithCurve[g];
        b = scaleChannelWithCurve[b];

        int new_r = r;
        int new_g = (g * 3 + b) / 4;
        int new_b = b;

        r = new_r;
        g = new_r; // correct, according to LIJI
        b = new_r; // correct, according to LIJI

        new_r = new_r * 7 / 8 + (g + b) / 16;
        new_g = new_g * 7 / 8 + (r + b) / 16;
        new_b = new_b * 7 / 8 + (r + g) / 16;

        new_r = new_r * (224 - 32) / 255 + 32;
        new_g = new_g * (220 - 36) / 255 + 36;
        new_b = new_b * (216 - 40) / 255 + 40;

        return (new_r << 16) | (new_g << 8) | new_b;
    }
}