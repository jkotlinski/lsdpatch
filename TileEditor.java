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
import javax.swing.SwingUtilities;

import java.awt.Graphics;

class TileEditor extends JPanel implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {
    public interface TileChangedListener {
        void tileChanged();
    }

    byte[] romImage = null;
    int fontOffset = -1;
    int selectedTile = 0;
    int color = 3;
    int rightColor = 3;

    int clipboard[][] = null;

    TileChangedListener tileChangedListener;

    TileEditor() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

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
    
    int getTile()
    {
    	return selectedTile;
    }
    
    void shiftUp(int tile)
    {
        int tileOffset = fontOffset + tile * 16;
        byte line0origin1 = romImage[tileOffset];
        byte line0origin2 = romImage[tileOffset+1];
        for(int i = 0; i < 8; i++)
        {
            int lineTargetOffset = fontOffset + tile * 16 + i*2;
            int lineOriginOffset = fontOffset + tile * 16 + (i+1%8)*2;
            romImage[lineTargetOffset] = romImage[lineOriginOffset];
            romImage[lineTargetOffset+1] = romImage[lineOriginOffset+1];
        }
        romImage[tileOffset+7*2] = line0origin1;
        romImage[tileOffset+7*2+1] = line0origin1;
    	tileChanged();
    }
    
    void shiftDown(int tile)
    {
        int tileOffset = fontOffset + tile * 16;
        byte line7origin1 = romImage[tileOffset + 7*2];
        byte line7origin2 = romImage[tileOffset + 7*2 + 1];
        for(int i = 7; i > 0; i--)
        {
            int lineTargetOffset = fontOffset + tile * 16 + i*2;
            int lineOriginOffset = fontOffset + tile * 16 + (i-1)*2;
            romImage[lineTargetOffset] = romImage[lineOriginOffset];
            romImage[lineTargetOffset+1] = romImage[lineOriginOffset+1];
        }
        romImage[tileOffset] = line7origin1;
        romImage[tileOffset+1] = line7origin2;
    	tileChanged();
    }
    
    void shiftRight(int tile)
    {
        int tileOffset = fontOffset + tile * 16;
        for(int i = 0; i < 16; i++)
        {
        	romImage[tileOffset+i] = (byte) (((romImage[tileOffset+i]&1)<<7) | (romImage[tileOffset+i]>>1)&0b01111111);
        }
    	tileChanged();
    }
    
    void shiftLeft(int tile)
    {
    	int tileOffset = fontOffset + tile * 16;
    	for(int i = 0; i < 16; i++)
    	{
    		romImage[tileOffset+i] = (byte) (((romImage[tileOffset+i]&0b10000000)>>7) | (romImage[tileOffset+i]<<1));
    	}
    	tileChanged();
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

    private void paintGrid(Graphics g) {
        g.setColor(java.awt.Color.gray);
        int dx = getWidth() / 8;
        for (int x = dx; x < getWidth(); x += dx) {
            g.drawLine(x, 0, x, getHeight());
        }

        int dy = getHeight() / 8;
        for (int y = dy; y < getHeight(); y += dy) {
            g.drawLine(0, y, getWidth(), y);
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
        if (x < 0 || y < 0 || x > 7 || y > 7) {
            return;
        }
        assert color >= 1 && color <= 3;
        int tileOffset = fontOffset + selectedTile * 16 + y * 2;
        int xMask = 0x80 >> x;
        romImage[tileOffset] &= 0xff ^ xMask;
        romImage[tileOffset + 1] &= 0xff ^ xMask;
        switch (color) {
            case 3:
                romImage[tileOffset + 1] |= xMask;
            case 2:
                romImage[tileOffset] |= xMask;
        }
    }
    
    void setDirectPixel(int tile, int x, int y, int color)
    {
    	System.err.print(color);
        if (x < 0 || y < 0 || x > 7 || y > 7) {
        	System.err.println("Out of bounds : "+x+";"+y);
        	return;
        }
        assert color >= 1 && color <= 3;
        int tileOffset = fontOffset + tile * 16 + y * 2;
        int xMask = 0x80 >> x;
        romImage[tileOffset] &= 0xff ^ xMask;
        romImage[tileOffset + 1] &= 0xff ^ xMask;
        switch (color) {
            case 3:
                romImage[tileOffset + 1] |= xMask;
            case 2:
                romImage[tileOffset] |= xMask;
        }    	
    }
    
    int getDirectPixel(int x, int y)
    {
		int tileToRead = (y/8) * 8 + x/8;
        int tileOffset = fontOffset + tileToRead * 16 + (y%8) * 2;
        int xMask = 7 - (x%8);
        int value = (romImage[tileOffset] >> xMask) & 1;
        value |= ((romImage[tileOffset + 1] >> xMask) & 1) << 1;
        return value;
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
        int tilesToCopy = 69;
        for (int i = 0; i < tilesToCopy * 16; i += 2) {
            int src = i + fontOffset + 2 * 16;  // The two first tiles are not mirrored.
            int inverted = src + 0x4d2;
            int shaded = inverted + 0x4d2;

            // Shaded.
            romImage[shaded] = (byte)0xff;
            romImage[shaded + 1] = romImage[src + 1];

            // Inverted.
            // lsb 1, msb 1 => lsb 0, msb 0
            // lsb 0, msb 0 => lsb 1, msb 1
            // lsb 1, msb 0 => lsb 1, msb 0
            romImage[inverted] = (byte)~romImage[src + 1];
            romImage[inverted + 1] = (byte)~romImage[src];
        }
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
