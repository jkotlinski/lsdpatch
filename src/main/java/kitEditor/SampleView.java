package kitEditor;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Locale;

public class SampleView extends Canvas {
    private byte[] buf;
    private float duration;

    public void setBufferContent(byte[] newBuffer, float duration) {
        buf = newBuffer;
        setBackground(Color.black);
        this.duration = duration;
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
        gp.moveTo(0, h / 2);
        for (int it = 0; it < buf.length; ++it) {
            // Only draws every second sample. This is probably OK.
            double val = buf[it] & 0xf;
            val -= 7.5;
            val /= 7.5;
            gp.lineTo(it * w / (buf.length - 1), h * (1 - val) / 2);
        }
        g.setColor(Color.YELLOW);
        g.draw(gp);

        drawDuration(g, (int) w, (int) h);
    }

    private void drawDuration(Graphics2D g, int w, int h) {
        String durationText = String.format(Locale.US, "%.3fs", duration);
        int x = -g.getFontMetrics().stringWidth(durationText) - 1;
        int y = -2;
        g.setColor(Color.BLACK);
        g.drawString(durationText, w + x, h + y);
        --x;
        --y;
        g.setColor(Color.WHITE);
        g.drawString(durationText, w + x, h + y);
    }
}
