package paletteEditor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

class PaletteUIEntry {
    private static class PreviewPanel extends JPanel implements MouseListener {
        ColorPicker colorPicker;
        JSpinner[] spinners;

        static LinkedList<PreviewPanel> allPreviewPanels = new LinkedList<>();

        PreviewPanel(JSpinner[] spinners, ColorPicker colorPicker) {
            this.spinners = spinners;
            this.colorPicker = colorPicker;
            addMouseListener(this);
            allPreviewPanels.add(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        public void select() {
            mousePressed(null);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            colorPicker.setColor(
                    (int)spinners[0].getValue(),
                    (int)spinners[1].getValue(),
                    (int)spinners[2].getValue());
            colorPicker.subscribe((r, g, b) -> {
                spinners[0].setValue(r);
                spinners[1].setValue(g);
                spinners[2].setValue(b);
            });
            for (PreviewPanel panel : allPreviewPanels) {
                if (panel != this) {
                    panel.setBorder(BorderFactory.createLoweredBevelBorder());
                }
            }
            final int w = 3;
            setBorder(BorderFactory.createMatteBorder(w, w, w, w, Color.magenta));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private final Border previewLabelBorder = javax.swing.BorderFactory.createLoweredBevelBorder();

    public final JSpinner[] background = new JSpinner[3];
    public final JSpinner[] foreground = new JSpinner[3];
    public final PreviewPanel[] preview = new PreviewPanel[2];

    public PaletteUIEntry(ColorPicker colorPicker) {
        createSpinners(background);
        createSpinners(foreground);
        createPreviews(colorPicker);
    }

    public void registerToPanel(JPanel panel, String entryName) {
        final int previewWidth = 34;
        final int previewHeight = 34;
        panel.add(new JLabel(entryName), "span, wrap");
        panel.add(preview[0]);
        preview[0].setMinimumSize(new Dimension(previewWidth, previewHeight));
        panel.add(preview[1], "wrap");
        preview[1].setMinimumSize(new Dimension(previewWidth, previewHeight));
    }

    public void selectBackground() {
        preview[0].select();
    }

    public void selectForeground() {
        preview[1].select();
    }

    private void createPreviews(ColorPicker colorPicker) {
        preview[0] = new PreviewPanel(background, colorPicker);
        preview[0].setBorder(previewLabelBorder);
        preview[0].setMinimumSize(new Dimension(32, 0));

        preview[1] = new PreviewPanel(foreground, colorPicker);
        preview[1].setBorder(previewLabelBorder);
        preview[1].setMinimumSize(new Dimension(32, 0));
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
        background[1].setValue(backgroundColor.getGreen() >> 3);
        background[2].setValue(backgroundColor.getBlue() >> 3);

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
