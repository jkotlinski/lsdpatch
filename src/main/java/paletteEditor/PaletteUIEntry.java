package paletteEditor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Random;

class PaletteUIEntry {
    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    public final JSpinner[] background = new JSpinner[3];
    public final JSpinner[] foreground = new JSpinner[3];
    public final JPanel[] preview = new JPanel[2];

    public PaletteUIEntry() {
        createSpinners(background);
        createSpinners(foreground);
        createPreviews(preview);
    }

    public void registerToPanel(JPanel panel, String entryName) {
        panel.add(new JLabel(entryName), "span, split 3");
        for (int i = 0; i < preview.length; ++i) {
            panel.add(preview[i], i == preview.length - 1 ? "grow, wrap" : "grow");
        }
        for (int i = 0; i < background.length; ++i) {
            panel.add(background[i], i == background.length - 1 ? "grow, wrap" : "grow");
        }
        for (int i = 0; i < foreground.length; ++i) {
            panel.add(foreground[i], i == foreground.length - 1 ? "grow, wrap 10" : "grow");
        }
    }

    public void createPreviews(JPanel[] previews) {
        for (int i = 0; i < previews.length; ++i) {
            previews[i] = new JPanel();
            previews[i].setBorder(previewLabelBorder);
        }
    }

    // TODO compute the color from the internal data
    public void updatePreviews(Color firstColor, Color secondColor) {
        preview[0].setBackground(firstColor);
        preview[1].setBackground(secondColor);
    }

    private void createSpinners(JSpinner[] spinners) {
        for (int i = 0; i < spinners.length; ++i) {
            spinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 31, 1));
        }
    }

    public void updateSpinnersFromColor(Color foregroundColor, Color backgroundColor) {
        background[0].setValue(backgroundColor.getRed() >> 3);
        background[1].setValue(backgroundColor.getBlue() >> 3);

        foreground[0].setValue(foregroundColor.getRed() >> 3);
        foreground[1].setValue(foregroundColor.getGreen() >> 3);
        foreground[2].setValue(foregroundColor.getBlue() >> 3);

    }

    public void addListenerToAllSpinners(ChangeListener listener) {
        for (JSpinner spinner : background) {
            spinner.addChangeListener(listener);
        }
        for (JSpinner spinner : foreground) {
            spinner.addChangeListener(listener);
        }
    }

    public void randomize(Random rand) {
        for (JSpinner spinner : background) {
            spinner.setValue(rand.nextInt(32));
        }
        for (JSpinner spinner : foreground) {
            spinner.setValue(rand.nextInt(32));
        }
    }

    public void writeToRom(byte[] romImage, int offset) {
        int r1 = (Integer) background[0].getValue();
        int g1 = (Integer) background[1].getValue();
        int b1 = (Integer) background[2].getValue();
        // gggrrrrr 0bbbbbgg
        romImage[offset] = (byte) (r1 | (g1 << 5));
        romImage[offset + 1] = (byte) ((g1 >> 3) | (b1 << 2));

        int r2 = (Integer) foreground[0].getValue();
        int g2 = (Integer) foreground[1].getValue();
        int b2 = (Integer) foreground[2].getValue();
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
