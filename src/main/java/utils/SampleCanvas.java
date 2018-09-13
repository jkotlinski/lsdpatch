package utils;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class SampleCanvas extends Canvas {
    byte[] buf;

    public void setBufferContent(byte[] newBuffer)
    {
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
        gp.moveTo(0, h);
        for (int it = 0; it < buf.length; ++it) {
            double val = buf[it];
            if (val < 0) val += 256;
            val /= 0xf0;
            gp.lineTo(it * w / (buf.length - 1), h - h * val);
        }
        g.setColor(Color.YELLOW);
        g.draw(gp);
    }
}
