package paletteEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedList;

public class ColorPicker extends JFrame implements HuePanel.Listener, SaturationBrightnessPanel.Listener {
    interface Listener {
        void colorChanged(int r, int g, int b);
    }

    final LinkedList<Listener> listeners = new LinkedList<>();

    final HuePanel huePanel;
    final SaturationBrightnessPanel saturationBrightnessPanel;

    public ColorPicker(int r, int g, int b) {
        assert(r >= 0 && r < 32);
        assert(g >= 0 && g < 32);
        assert(b >= 0 && b < 32);

        float[] hsb = new float[3];
        Color.RGBtoHSB(r << 3, g << 3, b << 3, hsb);

        huePanel = new HuePanel(hsb[0]);
        saturationBrightnessPanel = new SaturationBrightnessPanel(huePanel, hsb[1], hsb[2]);

        huePanel.subscribe(this);
        saturationBrightnessPanel.subscribe(this);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout());

        contentPane.add(saturationBrightnessPanel);
        contentPane.add(huePanel, "gap 5");
        pack();
    }

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }

    private void broadcastColor() {
        float hue = huePanel.hue();
        float saturation = saturationBrightnessPanel.saturation();
        float brightness = saturationBrightnessPanel.brightness();

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        byte b = (byte)((rgb & 255) >> 3);
        rgb >>= 8;
        byte g = (byte)((rgb & 255) >> 3);
        rgb >>= 8;
        byte r = (byte)((rgb & 255) >> 3);
        for (Listener listener : listeners) {
            listener.colorChanged(r, g, b);
        }
    }

    @Override
    public void hueChanged() {
        broadcastColor();
    }

    @Override
    public void saturationBrightnessChanged() {
        broadcastColor();
    }
}
