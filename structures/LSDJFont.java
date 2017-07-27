package structures;

/**
 * Helper class to access and manipulate font data.
 * @author Eiyeron
 *
 */
public class LSDJFont {
	public static final int FONT_NUM_TILES_X = 8;
	public static final int FONT_NUM_TILES_Y = 9;
	public static final int FONT_MAP_WIDTH = FONT_NUM_TILES_X * 8;
	public static final int FONT_MAP_HEIGHT = FONT_NUM_TILES_Y * 9;

	private byte romImage[] = null;
	private int fontOffset = -1;

	public LSDJFont() {
	}

	public void setRomImage(byte romImage[]) {
		this.romImage = romImage;
	}

	public void setFontOffset(int fontOffset) {
		this.fontOffset = fontOffset;
	}

	public int getPixel(int x, int y) {
		if (x < 0 || x >= FONT_MAP_WIDTH || y < 0 || y >= FONT_MAP_HEIGHT)
			return -1;

		int tileToRead = (y / 8) * 8 + x / 8;
		int tileOffset = fontOffset + tileToRead * 16 + (y % 8) * 2;
		int xMask = 7 - (x % 8);
		int value = (romImage[tileOffset] >> xMask) & 1;
		value |= ((romImage[tileOffset + 1] >> xMask) & 1) << 1;
		return value;
	}

	public int getTilePixel(int tile, int localX, int localY) {
		return getPixel((tile % FONT_NUM_TILES_X) * 8 + (localX % 8), (tile / FONT_NUM_TILES_X) * 8 + (localY % 8));
	}

	public void setPixel(int x, int y, int color) {
		assert color >= 1 && color <= 3;
		if (x < 0 || x >= FONT_MAP_WIDTH || y < 0 || y >= FONT_MAP_HEIGHT)
			return;
		int localX = x % 8;
		int localY = y % 8;
		int tileToEdit = (y / 8) * 8 + x / 8;

		int tileOffset = fontOffset + tileToEdit * 16 + localY * 2;
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
		setPixel((tile % FONT_NUM_TILES_X) * 8 + (localX % 8), (tile / FONT_NUM_TILES_X) * 8 + (localY % 8), color);
	}

	public void shiftUp(int tile) {
		int tileOffset = fontOffset + tile * 16;
		byte line0origin1 = romImage[tileOffset];
		byte line0origin2 = romImage[tileOffset + 1];
		for (int i = 0; i < 8; i++) {
			int lineTargetOffset = fontOffset + tile * 16 + i * 2;
			int lineOriginOffset = fontOffset + tile * 16 + (i + 1 % 8) * 2;
			romImage[lineTargetOffset] = romImage[lineOriginOffset];
			romImage[lineTargetOffset + 1] = romImage[lineOriginOffset + 1];
		}
		romImage[tileOffset + 7 * 2] = line0origin1;
		romImage[tileOffset + 7 * 2 + 1] = line0origin2;
	}

	public void shiftDown(int tile) {
		int tileOffset = fontOffset + tile * 16;
		byte line7origin1 = romImage[tileOffset + 7 * 2];
		byte line7origin2 = romImage[tileOffset + 7 * 2 + 1];
		for (int i = 7; i > 0; i--) {
			int lineTargetOffset = fontOffset + tile * 16 + i * 2;
			int lineOriginOffset = fontOffset + tile * 16 + (i - 1) * 2;
			romImage[lineTargetOffset] = romImage[lineOriginOffset];
			romImage[lineTargetOffset + 1] = romImage[lineOriginOffset + 1];
		}
		romImage[tileOffset] = line7origin1;
		romImage[tileOffset + 1] = line7origin2;
	}

	public void shiftRight(int tile) {
		int tileOffset = fontOffset + tile * 16;
		for (int i = 0; i < 16; i++) {
			romImage[tileOffset
					+ i] = (byte) (((romImage[tileOffset + i] & 1) << 7) | (romImage[tileOffset + i] >> 1) & 0x7F);
		}
	}

	public void shiftLeft(int tile) {
		int tileOffset = fontOffset + tile * 16;
		for (int i = 0; i < 16; i++) {
			romImage[tileOffset
					+ i] = (byte) (((romImage[tileOffset + i] & 0x80) >> 7) | (romImage[tileOffset + i] << 1));
		}
	}

	/**
	 * I don't know its usage, but I guess that's designed to make inverted tiles
	 * for highlight and misc.
	 */
	public void generateShadedAndInvertedTiles() {
		int tilesToCopy = 69;
		for (int i = 0; i < tilesToCopy * 16; i += 2) {
			int src = i + fontOffset + 2 * 16; // The two first tiles are not mirrored.
			int inverted = src + 0x4d2;
			int shaded = inverted + 0x4d2;

			// Shaded.
			romImage[shaded] = (byte) 0xff;
			romImage[shaded + 1] = romImage[src + 1];

			// Inverted.
			// lsb 1, msb 1 => lsb 0, msb 0
			// lsb 0, msb 0 => lsb 1, msb 1
			// lsb 1, msb 0 => lsb 1, msb 0
			romImage[inverted] = (byte) ~romImage[src + 1];
			romImage[inverted + 1] = (byte) ~romImage[src];
		}
	}
}
