package kitEditor;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class SampleView extends Canvas {
    byte[] buf;

    public void setBufferContent(byte[] newBuffer) {
        buf = newBuffer;
        setBackground(Color.black);
    }

    @Override
    public void paint(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        double w = g.getClipBounds().getWidth();
        double h = g.getClipBounds().getHeight();

        if (buf == null) {
            return;
        }

        GeneralPath gp = new GeneralPath();
        gp.moveTo(1, h);
        for (int it = 0; it < buf.length; ++it) {
            double val = buf[it] & 0xf;
            val -= 7.5;
            val /= 7.5;
            gp.lineTo(it * w / (buf.length - 1), h * (1 - val) / 2);
        }
        g.setColor(Color.YELLOW);
        g.draw(gp);
    }
}
