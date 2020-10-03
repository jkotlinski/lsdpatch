package paletteEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

public class SwatchPanel extends JPanel implements MouseListener {
    ColorPicker colorPicker;
    RGB555 myColor;

    static LinkedList<SwatchPanel> allSwatchPanels = new LinkedList<>();

    SwatchPanel(RGB555 myColor, ColorPicker colorPicker) {
        this.myColor = myColor;
        this.colorPicker = colorPicker;
        addMouseListener(this);
        allSwatchPanels.add(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void select() {
        mousePressed(null);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        colorPicker.setColor(myColor.r(), myColor.g(), myColor.b());
        colorPicker.subscribe((r, g, b) -> {
            myColor.setR(r);
            myColor.setG(g);
            myColor.setB(b);
        });
        for (SwatchPanel panel : allSwatchPanels) {
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
