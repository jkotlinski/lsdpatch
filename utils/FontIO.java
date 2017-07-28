package utils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import structures.LSDJFont;

public class FontIO {
	
	public static String loadFnt(File file, byte[] array) throws IOException {
		return loadFnt(file, array, 0);
	}

	public static String loadFnt(File file, byte[] array, int arrayOffset) throws IOException {
		String name = new String();
		int bytesPerTile = 16;
        int fontSize = LSDJFont.TILE_COUNT * bytesPerTile;
        java.io.RandomAccessFile f = new java.io.RandomAccessFile(file, "r");
        name += (char)f.read();
        name += (char)f.read();
        name += (char)f.read();
        name += (char)f.read();
        for (int i = 0; i < fontSize; ++i) {
        	array[i + arrayOffset] = (byte)f.read();
        }
        f.close();
        return name;
    }

    public static void saveFnt(File file, String fontName, byte[] array) throws IOException {
    	saveFnt(file, fontName, array, 0);
    }
    
    public static void saveFnt(File file, String fontName, byte[] array, int arrayOffset) throws IOException {
            FileOutputStream f = new java.io.FileOutputStream(file);
            f.write(fontName.charAt(0));
            f.write(fontName.charAt(1));
            f.write(fontName.charAt(2));
            f.write(fontName.charAt(3));
            int bytesPerTile = 16;
            int fontSize = LSDJFont.TILE_COUNT * bytesPerTile;
            for (int i = 0; i < fontSize; ++i) {
                f.write(array[i + arrayOffset]);
            }
            f.close();
    }

}
