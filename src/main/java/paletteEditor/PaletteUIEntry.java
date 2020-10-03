package paletteEditor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Random;

class PaletteUIEntry {

    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    public final RGB555 background = new RGB555();
    public final RGB555 foreground = new RGB555();
    public final Swatch[] preview = new Swatch[2];

    public PaletteUIEntry(ColorPicker colorPicker) {
        createPreviews(colorPicker);
    }

    public void registerToPanel(JPanel panel, String entryName) {
        final int previewWidth = 34;
        final int previewHeight = 34;
        panel.add(new JLabel(entryName), "span, wrap");
        panel.add(preview[0]);
        preview[0].setMinimumSize(new Dimension(previewWidth, previewHeight));
        panel.add(preview[1]);
        preview[1].setMinimumSize(new Dimension(previewWidth, previewHeight));

        JButton swapButton = new JButton("<>");
        swapButton.addActionListener(e -> {
            int tmp = background.r();
            background.setR(foreground.r());
            foreground.setR(tmp);
            tmp = background.g();
            background.setG(foreground.g());
            foreground.setG(tmp);
            tmp = background.b();
            background.setB(foreground.b());
            foreground.setB(tmp);
        });
        panel.add(swapButton, "wrap");
    }

    public void selectBackground() {
        preview[0].select();
    }

    public void selectForeground() {
        preview[1].select();
    }

    private void createPreviews(ColorPicker colorPicker) {
        preview[0] = new Swatch(background, colorPicker);
        preview[0].setBorder(previewLabelBorder);
        preview[0].setMinimumSize(new Dimension(32, 0));

        preview[1] = new Swatch(foreground, colorPicker);
        preview[1].setBorder(previewLabelBorder);
        preview[1].setMinimumSize(new Dimension(32, 0));
    }

    // TODO compute the color from the internal data
    public void updatePreviews(Color firstColor, Color secondColor) {
        preview[0].setBackground(firstColor);
        preview[1].setBackground(secondColor);
    }

    public void setColors(Color foregroundColor, Color backgroundColor) {
        background.setR(backgroundColor.getRed() >> 3);
        background.setG(backgroundColor.getGreen() >> 3);
        background.setB(backgroundColor.getBlue() >> 3);

        foreground.setR(foregroundColor.getRed() >> 3);
        foreground.setG(foregroundColor.getGreen() >> 3);
        foreground.setB(foregroundColor.getBlue() >> 3);
    }

    public void listenToColorChanges(RGB555.Listener listener) {
        background.addChangeListener(listener);
        foreground.addChangeListener(listener);
    }

    public void randomize(Random rand) {
        background.randomize(rand);
        foreground.randomize(rand);
    }

    public void writeToRom(byte[] romImage, int offset) {
        int r1 = background.r();
        int g1 = background.g();
        int b1 = background.b();
        // gggrrrrr 0bbbbbgg
        romImage[offset] = (byte) (r1 | (g1 << 5));
        romImage[offset + 1] = (byte) ((g1 >> 3) | (b1 << 2));

        int r2 = foreground.r();
        int g2 = foreground.g();
        int b2 = foreground.b();
        romImage[offset + 6] = (byte) (r2 | (g2 << 5));
        romImage[offset + 7] = (byte) ((g2 >> 3) | (b2 << 2));

        // Generating antialiasing colors.
        int rMid = (r1 + r2) / 2;
        int gMid = (g1 + g2) / 2;
        int bMid = (b1 + b2) / 2;
        romImage[offset + 2] = (byte) (rMid | (gMid << 5));
        romImage[offset + 3] = (byte) ((gMid >> 3) | (bMid << 2));
        romImage[offset + 4] = romImage[offset + 2];
        romImage[offset + 5] = romImage[offset + 3];
    }

}
