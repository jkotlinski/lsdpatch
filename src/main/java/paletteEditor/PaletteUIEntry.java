package paletteEditor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

class PaletteUIEntry {

    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    public final Swatch[] swatch = new Swatch[2];

    private final ColorPicker colorPicker;

    public PaletteUIEntry(ColorPicker colorPicker) {
        this.colorPicker = colorPicker;
        createSwatches();
    }

    public void registerToPanel(JPanel panel, String entryName) {
        final int previewWidth = 34;
        final int previewHeight = 34;
        panel.add(new JLabel(entryName), "span, wrap");
        panel.add(swatch[0]);
        swatch[0].setMinimumSize(new Dimension(previewWidth, previewHeight));
        panel.add(swatch[1], "wrap");
        swatch[1].setMinimumSize(new Dimension(previewWidth, previewHeight));
    }

    public void selectBackground() {
        select(swatch[0]);
    }

    public void selectForeground() {
        select(swatch[1]);
    }

    private void select(Swatch swatch) {
        colorPicker.setColor(swatch.r(), swatch.g(), swatch.b());
        colorPicker.subscribe(swatch::setRGB);
        /* TODO - move up in hierarchy where we can find all swatches
        for (Swatch panel : allSwatches) {
            if (panel != this) {
                panel.setBorder(BorderFactory.createLoweredBevelBorder());
            }
        }
         */
        final int w = 3;
        swatch.setBorder(BorderFactory.createMatteBorder(w, w, w, w, Color.magenta));
    }

    private void createSwatches() {
        swatch[0] = new Swatch();
        swatch[0].setBorder(previewLabelBorder);
        swatch[0].setMinimumSize(new Dimension(32, 0));
        swatch[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                swatchPressed(e);
            }
        });

        swatch[1] = new Swatch();
        swatch[1].setBorder(previewLabelBorder);
        swatch[1].setMinimumSize(new Dimension(32, 0));
        swatch[1].addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                swatchPressed(e);
            }
        });
    }

    private void swatchPressed(MouseEvent e) {
        select((Swatch)e.getSource());
    }

    // TODO compute the color from the internal data
    public void updatePreviews(Color firstColor, Color secondColor) {
        swatch[0].setBackground(firstColor);
        swatch[1].setBackground(secondColor);
    }

    public void setColors(Color foregroundColor, Color backgroundColor) {
        swatch[0].setRGB(backgroundColor.getRed() >> 3,
                backgroundColor.getGreen() >> 3,
                backgroundColor.getBlue() >> 3);
        swatch[1].setRGB(foregroundColor.getRed() >> 3,
                foregroundColor.getGreen() >> 3,
                foregroundColor.getBlue() >> 3);
    }

    public void listenToColorChanges(RGB555.Listener listener) {
        swatch[0].addChangeListener(listener);
        swatch[1].addChangeListener(listener);
    }

    public void randomize(Random rand) {
        swatch[0].randomize(rand);
        swatch[1].randomize(rand);
    }

    public void writeToRom(byte[] romImage, int offset) {
        int r1 = swatch[0].r();
        int g1 = swatch[0].g();
        int b1 = swatch[0].b();
        // gggrrrrr 0bbbbbgg
        romImage[offset] = (byte) (r1 | (g1 << 5));
        romImage[offset + 1] = (byte) ((g1 >> 3) | (b1 << 2));

        int r2 = swatch[1].r();
        int g2 = swatch[1].g();
        int b2 = swatch[1].b();
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
