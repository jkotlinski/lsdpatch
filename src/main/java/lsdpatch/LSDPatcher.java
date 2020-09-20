package lsdpatch;/* Copyright (C) 2001-2011 by Johan Kotlinski

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

import utils.CommandLineFunctions;
import utils.GlobalHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class LSDPatcher {
    public static String getVersion() {
        String version = GlobalHolder.class.getPackage().getImplementationVersion();
        if (version == null) {
            return "DEV";
        }
        return version;
    }

    private static void initUi() {
        JFrame frame = new MainMenu();
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

    private static void usage() {
        System.out.printf("LSDJPatcher v%s\n\n", getVersion());
        System.out.println("java -jar LSDJPatcher.jar");
        System.out.println(" Opens the GUI.\n");

        System.out.println("java -jar LSDJPatcher.jar fnt2png <fntfile> <pngfile>");
        System.out.println(" Exports the font file into a PNG\n");

        System.out.println("java -jar LSDJPatcher.jar png2fnt <font title> <pngfile> <fntfile>");
        System.out.println(" Converts the PNG into a font with given name.\n");

        System.out.println("java -jar LSDJPatcher.jar romfnt2png <romFile> <fontIndex>");
        System.out.println(" Extracts the nth font from the given rom into a png named like the font.\n");

        System.out.println("java -jar LSDJPatcher.jar png2romfnt <romFile> <pngfile> <index> <fontname>");
        System.out.println(" Imports the PNG into the rom with given name.\n");

        System.out.println("java -jar LSDJPatcher.jar clone <inRomFile> <outRomlFile>");
        System.out.println(" Clones all customizations from a ROM file to another.\n");

    }

    public static void main(String[] args) {
        if (args.length >= 1) {
            processArguments(args);
            return;
        }
        try {
            // Use the system's UI look when applicable
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            // Use font anti-aliasing when applicable
            System.setProperty("awt.useSystemAAFontSettings","on");
            System.setProperty("swing.aatext", "true");
            useJLabelFontForMenus();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Preferences preferences = Preferences.userRoot().node(LSDPatcher.class.getName());
        GlobalHolder.set(preferences, Preferences.class);
        preferences.put("path", preferences.get("path", System.getProperty("user.dir")));

        initUi();
    }

    private static void useJLabelFontForMenus() {
        // On some systems, the default font given to menus is a bit wonky with anti-aliasing. Using the one given
        // to JLabels will give a better result.
        Font systemFont = new JLabel().getFont();
        HashMap<TextAttribute, Object> attributes = new HashMap<>(systemFont.getAttributes());
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
        attributes.put(TextAttribute.SIZE, systemFont.getSize());
        Font selectedFont = Font.getFont(attributes);
        UIManager.put("Menu.font", selectedFont);
        UIManager.put("MenuBar.font", selectedFont);
        UIManager.put("MenuItem.font", selectedFont);
    }

    private static void processArguments(String[] args) {
        String command = args[0].toLowerCase();

        if (command.compareTo("fnt2png") == 0 && args.length == 3) {
            CommandLineFunctions.fontToPng(args[1], args[2]);
        } else if (command.compareTo("png2fnt") == 0 && args.length == 4) {
            CommandLineFunctions.pngToFont(args[1], args[2], args[3]);
        } else if (command.compareTo("romfnt2png") == 0 && args.length == 3) {
            // -1 to allow 1-3 range instead of 0-2
            CommandLineFunctions.extractFontToPng(args[1], Integer.parseInt(args[2]) - 1);
        } else if (command.compareTo("png2romfnt") == 0 && args.length == 5) {
            // -1 to allow 1-3 range instead of 0-2
            CommandLineFunctions.loadPngToRom(args[1], args[2], Integer.parseInt(args[3]) - 1, args[4]);
        } else if (command.compareTo("clone") == 0 && args.length == 3) {
            // -1 to allow 1-3 range instead of 0-2
            CommandLineFunctions.copyAllCustomizations(args[1], args[2]);
        }

        usage();

    }

}
