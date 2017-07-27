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

import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import structures.LSDJFont;

class TileEditor extends JPanel implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {

	private static final long serialVersionUID = 4048727729255703626L;

	public interface TileChangedListener {
        void tileChanged();
    }

//    byte[] romImage = null;
//    int fontOffset = -1;
    LSDJFont font = null;
    int selectedTile = 0;
    int color = 3;
    int rightColor = 3;

    int clipboard[][] = null;

    TileChangedListener tileChangedListener;

    TileEditor() {
    	font = new LSDJFont();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    void setRomImage(byte[] romImage) {
        font.setRomImage(romImage);
    }

    void setFontOffset(int offset) {
    	font.setFontOffset(offset);
    	repaint();
    }

    void setTile(int tile) {
        selectedTile = tile;
        repaint();
    }
    
    int getTile()
    {
    	return selectedTile;
    }
    
    void shiftUp(int tile)
    {
    	font.shiftUp(tile);
    	tileChanged();
    }
    
    void shiftDown(int tile)
    {
    	font.shiftDown(tile);
    	tileChanged();
    }
    
    void shiftRight(int tile)
    {
    	font.shiftRight(tile);
    	tileChanged();
    }
    
    void shiftLeft(int tile)
    {
    	font.shiftLeft(tile);
    	tileChanged();
    }
    
    private int getColor(int tile, int x, int y) {
    	return font.getTilePixel(tile, x, y);
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

    private void paintGrid(Graphics g) {
        g.setColor(java.awt.Color.gray);
        int minimumDimension = Integer.min(getWidth(), getHeight());
        int offsetX = (getWidth() - minimumDimension) / 2;
        int offsetY = (getHeight() - minimumDimension) / 2;
        int dx = minimumDimension / 8;
        for (int x = dx + offsetX; x < minimumDimension + offsetX; x += dx) {
            g.drawLine(x, offsetY, x, minimumDimension + offsetY);
        }

        int dy = minimumDimension / 8;
        for (int y = dy + offsetY; y < minimumDimension + offsetY; y += dy) {
            g.drawLine(offsetX, y, offsetX + minimumDimension, y);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int minimumDimension = Integer.min(getWidth(), getHeight());
        int offsetX = (getWidth() - minimumDimension) / 2;
        int offsetY = (getHeight() - minimumDimension) / 2;
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                int color = getColor(selectedTile, x, y);
                switchColor(g, color);
                int pixelWidth = minimumDimension / 8;
                int pixelHeight = minimumDimension / 8;
                g.fillRect(offsetX + x * pixelWidth, offsetY +  y * pixelHeight, pixelWidth, pixelHeight);
            }
        }

        paintGrid(g);
    }

    void doMousePaint(java.awt.event.MouseEvent e) {
        int x = (e.getX() * 8) / getWidth();
        int y = (e.getY() * 8) / getHeight();
        if (SwingUtilities.isLeftMouseButton(e))
        	setColor(x, y, color);
        else if(SwingUtilities.isRightMouseButton(e))
        	setColor(x, y, rightColor);
        tileChanged();
    }

    void setColor(int x, int y, int color) {
    	font.setTilePixel(selectedTile, x, y, color);
    }
    
    void setDirectPixel(int tile, int x, int y, int color)
    {
    	font.setTilePixel(tile, x, y, color);
    }
    
    int getDirectPixel(int x, int y)
    {
    	return font.getPixel(x, y);
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {}
    public void mouseExited(java.awt.event.MouseEvent e) {}
    public void mouseReleased(java.awt.event.MouseEvent e) {}
    public void mousePressed(java.awt.event.MouseEvent e) {}
    public void mouseClicked(java.awt.event.MouseEvent e) {
        doMousePaint(e);
    }
    public void mouseMoved(java.awt.event.MouseEvent e) {}
    public void mouseDragged(java.awt.event.MouseEvent e) {
        doMousePaint(e);
    }

    void setColor(int color) {
        assert color >= 1 && color <= 3;
        this.color = color;
    }

    void setRightColor(int color) {
        assert color >= 1 && color <= 3;
        this.rightColor = color;
    }

    void setTileChangedListener(TileChangedListener l) {
        tileChangedListener = l;
    }

    void copyTile() {
        if (clipboard == null) {
            clipboard = new int[8][8];
        }
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                clipboard[x][y] = getColor(selectedTile, x, y);
            }
        }
    }

    void generateShadedAndInvertedTiles() {
    	font.generateShadedAndInvertedTiles();
    }

    void pasteTile() {
        if (clipboard == null) {
            return;
        }
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                int c = clipboard[x][y];
                if (c < 3) {
                    ++c;  // Adjusts from Gameboy color to editor color.
                }
                setColor(x, y, c);
            }
        }
        tileChanged();
    }

    void tileChanged() {
        repaint();
        generateShadedAndInvertedTiles();
        tileChangedListener.tileChanged();
    }
}
