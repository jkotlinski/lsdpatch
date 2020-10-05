package paletteEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ColorPicker extends JPanel implements HuePanel.Listener, SaturationBrightnessPanel.Listener {
    interface Listener {
        void colorChanged(int r, int g, int b);
    }

    private Listener listener;

    final HuePanel huePanel;
    final SaturationBrightnessPanel saturationBrightnessPanel;

    public ColorPicker() {
        huePanel = new HuePanel();
        saturationBrightnessPanel = new SaturationBrightnessPanel(huePanel);

        huePanel.subscribe(this);
        saturationBrightnessPanel.subscribe(this);

        setLayout(new MigLayout());

        add(saturationBrightnessPanel);
        add(huePanel, "gap 5");
    }

    public void setColor(int r, int g, int b) {
        assert(r >= 0 && r < 32);
        assert(g >= 0 && g < 32);
        assert(b >= 0 && b < 32);

        r <<= 3;
        g <<= 3;
        b <<= 3;
        r *= 0xff;
        r /= 0xf8;
        g *= 0xff;
        g /= 0xf8;
        b *= 0xff;
        b /= 0xf8;

        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        huePanel.setHue(hsb[0]);
        saturationBrightnessPanel.setSaturationBrightness(hsb[1], hsb[2]);
    }

    public void subscribe(Listener listener) {
        this.listener = listener;
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
        if (listener != null) {
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
