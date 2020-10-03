package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class SaturationBrightnessPanel extends JPanel implements HuePanel.Listener, MouseListener, MouseMotionListener {
    interface Listener {
        void saturationBrightnessChanged();
    }

    private final LinkedList<Listener> listeners = new LinkedList<>();

    final int width = 256;
    final int height = 256;

    final Point selection = new Point();

    HuePanel huePanel;

    boolean mousePressed;

    public SaturationBrightnessPanel(HuePanel huePanel, float saturation, float brightness) {
        selection.setLocation(saturation * width, (1 - brightness) * height);
        this.huePanel = huePanel;
        huePanel.subscribe(this);
        setMinimumSize(new Dimension(width, height));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void hueChanged() {
        repaint();
    }

    public float saturation() {
        return (float) selection.getX() / width;
    }

    public float brightness() {
        return 1 - (float)selection.getY()/width;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float s = (float) x / width;
                float b = 1 - (float) y / width;
                Color color = Color.getHSBColor(huePanel.hue(), s, b);
                color = new Color(ColorUtil.colorCorrect(color));
                image.setRGB(x, y, color.getRGB());
            }
        }
        g.drawImage(image, 0, 0, null);
        g.setXORMode(Color.WHITE);
        g.setColor(Color.BLACK);
        final int r = 3;
        g.drawRect((int)selection.getX() - r, (int)selection.getY() - r, 2 * r, 2 * r);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        mouseDragged(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        selection.x = Math.min(width, Math.max(0, e.getX()));
        selection.y = Math.min(height, Math.max(0, e.getY()));
        for (Listener listener : listeners) {
            listener.saturationBrightnessChanged();
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
