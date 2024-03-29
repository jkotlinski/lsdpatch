package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

class SwatchPair implements Swatch.Listener {
    public interface Listener {
        void swatchSelected(Swatch swatch);
        void swatchChanged();
    }
    private final LinkedList<Listener> listeners = new LinkedList<>();
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    @Override
    public void swatchChanged() {
        for (Listener listener : listeners) {
            listener.swatchChanged();
        }
    }

    public final Swatch bgSwatch = new Swatch();
    public final Swatch fgSwatch = new Swatch();

    public SwatchPair() {
        createSwatches();

        bgSwatch.addListener(this);
        fgSwatch.addListener(this);
    }

    public void registerToPanel(JPanel panel, String entryName) {
        panel.add(bgSwatch, "grow");
        bgSwatch.setToolTipText(entryName + " background");
        panel.add(fgSwatch, "grow, wrap");
        fgSwatch.setToolTipText(entryName + " text");
    }

    public void selectBackground() {
        select(bgSwatch);
    }

    public void selectForeground() {
        select(fgSwatch);
    }

    private void select(Swatch swatch) {
        for (Listener listener : listeners) {
            listener.swatchSelected(swatch);
        }
    }

    private void createSwatches() {
        bgSwatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                select(bgSwatch);
            }});

        fgSwatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                select(fgSwatch);
            }});
    }

    public void setColors(Color foregroundColor, Color backgroundColor) {
        bgSwatch.setRGB(backgroundColor.getRed() >> 3,
                backgroundColor.getGreen() >> 3,
                backgroundColor.getBlue() >> 3);
        fgSwatch.setRGB(foregroundColor.getRed() >> 3,
                foregroundColor.getGreen() >> 3,
                foregroundColor.getBlue() >> 3);
    }

    public void randomize(Random rand) {
        bgSwatch.randomize(rand);
        fgSwatch.randomize(rand);
    }

    private RGB555 findMidTone(RGB555 bg, RGB555 fg) {
        ColorUtil.ColorSpace prevColorSpace = ColorUtil.colorSpace;
        ColorUtil.colorSpace = ColorUtil.ColorSpace.Emulator;
        Color target = midToneTarget(bg, fg);
        RGB555 bestRgb = findBestRgb(new RGB555(15, 15, 15), target);
        ColorUtil.colorSpace = prevColorSpace;
        return bestRgb;
    }

    private Color midToneTarget(RGB555 bg, RGB555 fg) {
        int r1 = ColorUtil.to8bit(bg.r());
        int g1 = ColorUtil.to8bit(bg.g());
        int b1 = ColorUtil.to8bit(bg.b());
        int r2 = ColorUtil.to8bit(fg.r());
        int g2 = ColorUtil.to8bit(fg.g());
        int b2 = ColorUtil.to8bit(fg.b());
        Color bgColor = new Color(ColorUtil.colorCorrect( new Color(r1, g1, b1)));
        Color fgColor = new Color(ColorUtil.colorCorrect( new Color(r2, g2, b2)));
        int k = 55;
        int midR = (bgColor.getRed() * k + fgColor.getRed() * (100 - k)) / 100;
        int midG = (bgColor.getGreen() * k + fgColor.getGreen() * (100 - k)) / 100;
        int midB = (bgColor.getBlue() * k + fgColor.getBlue() * (100 - k)) / 100;
        return new Color(midR, midG, midB);
    }

    private RGB555 findBestRgb(RGB555 start, Color target) {
        TreeMap<Double, RGB555> map = new TreeMap<>();
        double startDiff = diff(target, start.r(), start.g(), start.b());
        map.put(startDiff, start);
        add(map, target, start, 1, 0, 0);
        add(map, target, start, -1, 0, 0);
        add(map, target, start, 0, 1, 0);
        add(map, target, start, 0, -1, 0);
        add(map, target, start, 0, 0, 1);
        add(map, target, start, 0, 0, -1);
        if (map.firstKey() == startDiff) {
            return start;
        }
        return findBestRgb(map.firstEntry().getValue(), target);
    }

    private void add(TreeMap<Double, RGB555> map, Color target, RGB555 start, int rd, int gd, int bd) {
        int r = start.r() + rd;
        int g = start.g() + gd;
        int b = start.b() + bd;
        if (r < 0 || r > 31 || g < 0 || g > 31 || b < 0 || b > 31) {
            return;
        }
        RGB555 rgb555 = new RGB555(start.r() + rd, start.g() + gd, start.b() + bd);
        map.put(diff(target, r, g, b), rgb555);
    }

    private static double diff(Color target, int r, int g, int b) {
        int rgb24 = ColorUtil.colorCorrect(
                ColorUtil.to8bit(r),
                ColorUtil.to8bit(g),
                ColorUtil.to8bit(b));
        int rr = rgb24 >> 16;
        int gg = (rgb24 >> 8) & 0xff;
        int bb = rgb24 & 0xff;

        // red-mean
        double rm = (rr + target.getRed()) / 2.0;
        double rd = rr - target.getRed();
        double gd = gg - target.getGreen();
        double bd = bb - target.getBlue();

        return Math.sqrt((2.0 + rm / 256.0) * rd * rd +
                4.0 * gd * gd +
                ((2.0 + (255.0 - rm) / 256.0) * bd * bd));
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

        // Mid-tone.
        RGB555 rgbMid = findMidTone(new RGB555(r1, g1, b1), new RGB555(r2, g2, b2));

        romImage[offset + 2] = (byte) (rgbMid.r() | (rgbMid.g() << 5));
        romImage[offset + 3] = (byte) ((rgbMid.g() >> 3) | (rgbMid.b() << 2));
        romImage[offset + 4] = romImage[offset + 2];
        romImage[offset + 5] = romImage[offset + 3];
    }

    public void deselect() {
        bgSwatch.deselect();
        fgSwatch.deselect();
    }
}