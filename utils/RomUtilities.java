package utils;

public class RomUtilities {
    public static final int BANK_COUNT = 64;
    public static final int BANK_SIZE = 0x4000;
	
	
	public static int findFontOffset(byte[] romImage) {
		int i = 30 * 0x4000; // Bank 30.
		while (i < 31 * 0x4000) {
			// Looks for the end of graphics font.
			if (romImage[i] == 0 && romImage[i + 1] == 0 && romImage[i + 2] == 0 && romImage[i + 3] == 0
					&& romImage[i + 4] == (byte) 0xd0 && romImage[i + 5] == (byte) 0x90 && romImage[i + 6] == 0x50
					&& romImage[i + 7] == 0x50 && romImage[i + 8] == 0x50 && romImage[i + 9] == 0x50
					&& romImage[i + 10] == 0x50 && romImage[i + 11] == 0x50 && romImage[i + 12] == (byte) 0xd0
					&& romImage[i + 13] == (byte) 0x90 && romImage[i + 14] == 0 && romImage[i + 15] == 0) {
				return i + 16;
			}
			++i;
		}
		return -1;
	}
	
	public static int findNameOffset(byte[] romImage) {
		// Palette names are in bank 27.
		int i = 0x4000 * 27;
		while (i < 0x4000 * 28) {
			if (romImage[i] == 'G' && // gray
					romImage[i + 1] == 'R' && romImage[i + 2] == 'A' && romImage[i + 3] == 'Y' && romImage[i + 4] == 0
					&& romImage[i + 5] == 'I' && // inv
					romImage[i + 6] == 'N' && romImage[i + 7] == 'V' && romImage[i + 8] == ' ' && romImage[i + 9] == 0
					&& romImage[i + 10] == 0 && // empty
					romImage[i + 11] == 0 && romImage[i + 12] == 0 && romImage[i + 13] == 0 && romImage[i + 14] == 0
					&& romImage[i + 15] == 0 && // empty
					romImage[i + 16] == 0 && romImage[i + 17] == 0 && romImage[i + 18] == 0 && romImage[i + 19] == 0
					&& romImage[i + 20] == 0 && // empty
					romImage[i + 21] == 0 && romImage[i + 22] == 0 && romImage[i + 23] == 0 && romImage[i + 24] == 0
					&& romImage[i + 25] == 0 && // empty
					romImage[i + 26] == 0 && romImage[i + 27] == 0 && romImage[i + 28] == 0 && romImage[i + 29] == 0) {
				return i - 15;
			}
			++i;
		}
		return -1;
	}
	
	public static String getFontName(byte[] romImage, int font) {
		int fontNameSize = 5;
		int nameOffset = findNameOffset(romImage);
		String s = new String();
		s = s + (char) romImage[nameOffset + font * fontNameSize + 0];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 1];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 2];
		s = s + (char) romImage[nameOffset + font * fontNameSize + 3];
		return s;
	}

	public static void setFontName(byte[] romImage, int fontIndex, String fontName) {
		while (fontName.length() < 4) {
			fontName += " ";
		}
		int fontNameSize = 5;
		int nameOffset = findNameOffset(romImage);
		romImage[nameOffset + fontIndex * fontNameSize + 0] = (byte) fontName.charAt(0);
		romImage[nameOffset + fontIndex * fontNameSize + 1] = (byte) fontName.charAt(1);
		romImage[nameOffset + fontIndex * fontNameSize + 2] = (byte) fontName.charAt(2);
		romImage[nameOffset + fontIndex * fontNameSize + 3] = (byte) fontName.charAt(3);
	}	
}
