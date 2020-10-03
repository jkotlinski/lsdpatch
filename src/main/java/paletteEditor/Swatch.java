package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

public class Swatch extends JPanel implements MouseListener {
    ColorPicker colorPicker;
    RGB555 rgb555;

    static LinkedList<Swatch> allSwatches = new LinkedList<>();

    Swatch(RGB555 rgb555, ColorPicker colorPicker) {
        this.rgb555 = rgb555;
        this.colorPicker = colorPicker;
        addMouseListener(this);
        allSwatches.add(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void select() {
        mousePressed(null);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        colorPicker.setColor(rgb555.r(), rgb555.g(), rgb555.b());
        colorPicker.subscribe((r, g, b) -> {
            rgb555.setR(r);
            rgb555.setG(g);
            rgb555.setB(b);
        });
        for (Swatch panel : allSwatches) {
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
