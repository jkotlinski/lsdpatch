package fontEditor;
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

import structures.LSDJFont;

import java.awt.Dimension;
import java.awt.Graphics;

public class FontMap extends JPanel implements java.awt.event.MouseListener {
	private static final long serialVersionUID = -7745908775698863845L;
	byte[] romImage = null;
    int fontOffset = -1;
    int tileCount = 71;
    int tileZoom = 1;
    int displayTileSize = 16;

    public interface TileSelectListener {
        public void tileSelected(int tile);
    }

    private TileSelectListener tileSelectedListener = null;

    FontMap() {
        addMouseListener(this);
    }

    public void setTileSelectListener(TileSelectListener l) {
        tileSelectedListener = l;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    	tileZoom = Integer.min(getWidth()/LSDJFont.FONT_MAP_WIDTH/2 , getHeight()/LSDJFont.FONT_MAP_HEIGHT/2);
    	tileZoom = Integer.max(tileZoom, 1);
    	setPreferredSize(new Dimension(LSDJFont.FONT_MAP_WIDTH * tileZoom, LSDJFont.FONT_MAP_HEIGHT * tileZoom));
    	int offsetX = (getWidth()/2 - LSDJFont.FONT_MAP_WIDTH * tileZoom)/2;
    	int offsetY = (getHeight()/2 - LSDJFont.FONT_MAP_HEIGHT* tileZoom)/2;
    	
        for (int tile = 0; tile < tileCount; ++tile) {
            paintTile(g, tile, offsetX, offsetY);
        }
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

    private int getColor(int tile, int x, int y) {
        int tileOffset = fontOffset + tile * 16 + y * 2;
        int xMask = 7 - x;
        int value = (romImage[tileOffset] >> xMask) & 1;
        value |= ((romImage[tileOffset + 1] >> xMask) & 1) << 1;
        return value;
    }

    private void paintTile(Graphics g, int tile, int offsetX, int offsetY) {
    	displayTileSize = 16 * tileZoom;
    	int x = (tile % 8) * displayTileSize;
    	int y = (tile / 8) * displayTileSize;

        for (int row = 0; row < 8; ++row) {
            for (int column = 0; column < 8; ++column) {
                switchColor(g, getColor(tile, column, row));
                g.fillRect(offsetX + x + column * 2*tileZoom, offsetY + y + row * 2*tileZoom, 2*tileZoom, 2*tileZoom);
            }
        }
    }

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
    }

    public void setFontOffset(int fontOffset) {
        this.fontOffset = fontOffset;
        repaint();
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {}
    public void mouseExited(java.awt.event.MouseEvent e) {}
    public void mouseReleased(java.awt.event.MouseEvent e) {}
    public void mousePressed(java.awt.event.MouseEvent e) {}
    public void mouseClicked(java.awt.event.MouseEvent e) {
        int tile = (e.getY() / displayTileSize) * (getWidth() / displayTileSize) +
            e.getX() / displayTileSize;
        assert tile >= 0;
        if (tileSelectedListener != null && tile < tileCount) {
            tileSelectedListener.tileSelected(tile);
        }
    }

}
