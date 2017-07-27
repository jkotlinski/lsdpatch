
/** Copyright (C) 2001-2011 by Johan Kotlinski

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import structures.LSDJFont;
import utils.FontIO;

public class LSDPatcher {

	public LSDPatcher() {
		MainWindow frame = new MainWindow();
		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		frame.pack();
		frame.validate();

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			boolean openWindow = processArguments(args);
			if (!openWindow)
				return;
		}
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new LSDPatcher();
	}

	private static boolean processArguments(String[] args) {
		String command = args[0];
		if (command.toLowerCase().compareTo("fnt2bmp") == 0 && args.length == 3) {
			fontToBmp(args[1], args[2]);
			return false;
		} else if (command.toLowerCase().compareTo("bmp2fnt") == 0 && args.length == 4) {
			bmpToFont(args[1], args[2], args[3]);
			return false;
		}
		System.err.println("Meh");
		return false;
	}

	private static void bmpToFont(String name, String bmpFile, String fntFile) {
		try {
			byte buffer[] = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
			BufferedImage image = ImageIO.read(new File(bmpFile));
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


	private static void fontToBmp(String fntFile, String bmpFile) {
		try {
			byte buffer[] = new byte[LSDJFont.FONT_NUM_TILES_X * LSDJFont.FONT_NUM_TILES_Y * 16];
			String name = FontIO.loadFnt(new File(fntFile), buffer);
			LSDJFont font = new LSDJFont();
			font.setRomImage(buffer);
			font.setFontOffset(0);
			BufferedImage image = font.createImage();
			ImageIO.write(image, "BMP", new File(bmpFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
