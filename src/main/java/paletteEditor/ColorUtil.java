package paletteEditor;

public class ColorUtil {
    private static final int[] scaleChannelWithCurve = {
            0, 5, 8, 11, 16, 22, 28, 36, 43, 51, 59, 67, 77, 87, 97, 107,
            119, 130, 141, 153, 166, 177, 188, 200, 209, 221, 230, 238, 245, 249, 252, 255
    };

    // From Sameboy.
    public static int colorCorrect(java.awt.Color c) {
        int r = c.getRed() >> 3;
        int g = c.getGreen() >> 3;
        int b = c.getBlue() >> 3;

        r = scaleChannelWithCurve[r];
        g = scaleChannelWithCurve[g];
        b = scaleChannelWithCurve[b];

        int new_r = r;
        int new_g = (g * 3 + b) / 4;
        int new_b = b;

        r = new_r;
        g = new_g;
        b = new_b;

        new_r = new_r * 7 / 8 + (    g + b) / 16;
        new_g = new_g * 7 / 8 + (r   +   b) / 16;
        new_b = new_b * 7 / 8 + (r + g    ) / 16;

        new_r = new_r * (224 - 32) / 255 + 32;
        new_g = new_g * (220 - 36) / 255 + 36;
        new_b = new_b * (216 - 40) / 255 + 40;

        return (new_r << 16) | (new_g << 8) | new_b;
    }
}