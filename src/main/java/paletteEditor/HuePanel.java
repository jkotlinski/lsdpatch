package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

class HuePanel extends JPanel implements MouseListener, MouseMotionListener {
    int selectedPosition;
    final int width = 24;
    final int height = 244;
    private final LinkedList<Listener> listeners = new LinkedList<>();

    public interface Listener {
        void hueChanged();
    }

    HuePanel() {
        setMinimumSize(new Dimension(width, height));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    void setHue(float hue) {
        assert(hue >= 0);
        assert(hue <= 1);
        selectedPosition = (int) (hue * height);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; ++y) {
            Color color = Color.getHSBColor((float) y / height, 1, 1);
            color = new Color(ColorUtil.colorCorrect(color));
            for (int x = 0; x < width; ++x) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        g.drawImage(image, 0, 0, null);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        int r = 5;
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(width / 2 - r, selectedPosition - r, 2 * r, 2 * r);
    }

    public float hue() {
        float hue = selectedPosition;
        hue /= height;
        assert(hue >= 0);
        assert(hue <= 1);
        return hue;
    }

    boolean mousePressed;

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
        selectedPosition = Math.max(0, Math.min(height, e.getY()));
        for (Listener listener : listeners) {
            listener.hueChanged();
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }
}

