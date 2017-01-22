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

class TileEditor extends JPanel {
    byte[] romImage = null;
    int fontOffset = -1;
    int selectedTile = 0;

    void setRomImage(byte[] romImage) {
        this.romImage = romImage;
    }

    void setFontOffset(int offset) {
        fontOffset = offset;
        repaint();
    }

    void setTile(int tile) {
        selectedTile = tile;
        repaint();
    }

    private int getColor(int tile, int x, int y) {
        int tileOffset = fontOffset + tile * 16 + y * 2;
        int xMask = 7 - x;
        int value = (romImage[tileOffset] >> xMask) & 1;
        value |= ((romImage[tileOffset + 1] >> xMask) & 1) << 1;
        return value;
    }

    private void switchColor(Graphics g, int c) {
        switch (c & 3) {
            case 0:
                g.setColor(java.awt.Color.white);
                break;
            case 1:
                g.setColor(java.awt.Color.lightGray);
                break;
            case 2:
                g.setColor(java.awt.Color.pink);  // Not used.
                break;
            case 3:
                g.setColor(java.awt.Color.black);
                break;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                int color = getColor(selectedTile, x, y);
                switchColor(g, color);
                int pixelWidth = getWidth() / 8;
                int pixelHeight = getHeight() / 8;
                g.fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight);
            }
        }
    }
}
