package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import structures.LSDJFont;

public class CommandLineFunctions {
	public static void pngToFont(String name, String pngFile, String fntFile) {
		try {
			byte buffer[] = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
			BufferedImage image = ImageIO.read(new File(pngFile));
			if (image.getWidth() != LSDJFont.FONT_MAP_WIDTH && image.getHeight() != LSDJFont.FONT_MAP_HEIGHT) {
				System.err.println("Wrong size!");
				return;
			}

			LSDJFont font = new LSDJFont();
			font.setRomImage(buffer);
			font.setFontOffset(0);
			String sub = font.readImage(name, image);

			FontIO.saveFnt(new File(fntFile), sub, buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void fontToPng(String fntFile, String pngFile) {
		try {
			byte buffer[] = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
			@SuppressWarnings("unused")
			String name = FontIO.loadFnt(new File(fntFile), buffer);
			LSDJFont font = new LSDJFont();
			font.setRomImage(buffer);
			font.setFontOffset(0);
			BufferedImage image = font.createImage();
			ImageIO.write(image, "PNG", new File(pngFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void extractFontToPng(String romFileName, int numFont) {
		if (numFont < 1 || numFont > 3) {
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
			font.setFontOffset(selectedFontOffset);
			BufferedImage image = font.createImage();
			ImageIO.write(image, "PNG", new File(RomUtilities.getFontName(romImage, numFont) + ".png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadPngToRom(String romFileName, String imageFileName, int numFont, String fontName) {
		if (numFont < 1 || numFont > 3) {
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
			font.setFontOffset(selectedFontOffset);

			String correctedName = font.readImage(fontName, ImageIO.read(new File(imageFileName)));
			RomUtilities.setFontName(romImage, numFont, correctedName);
			romFile.seek(0);
			romFile.write(romImage);
			romFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
