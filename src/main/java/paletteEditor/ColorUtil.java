package paletteEditor;

import static java.lang.Math.pow;
import static java.lang.Math.round;

public class ColorUtil {
    private static final int[] scaleChannelWithCurve = {
            0, 6, 12, 20, 28, 36, 45, 56, 66, 76, 88, 100, 113, 125, 137, 149, 161, 172,
            182, 192, 202, 210, 218, 225, 232, 238, 243, 247, 250, 252, 254, 255
    };

    enum ColorSpace {
        Emulator,
        Reality
    }
    static public ColorSpace colorSpace = ColorSpace.Emulator;

    public static void setColorSpace(ColorSpace colorSpace_) {
        colorSpace = colorSpace_;
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
        r >>= 3;
        g >>= 3;
        b >>= 3;

        r = scaleChannelWithCurve[r];
        g = scaleChannelWithCurve[g];
        b = scaleChannelWithCurve[b];

        double gamma = colorSpace == ColorSpace.Emulator ? 1.6 : 2.2;

        int new_g = (int)round(pow((pow(g / 255.0, gamma) * 3 + pow(b / 255.0, gamma)) / 4, 1 / gamma) * 255);
        int new_r = r;
        int new_b = b;

        if (colorSpace == ColorSpace.Reality) {
            // r = new_r;
            g = new_r;
            b = new_r;

            new_r = new_r * 7 / 8 + (g + b) / 16;
            new_g = new_g * 7 / 8 + (r + b) / 16;
            new_b = new_b * 7 / 8 + (r + g) / 16;

            new_r = new_r * (162 - 45) / 255 + 45;
            new_g = new_g * (167 - 41) / 255 + 41;
            new_b = new_b * (157 - 38) / 255 + 38;
        }

        return (new_r << 16) | (new_g << 8) | new_b;
    }
}