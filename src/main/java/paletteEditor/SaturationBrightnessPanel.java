package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class SaturationBrightnessPanel extends JPanel implements HuePanel.Listener, MouseListener, MouseMotionListener {
    private RGB555 rgb555 = null;
    public void printRGB555(RGB555 rgb555) {
        this.rgb555 = rgb555;
    }

    interface Listener {
        void saturationBrightnessChanged();
    }

    private Listener listener;

    final Point selection = new Point();

    HuePanel huePanel;

    boolean mousePressed;

    public SaturationBrightnessPanel(HuePanel huePanel) {
        this.huePanel = huePanel;
        huePanel.subscribe(this);
        setPreferredSize(new Dimension(254, 244));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setSaturationBrightness(float saturation, float brightness) {
        assert(saturation >= 0);
        assert(saturation <= 1);
        assert(brightness >= 0);
        assert(brightness <= 1);
        selection.setLocation(saturation * getWidth(), (1 - brightness) * getHeight());
        repaint();
    }

    public void subscribe(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void hueChanged() {
        repaint();
    }

    public float saturation() {
        return (float) selection.getX() / getWidth();
    }

    public float brightness() {
        return 1 - (float)selection.getY() / getHeight();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < getHeight(); ++y) {
            for (int x = 0; x < getWidth(); ++x) {
                float s = (float) x / getWidth();
                float b = 1 - (float) y / getHeight();
                Color color = Color.getHSBColor(huePanel.hue(), s, b);
                color = new Color(ColorUtil.colorCorrect(color));
                image.setRGB(x, y, color.getRGB());
            }
        }
        g.drawImage(image, 0, 0, null);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int radius = 7;
        int w = 2;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(w));
        g2d.drawOval((int) selection.getX() - radius,
                (int) selection.getY() - radius,
                2 * radius, 2 * radius);
        g2d.setColor(Color.WHITE);
        radius -= w;
        g2d.drawOval((int) selection.getX() - radius,
                (int) selection.getY() - radius,
                2 * radius, 2 * radius);

        String colorString = rgb555.r + "," + rgb555.g + "," + rgb555.b;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(colorString,
                getWidth() - fm.stringWidth(colorString),
                getHeight() - fm.getDescent());
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
        selection.x = Math.min(getWidth(), Math.max(0, e.getX()));
        selection.y = Math.min(getHeight(), Math.max(0, e.getY()));
        if (listener != null) {
            listener.saturationBrightnessChanged();
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
