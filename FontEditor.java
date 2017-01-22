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

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Color;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class FontEditor extends JFrame {

	private JPanel contentPane;

    private byte romImage[] = null;
    private int fontOffset = -1;
    private int nameOffset = -1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FontEditor frame = new FontEditor();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FontEditor() {
		setTitle("Font Editor");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 415, 324);
        setResizable(false);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save...");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel fontMap = new JPanel();
		fontMap.setBounds(10, 42, 128, 126);
		contentPane.add(fontMap);

		JPanel charEditor = new JPanel();
		charEditor.setBounds(148, 11, 240, 240);
		contentPane.add(charEditor);

		JComboBox fontSelector = new JComboBox();
		fontSelector.setBounds(10, 11, 128, 20);
		contentPane.add(fontSelector);

		JRadioButton color1 = new JRadioButton("1");
		color1.setSelected(true);
		color1.setBounds(10, 200, 37, 23);
		contentPane.add(color1);

		JRadioButton color2 = new JRadioButton("2");
		color2.setBounds(49, 200, 37, 23);
		contentPane.add(color2);

		JRadioButton color3 = new JRadioButton("3");
		color3.setBounds(88, 200, 37, 23);
		contentPane.add(color3);

		JLabel lblColor = new JLabel("Color:");
		lblColor.setBounds(10, 179, 46, 14);
		contentPane.add(lblColor);
	}

    private int findFontOffset() {
        int i = 30 * 0x4000;  // Bank 30.
        while (i < 31 * 0x4000) {
            // Looks for the end of graphics font.
            if (romImage[i] == 0 &&
                    romImage[i + 1] == 0 &&
                    romImage[i + 2] == 0 &&
                    romImage[i + 3] == 0 &&
                    romImage[i + 4] == (byte)0xd0 &&
                    romImage[i + 5] == (byte)0x90 &&
                    romImage[i + 6] == 0x50 &&
                    romImage[i + 7] == 0x50 &&
                    romImage[i + 8] == 0x50 &&
                    romImage[i + 9] == 0x50 &&
                    romImage[i + 10] == 0x50 &&
                    romImage[i + 11] == 0x50 &&
                    romImage[i + 12] == (byte)0xd0 &&
                    romImage[i + 13] == (byte)0x90 &&
                    romImage[i + 14] == 0 &&
                    romImage[i + 15] == 0) {
                return i + 16;
            }
            ++i;
        }
        return -1;
    }

    private int findNameOffset() {
        // Palette names are in bank 27.
        int i = 0x4000 * 27;
        while (i < 0x4000 * 28) {
            if (romImage[i] == 'G' &&  // gray
                    romImage[i + 1] == 'R' &&
                    romImage[i + 2] == 'A' &&
                    romImage[i + 3] == 'Y' &&
                    romImage[i + 4] == 0 &&
                    romImage[i + 5] == 'I' &&  // inv
                    romImage[i + 6] == 'N' &&
                    romImage[i + 7] == 'V' &&
                    romImage[i + 8] == ' ' &&
                    romImage[i + 9] == 0 &&
                    romImage[i + 10] == 0 &&  // empty
                    romImage[i + 11] == 0 &&
                    romImage[i + 12] == 0 &&
                    romImage[i + 13] == 0 &&
                    romImage[i + 14] == 0 &&
                    romImage[i + 15] == 0 &&  // empty
                    romImage[i + 16] == 0 &&
                    romImage[i + 17] == 0 &&
                    romImage[i + 18] == 0 &&
                    romImage[i + 19] == 0 &&
                    romImage[i + 20] == 0 &&  // empty
                    romImage[i + 21] == 0 &&
                    romImage[i + 22] == 0 &&
                    romImage[i + 23] == 0 &&
                    romImage[i + 24] == 0 &&
                    romImage[i + 25] == 0 &&  // empty
                    romImage[i + 26] == 0 &&
                    romImage[i + 27] == 0 &&
                    romImage[i + 28] == 0 &&
                    romImage[i + 29] == 0) {
                        return i - 15;
                    }
            ++i;
        }
        return -1;
    }

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
        fontOffset = findFontOffset();
        if (fontOffset == -1) {
            System.err.println("Could not find font offset!");
        }
        nameOffset = findNameOffset();
        if (nameOffset == -1) {
            System.err.println("Could not find font name offset!");
        }
        // populateFontSelector();  // Needs to be done first.
    }
}
