package structures;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Helper class to access and manipulate font data.
 * This class acts as a span over data owned elsewhere and acts in it as it was its own.
 * @author Eiyeron
 */
public class LSDJFont extends ROMDataManipulator {
    public static final int TILE_COUNT = 71;
    public static final int GFX_TILE_COUNT = 46;
    public static final int FONT_NUM_TILES_X = 8;
    public static final int FONT_NUM_TILES_Y = (int)Math.ceil(TILE_COUNT/ (float)FONT_NUM_TILES_X);
    public static final int GFX_FONT_NUM_TILES_Y = (int)Math.ceil((TILE_COUNT + GFX_TILE_COUNT)/ (float)FONT_NUM_TILES_X);
    public static final int FONT_MAP_WIDTH = FONT_NUM_TILES_X * 8;
    public static final int FONT_MAP_HEIGHT = FONT_NUM_TILES_Y * 8;
    public static final int GFX_FONT_MAP_HEIGHT = (GFX_FONT_NUM_TILES_Y) * 8;
    public static final int FONT_HEADER_SIZE = 130;
    public static final int FONT_COUNT = 3;
    public static final int FONT_SIZE = 0xe96;
    public static final int FONT_NAME_LENGTH = 4;
    public static final int FONT_TILE_SIZE = 16;

    private int gfxDataOffset = -1;

    public void setGfxDataOffset(int gfxDataOffset) {
        this.gfxDataOffset = gfxDataOffset;
    }

    private int getTileDataLocation(int index) {
        if (index >= TILE_COUNT) {
            index -= TILE_COUNT;
            return getGfxTileDataLocation(index);
        }
        if (index < 0 || index >= TILE_COUNT)
        {
            // TODO exception?
            return -1;
        }
        return getDataOffset() + index * FONT_TILE_SIZE;
    }

    private int getGfxTileDataLocation(int index) {
        if (index < 0 || index >= GFX_TILE_COUNT)
        {
            // TODO exception?
            return -1;
        }
        return gfxDataOffset + index * FONT_TILE_SIZE;
    }

    public int getPixel(int x, int y) {
        if (x < 0 || x >= FONT_MAP_WIDTH || y < 0 || y >= GFX_FONT_MAP_HEIGHT)
            return -1;

        int tileToRead = (y / 8) * 8 + x / 8;
        int tileOffset = getTileDataLocation(tileToRead) + (y % 8) * 2;
        int xMask = 7 - (x % 8);
        int value = (romImage[tileOffset] >> xMask) & 1;
        value |= ((romImage[tileOffset + 1] >> xMask) & 1) << 1;
        return value;
    }
    // - Tile data manipulation -
    // Note : those functions only affect the normal variant tileset.
    // In the future it might be good to either provide alternative functions
    // or to extend them to allow editing the other variants too.

    public int getTilePixel(int tile, int localX, int localY) {
        return getPixel((tile % FONT_NUM_TILES_X) * 8 + (localX % 8), (tile / FONT_NUM_TILES_X) * 8 + (localY % 8));
    }

    private void setPixel(int x, int y, int color) {
        assert color >= 1 && color <= 3;
        if (x < 0 || x >= FONT_MAP_WIDTH || y < 0 || y >= GFX_FONT_MAP_HEIGHT)
            return;
        int localX = x % 8;
        int localY = y % 8;
        int tileToEdit = (y / 8) * 8 + x / 8;

        int tileOffset = getTileDataLocation(tileToEdit) + localY * 2;
        int xMask = 0x80 >> localX;
        romImage[tileOffset] &= 0xff ^ xMask;
        romImage[tileOffset + 1] &= 0xff ^ xMask;
        switch (color) {
            case 3:
                romImage[tileOffset + 1] |= xMask;
            case 2:
                romImage[tileOffset] |= xMask;
        }
    }

    public void setTilePixel(int tile, int localX, int localY, int color) {
        setPixel((tile % FONT_NUM_TILES_X) * 8 + (localX % 8),
                (tile / FONT_NUM_TILES_X) * 8 + (localY % 8), color);
    }

    public void rotateTileUp(int tile) {
        int tileOffset = getTileDataLocation(tile);
        byte line0origin1 = romImage[tileOffset];
        byte line0origin2 = romImage[tileOffset + 1];
        for (int i = 0; i < 8; i++) {
            int lineTargetOffset = tileOffset + i * 2;
            int lineOriginOffset = tileOffset + (i + 1 % 8) * 2;
            romImage[lineTargetOffset] = romImage[lineOriginOffset];
            romImage[lineTargetOffset + 1] = romImage[lineOriginOffset + 1];
        }
        romImage[tileOffset + 7 * 2] = line0origin1;
        romImage[tileOffset + 7 * 2 + 1] = line0origin2;
    }

    public void rotateTileDown(int tile) {
        int tileOffset = getTileDataLocation(tile);
        byte line7origin1 = romImage[tileOffset + 7 * 2];
        byte line7origin2 = romImage[tileOffset + 7 * 2 + 1];
        for (int i = 7; i > 0; i--) {
            int lineTargetOffset = tileOffset + i * 2;
            int lineOriginOffset = tileOffset + (i - 1) * 2;
            romImage[lineTargetOffset] = romImage[lineOriginOffset];
            romImage[lineTargetOffset + 1] = romImage[lineOriginOffset + 1];
        }
        romImage[tileOffset] = line7origin1;
        romImage[tileOffset + 1] = line7origin2;
    }

    public void rotateTileRight(int tile) {
        int tileOffset = getTileDataLocation(tile);
        for (int i = 0; i < FONT_TILE_SIZE; i++) {
            byte currentByte = romImage[tileOffset + i];
            byte shiftedByte = (byte) (((currentByte & 1) << 7) | ((currentByte >> 1) & 0x7F));
            romImage[tileOffset + i] = shiftedByte;
        }
    }

    public void rotateTileLeft(int tile) {
        int tileOffset = getTileDataLocation(tile);
        for (int i = 0; i < FONT_TILE_SIZE; i++) {
            byte currentByte = romImage[tileOffset + i];
            byte shiftedByte = (byte) (((currentByte & 0x80) >> 7) | (currentByte << 1));
            romImage[tileOffset + i] = shiftedByte;
        }
    }


    /**
     * Generates the inverted and shaded font variants from the normal tileset.
     */
    public void generateShadedAndInvertedTiles() {
        for (int i = 2; i < TILE_COUNT; i++) {
            generateShadedTileVariant(i);
            generateInvertedTileVariant(i);
        }
    }

    public void generateInvertedTileVariant(int index) {
        if (index < 2 || index > TILE_COUNT) {
            // TODO exception?
            return;
        }
        int sourceLocation = getTileDataLocation(index); // The two first tiles are not mirrored.
        int invertedLocation = sourceLocation + 0x4d2;
        for (int i = 0; i < FONT_TILE_SIZE; i += 2) {
            romImage[invertedLocation + i] = (byte) ~romImage[sourceLocation + i + 1];
            romImage[invertedLocation + i + 1] = (byte) ~romImage[sourceLocation + i];
        }
    }


    public void generateShadedTileVariant(int index) {
        if (index < 2 || index > TILE_COUNT) {
            // TODO exception?
            return;
        }
        int sourceLocation = getTileDataLocation(index); // The two first tiles are not mirrored.
        int shadedLocation = sourceLocation + 0x4d2 * 2;
        for (int i = 0; i < FONT_TILE_SIZE; i += 2) {
            int sourceByte = romImage[sourceLocation + i];
            if (i % 4 == 2) {
                romImage[shadedLocation + i] = (byte)(sourceByte | 0xaa);
            } else {
                romImage[shadedLocation + i] = (byte)(sourceByte | 0x55);
            }
            romImage[shadedLocation + i + 1] = romImage[sourceLocation + i + 1];
        }
    }

    private int grayIndexToColor(int index) {
        switch (index) {
            case 0 :
                return 0xFFFFFF;
            case 1 :
                return 0x969696;
            case 2 :
                return 0x808080;
            case 3 :
                return 0x000000;
            default:
                return 0xDeadBeef;
        }
    }

    public String loadImageData(String name, BufferedImage image) {
        int numTiles = image.getHeight()/8 * image.getWidth()/8;
        // Limiting to either loading text tiles or load all tiles. No partial graphical tiles loading.
        int maxTileIndex = numTiles < LSDJFont.GFX_TILE_COUNT + LSDJFont.TILE_COUNT ? LSDJFont.TILE_COUNT :LSDJFont.TILE_COUNT + LSDJFont.GFX_TILE_COUNT;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int currentTileIndex = (y / 8) * 8 + x / 8;
                if (currentTileIndex >= maxTileIndex) break;
                int rgb = image.getRGB(x, y);
                float[] color = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
                int lum = (int) (color[2] * 255);

                int col = 0;
                if (lum >= 192)
                    col = 1;
                else if (lum >= 64)
                    col = 2;
                else if (lum >= 0)
                    col = 3;
                setTilePixel(currentTileIndex, x%8, y%8, col);
            }
        }
        StringBuilder sub;
        if (name.length() < 4) {
            sub = new StringBuilder(name);
            for (int i = 0; i < 4 - sub.length(); i++)
                sub.append(" ");
        } else
            sub = new StringBuilder(name.substring(0, 4));
        return sub.toString();
    }

    public BufferedImage saveDataToImage(Boolean includeGfxCharacters) {
        BufferedImage image = new BufferedImage(LSDJFont.FONT_MAP_WIDTH, includeGfxCharacters ? LSDJFont.GFX_FONT_MAP_HEIGHT : LSDJFont.FONT_MAP_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        int tileCount = includeGfxCharacters ? TILE_COUNT + GFX_TILE_COUNT : TILE_COUNT;
        for (int tile = 0; tile < tileCount; ++tile) {
            int baseX = (tile %  FONT_NUM_TILES_X)*8;
            int baseY = (tile / FONT_NUM_TILES_X)*8;
            for (int y = 0; y < 8; ++y) {
                for (int x = 0; x < 8; ++x) {
                    int colorIndex = getTilePixel(tile, x, y);
                    image.setRGB(baseX + x, baseY + y, grayIndexToColor(colorIndex));
                }
            }
        }
        return image;
    }
}
