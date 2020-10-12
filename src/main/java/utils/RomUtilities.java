package utils;

import structures.LSDJFont;

public class RomUtilities {
    public static final int BANK_COUNT = 64;
    public static final int BANK_SIZE = 0x4000;

    public static final int COLOR_SET_SIZE = 4 * 2;  // one color set contains 4 colors
    public static final int NUM_COLOR_SETS = 5;  // one palette contains 5 color sets
    public static final int PALETTE_SIZE = COLOR_SET_SIZE * NUM_COLOR_SETS;
    public static final int PALETTE_NAME_SIZE = 5;

    private static int findGrayscalePaletteNames(byte[] romImage)
    {
        for (int i = 0x4000 * 27;i < 0x4000 * 28; ++i) {
            if (romImage[i] != 0 &&
                    romImage[i + 1] != 0 &&
                    romImage[i + 2] != 0 &&
                    romImage[i + 3] != 0 &&
                    romImage[i + 4] == 0 &&
                    romImage[i + 5] != 0 &&
                    romImage[i + 6] != 0 &&
                    romImage[i + 7] != 0 &&
                    romImage[i + 8] != 0 &&
                    romImage[i + 9] == 0 &&
                    romImage[i + 10] != 0 &&
                    romImage[i + 11] != 0 &&
                    romImage[i + 12] != 0 &&
                    romImage[i + 13] != 0 &&
                    romImage[i + 14] == 0)
            {
                return i + 15;
            }
        }
        return -1;
    }

    private static int findScreenBackgroundData(byte[] romImage)
    {
        int numPalettes = getNumberOfPalettes(romImage);
        if (numPalettes == -1)
        {
            return -1;
        }
        for (int i = 0x4000; i < 0x8000; ++i)
        {
            if (romImage[i] == 0 &&
                    romImage[i + 1] == 0 &&
                    romImage[i + 2] == 0 &&
                    romImage[i + 3] == 0 &&
                    romImage[i + 4] == 0 &&
                    romImage[i + 5] == 0 &&
                    romImage[i + 6] == 0 &&
                    romImage[i + 7] == 0 &&
                    romImage[i + 8] == 0 &&
                    romImage[i + 9] == 0 &&
                    romImage[i + 10] == 0 &&
                    romImage[i + 11] == 0 &&
                    romImage[i + 12] == 0 &&
                    romImage[i + 13] == 0 &&
                    romImage[i + 14] == 0 &&
                    romImage[i + 15] == 0 &&
                    romImage[i + 16] == 0 &&
                    romImage[i + 17] == 72 &&
                    romImage[i + 18] == 72 &&
                    romImage[i + 19] == 72)
            {
                return i;
            }
        }
        return -1;
    }

    public static int getNumberOfPalettes(byte[] romImage)
    {
        int baseOffset = findGrayscalePaletteNames(romImage);
        if (baseOffset == -1)
        {
            return -1;
        }

        int numPalettes = 0;
        for (int j = baseOffset + 4; romImage[j] == 0; j +=5)
        {
            ++numPalettes;
        }
        return numPalettes/2;
    }

    public static int findPaletteOffset(byte[] romImage) {
        // Finds the palette location by searching for the screen
        // backgrounds, which are defined directly after the palettes
        // in bank 1.
        int baseOffset = findScreenBackgroundData(romImage);
        if (baseOffset == 1)
        {
            return -1;
        }
        return baseOffset - getNumberOfPalettes(romImage) * PALETTE_SIZE;
    }

    public static int findPaletteNameOffset(byte[] romImage) {
        // Palette names are in bank 27.
        int baseOffset = findGrayscalePaletteNames(romImage);
        if (baseOffset == -1)
        {
            return -1;
        }

        return baseOffset + 5 * getNumberOfPalettes(romImage);
    }

    // Returns address of first graphics character.
    public static int findGfxFontOffset(byte[] romImage) {
        for (int i = 30 * 0x4000; i < 31 * 0x4000; ++i) {
            if (romImage[i] == 1 && romImage[i + 1] == 46 && romImage[i + 2] == 0 && romImage[i + 3] == 1) {
                return i + 2 + 8 * 16;
            }
        }
        return -1;
    }

    public static int findFontOffset(byte[] romImage) {
        int gfxOffset = findGfxFontOffset(romImage);
        int gfxCharacterCount = 46;
        int gfxCharacterSize = 16;
        return gfxOffset == -1 ? -1 : gfxOffset + gfxCharacterCount * gfxCharacterSize;
    }

    public static int findFontNameOffset(byte[] romImage) {
        // Palette names are in bank 27.
        int baseOffset = findGrayscalePaletteNames(romImage);
        if (baseOffset == -1)
        {
            return -1;
        }
        return baseOffset - 15;
    }

    public static String getFontName(byte[] romImage, int font) {
        int fontNameSize = 5;
        int nameOffset = findFontNameOffset(romImage);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < LSDJFont.FONT_NAME_LENGTH; i++) {
            s.append((char) romImage[nameOffset + font * fontNameSize + i]);
        }
        return s.toString();
    }

    public static void setFontName(byte[] romImage, int fontIndex, String fontName) {
        StringBuilder fontNameBuilder = new StringBuilder(fontName);
        while (fontNameBuilder.length() < 4) {
            fontNameBuilder.append(" ");
        }
        fontName = fontNameBuilder.toString();
        int fontNameSize = 5;
        int nameOffset = findFontNameOffset(romImage);
        for (int i = 0; i < LSDJFont.FONT_NAME_LENGTH; i++) {
            romImage[nameOffset + fontIndex * fontNameSize + i] = (byte) fontName.charAt(i);
        }
    }

    public static void fixChecksum(byte[] romImage) {
        int checksum014D = 0;
        for (int i = 0x134; i < 0x14D; ++i) {
            checksum014D = checksum014D - romImage[i] - 1;
        }
        romImage[0x14D] = (byte) (checksum014D & 0xFF);

        int checksum014E = 0;
        for (int i = 0; i < romImage.length; ++i) {
            if (i == 0x14E || i == 0x14F) {
                continue;
            }
            checksum014E += romImage[i] & 0xFF;
        }

        romImage[0x14E] = (byte) ((checksum014E & 0xFF00) >> 8);
        romImage[0x14F] = (byte) (checksum014E & 0x00FF);
    }
}
