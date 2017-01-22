/** Copyright (C) 2017 by Johan Kotlinski

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE. */

import javax.swing.JPanel;
import java.awt.Graphics;

public class FontMap extends JPanel {
    byte[] romImage = null;
    int fontOffset = -1;
    int tileCount = 71;
    int displayTileSize = 16;

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int tile = 0; tile < tileCount; ++tile) {
            paintTile(g, tile);
        }
    }

    private void paintTile(Graphics g, int tile) {
        int x = (tile * displayTileSize) % getWidth();
        int y = ((tile * displayTileSize) / getWidth()) * displayTileSize;

        g.drawLine(x, y, x, y);
    }

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
    }

    public void setFontOffset(int fontOffset) {
        this.fontOffset = fontOffset;
    }
}
