package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.ImageIO;

import structures.LSDJFont;

public class CommandLineFunctions {
    public static void pngToFont(String name, String pngFile, String fntFile) {
        try {
            byte[] buffer = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
            BufferedImage image = ImageIO.read(new File(pngFile));
            if (image.getWidth() != LSDJFont.FONT_MAP_WIDTH && image.getHeight() != LSDJFont.FONT_MAP_HEIGHT) {
                System.err.println("Wrong size!");
                return;
            }

            LSDJFont font = new LSDJFont();
            font.setRomImage(buffer);
            font.setDataOffset(0);
            font.generateShadedAndInvertedTiles();
            String sub = font.loadImageData(name, image);

            FontIO.saveFnt(new File(fntFile), sub, buffer);

            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fontToPng(String fntFile, String pngFile) {
        try {
            byte[] buffer = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
            FontIO.loadFnt(new File(fntFile), buffer);
            LSDJFont font = new LSDJFont();
            font.setRomImage(buffer);
            font.setDataOffset(0);
            BufferedImage image = font.saveDataToImage();
            ImageIO.write(image, "PNG", new File(pngFile));
            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractFontToPng(String romFileName, int numFont) {
        if (numFont < 0 || numFont > 2) {
            // Already -1-ed.
            System.err.println("the font index must be comprised between 1 and 3.");
            return;
        }
        try {
            byte[] romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            RandomAccessFile romFile = new RandomAccessFile(new File(romFileName), "r");
            romFile.readFully(romImage);
            romFile.close();
            LSDJFont font = new LSDJFont();

            font.setRomImage(romImage);
            int selectedFontOffset = RomUtilities.findFontOffset(romImage) + ((numFont + 1) % 3) * LSDJFont.FONT_SIZE
                    + LSDJFont.FONT_HEADER_SIZE;
            font.setDataOffset(selectedFontOffset);
            BufferedImage image = font.saveDataToImage();
            ImageIO.write(image, "PNG", new File(RomUtilities.getFontName(romImage, numFont) + ".png"));

            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPngToRom(String romFileName, String imageFileName, int numFont, String fontName) {
        if (numFont < 0 || numFont > 2) {
            // Already -1-ed.
            System.err.println("the font index must be comprised between 1 and 3.");
            return;
        }
        try {
            byte[] romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            RandomAccessFile romFile = new RandomAccessFile(new File(romFileName), "rw");
            romFile.readFully(romImage);
            LSDJFont font = new LSDJFont();

            font.setRomImage(romImage);
            int selectedFontOffset = RomUtilities.findFontOffset(romImage) + ((numFont + 1) % 3) * LSDJFont.FONT_SIZE
                    + LSDJFont.FONT_HEADER_SIZE;
            font.setDataOffset(selectedFontOffset);
            font.generateShadedAndInvertedTiles();

            String correctedName = font.loadImageData(fontName, ImageIO.read(new File(imageFileName)));
            RomUtilities.setFontName(romImage, numFont, correctedName);
            romFile.seek(0);
            romFile.write(romImage);
            romFile.close();

            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO Merge with KitEditor's own version
    private static boolean isRomBankAKit(int bankIndex, byte[] romImage) {
        int l_offset = bankIndex * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == 0x60 && l_char_2 == 0x40);
    }

    // TODO Merge with KitEditor's own version
    private static boolean isRomBankEmpty(int bankIndex, byte[] romImage) {
        int l_offset = bankIndex * RomUtilities.BANK_SIZE;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return (l_char_1 == -1 && l_char_2 == -1);
    }
    // TODO replace KitEditor's own version with that
    private static void clearKitBank(int bankIndex, byte[] romImage) {
        int baseOffset = bankIndex * RomUtilities.BANK_SIZE;
        int endOfBank = (bankIndex + 1) * RomUtilities.BANK_SIZE;

        Arrays.fill(romImage, baseOffset, endOfBank, (byte)0xFF);
    }

    public static void copyAllCustomizations(String originFileName, String destinationFileName)
    {
        try {
            byte[] originRomFile = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            byte[] destinationRomFile = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
            RandomAccessFile originFile = new RandomAccessFile(new File(originFileName), "r");
            originFile.readFully(originRomFile);

            RandomAccessFile destinationFile = new RandomAccessFile(new File(destinationFileName), "rw");
            destinationFile.readFully(destinationRomFile);

            if (RomUtilities.getNumberOfPalettes(originRomFile) > RomUtilities.getNumberOfPalettes(destinationRomFile)) {
                System.err.println("Warning: Palettes skipped due to lack of space!");
            }

            {
                int inBaseFontOffset = RomUtilities.findFontOffset(originRomFile);
                int outBaseFontOffset = RomUtilities.findFontOffset(destinationRomFile);

                System.arraycopy(originRomFile, inBaseFontOffset, destinationRomFile, outBaseFontOffset,
                        (LSDJFont.FONT_SIZE + LSDJFont.FONT_HEADER_SIZE) * LSDJFont.FONT_COUNT);

                int inBaseFontNameOffset = RomUtilities.findFontNameOffset(originRomFile);
                int outBaseFontNameOffset = RomUtilities.findFontNameOffset(destinationRomFile);
                System.arraycopy(originRomFile, inBaseFontNameOffset, destinationRomFile, outBaseFontNameOffset,
                        LSDJFont.FONT_NAME_LENGTH * LSDJFont.FONT_COUNT);
            }

            {
                int paletteCount = Math.min(RomUtilities.getNumberOfPalettes(originRomFile),
                        RomUtilities.getNumberOfPalettes(destinationRomFile));
                int inPaletteOffset = RomUtilities.findPaletteOffset(originRomFile);
                int outPaletteOffset = RomUtilities.findPaletteOffset(destinationRomFile);
                System.arraycopy(originRomFile, inPaletteOffset, destinationRomFile, outPaletteOffset,
                        RomUtilities.PALETTE_SIZE * paletteCount);

                int inPaletteNameOffset = RomUtilities.findPaletteNameOffset(originRomFile);
                int outPaletteNameOffset = RomUtilities.findPaletteNameOffset(destinationRomFile);
                System.arraycopy(originRomFile, inPaletteNameOffset, destinationRomFile, outPaletteNameOffset,
                        RomUtilities.PALETTE_NAME_SIZE * paletteCount);
            }

            Vector<Integer> inKitsToCopy = new Vector<>();
            for (int index = 0; index < RomUtilities.BANK_COUNT; ++index) {
                if (isRomBankAKit(index, originRomFile)) {
                    inKitsToCopy.add(index);
                }
            }
            Vector<Integer> outAvailableKitSlots = new Vector<>();
            for (int index = 0; index < RomUtilities.BANK_COUNT; ++index) {
                if (isRomBankAKit(index, destinationRomFile) || isRomBankEmpty(index, destinationRomFile)) {
                    outAvailableKitSlots.add(index);
                }
            }

            if (outAvailableKitSlots.size() < inKitsToCopy.size()) {
                System.err.printf("The destination file doesn't have enough kit slots (%d < %d). Aborting.",
                        outAvailableKitSlots.size(), inKitsToCopy.size());
                return;
            }

            int numToClone = inKitsToCopy.size();
            for (int index = 0; index < numToClone; ++index)  {
                int inIndexOfKitToCopy = inKitsToCopy.get(index);
                int outIndexOfKitToOverwrite = outAvailableKitSlots.get(index);
                System.arraycopy(
                        originRomFile, inIndexOfKitToCopy * RomUtilities.BANK_SIZE,
                        destinationRomFile, outIndexOfKitToOverwrite * RomUtilities.BANK_SIZE,
                        RomUtilities.BANK_SIZE
                        );
            }
            // Cleaning the destination file
            for (int index = numToClone; index < outAvailableKitSlots.size(); ++index)  {
                clearKitBank(outAvailableKitSlots.get(index), destinationRomFile);
            }

            RomUtilities.fixChecksum(destinationRomFile);
            destinationFile.seek(0);
            destinationFile.write(destinationRomFile);
            destinationFile.close();

            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
