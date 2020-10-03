package paletteEditor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

class SwatchPair {

    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    public final Swatch bgSwatch = new Swatch();
    public final Swatch fgSwatch = new Swatch();

    private final ColorPicker colorPicker;

    public SwatchPair(ColorPicker colorPicker) {
        this.colorPicker = colorPicker;
        createSwatches();
    }

    public void registerToPanel(JPanel panel, String entryName) {
        final int previewWidth = 34;
        final int previewHeight = 34;
        panel.add(new JLabel(entryName), "span, wrap");
        panel.add(bgSwatch);
        bgSwatch.setMinimumSize(new Dimension(previewWidth, previewHeight));
        panel.add(fgSwatch, "wrap");
        fgSwatch.setMinimumSize(new Dimension(previewWidth, previewHeight));
    }

    public void selectBackground() {
        select(bgSwatch);
    }

    public void selectForeground() {
        select(fgSwatch);
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
        bgSwatch.setBorder(previewLabelBorder);
        bgSwatch.setMinimumSize(new Dimension(32, 0));
        bgSwatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                swatchPressed(e);
            }
        });

        fgSwatch.setBorder(previewLabelBorder);
        fgSwatch.setMinimumSize(new Dimension(32, 0));
        fgSwatch.addMouseListener(new MouseAdapter() {
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
        bgSwatch.setBackground(firstColor);
        fgSwatch.setBackground(secondColor);
    }

    public void setColors(Color foregroundColor, Color backgroundColor) {
        bgSwatch.setRGB(backgroundColor.getRed() >> 3,
                backgroundColor.getGreen() >> 3,
                backgroundColor.getBlue() >> 3);
        fgSwatch.setRGB(foregroundColor.getRed() >> 3,
                foregroundColor.getGreen() >> 3,
                foregroundColor.getBlue() >> 3);
    }

    public void addSwatchListener(Swatch.Listener listener) {
        bgSwatch.addListener(listener);
        fgSwatch.addListener(listener);
    }

    public void randomize(Random rand) {
        bgSwatch.randomize(rand);
        fgSwatch.randomize(rand);
    }

    public void writeToRom(byte[] romImage, int offset) {
        int r1 = bgSwatch.r();
        int g1 = bgSwatch.g();
        int b1 = bgSwatch.b();
        // gggrrrrr 0bbbbbgg
        romImage[offset] = (byte) (r1 | (g1 << 5));
        romImage[offset + 1] = (byte) ((g1 >> 3) | (b1 << 2));

        int r2 = fgSwatch.r();
        int g2 = fgSwatch.g();
        int b2 = fgSwatch.b();
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
