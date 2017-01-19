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
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import java.awt.Panel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class PaletteEditor extends JFrame {

	private JPanel contentPane;

    private byte romImage[] = null;
    private int paletteOffset = -1;
    private int nameOffset = -1;

    private int colorSetSize = 4 * 2;  // one colorset contains 4 colors
    private int paletteSize = 5 * colorSetSize;  // one palette contains 5 color sets
    private int paletteCount = 6;

    private JPanel preview1a;
    private JPanel preview1b;
    private JPanel preview2a;
    private JPanel preview2b;
    private JPanel preview3a;
    private JPanel preview3b;
    private JPanel preview4a;
    private JPanel preview4b;
    private JPanel preview5a;
    private JPanel preview5b;

    private JComboBox kitSelector;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PaletteEditor frame = new PaletteEditor();
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
	public PaletteEditor() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 500, 362);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		kitSelector = new JComboBox();
		kitSelector.setBounds(10, 10, 140, 20);
        kitSelector.setEditable(true);
		contentPane.add(kitSelector);

		JPanel previewSong = new JPanel();
		previewSong.setBounds(314, 10, 160, 144);
        previewSong.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(previewSong);

		JPanel previewProject = new JPanel();
		previewProject.setBounds(314, 164, 160, 144);
        previewProject.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(previewProject);

		JSpinner c1r1 = new JSpinner();
		c1r1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1r1.setBounds(10, 66, 36, 20);
		contentPane.add(c1r1);

		JLabel lblNormal = new JLabel("Normal");
		lblNormal.setBounds(10, 41, 46, 14);
		contentPane.add(lblNormal);

		JSpinner c1g1 = new JSpinner();
		c1g1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1g1.setBounds(56, 66, 36, 20);
		contentPane.add(c1g1);

		JSpinner c1b1 = new JSpinner();
		c1b1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1b1.setBounds(102, 66, 36, 20);
		contentPane.add(c1b1);

		preview1a = new JPanel();
		preview1a.setBounds(95, 41, 43, 14);
        preview1a.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview1a);

		preview1b = new JPanel();
		preview1b.setBounds(159, 41, 43, 14);
        preview1b.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview1b);

		JSpinner c1b2 = new JSpinner();
		c1b2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1b2.setBounds(251, 66, 36, 20);
		contentPane.add(c1b2);

		JSpinner c1g2 = new JSpinner();
		c1g2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1g2.setBounds(205, 66, 36, 20);
		contentPane.add(c1g2);

		JSpinner c1r2 = new JSpinner();
		c1r2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c1r2.setBounds(159, 66, 36, 20);
		contentPane.add(c1r2);

		JLabel lblShaded = new JLabel("Shaded");
		lblShaded.setBounds(10, 97, 46, 14);
		contentPane.add(lblShaded);

		JSpinner c2r1 = new JSpinner();
		c2r1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2r1.setBounds(10, 122, 36, 20);
		contentPane.add(c2r1);

		JSpinner c2g1 = new JSpinner();
		c2g1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2g1.setBounds(56, 122, 36, 20);
		contentPane.add(c2g1);

		JSpinner c2b1 = new JSpinner();
		c2b1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2b1.setBounds(102, 122, 36, 20);
		contentPane.add(c2b1);

		JSpinner c2r2 = new JSpinner();
		c2r2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2r2.setBounds(159, 122, 36, 20);
		contentPane.add(c2r2);

		JSpinner c2g2 = new JSpinner();
		c2g2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2g2.setBounds(205, 122, 36, 20);
		contentPane.add(c2g2);

		JSpinner c2b2 = new JSpinner();
		c2b2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c2b2.setBounds(251, 122, 36, 20);
		contentPane.add(c2b2);

		preview2b = new JPanel();
		preview2b.setBounds(159, 97, 43, 14);
        preview2b.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview2b);

		preview2a = new JPanel();
		preview2a.setBounds(95, 97, 43, 14);
        preview2a.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview2a);

		JLabel lblAlternate = new JLabel("Alternate");
		lblAlternate.setBounds(10, 153, 82, 14);
		contentPane.add(lblAlternate);

		JSpinner c3r1 = new JSpinner();
		c3r1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3r1.setBounds(10, 178, 36, 20);
		contentPane.add(c3r1);

		JSpinner c3g1 = new JSpinner();
		c3g1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3g1.setBounds(56, 178, 36, 20);
		contentPane.add(c3g1);

		JSpinner c3b1 = new JSpinner();
		c3b1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3b1.setBounds(102, 178, 36, 20);
		contentPane.add(c3b1);

		JSpinner c3r2 = new JSpinner();
		c3r2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3r2.setBounds(159, 178, 36, 20);
		contentPane.add(c3r2);

		JSpinner c3g2 = new JSpinner();
		c3g2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3g2.setBounds(205, 178, 36, 20);
		contentPane.add(c3g2);

		JSpinner c3b2 = new JSpinner();
		c3b2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c3b2.setBounds(251, 178, 36, 20);
		contentPane.add(c3b2);

		preview3b = new JPanel();
		preview3b.setBounds(159, 153, 43, 14);
        preview3b.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview3b);

		preview3a = new JPanel();
		preview3a.setBounds(95, 153, 43, 14);
        preview3a.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview3a);

		JLabel lblCursor = new JLabel("Selection");
		lblCursor.setBounds(10, 209, 82, 14);
		contentPane.add(lblCursor);

		JSpinner c4r1 = new JSpinner();
		c4r1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4r1.setBounds(10, 234, 36, 20);
		contentPane.add(c4r1);

		JSpinner c4g1 = new JSpinner();
		c4g1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4g1.setBounds(56, 234, 36, 20);
		contentPane.add(c4g1);

		JSpinner c4b1 = new JSpinner();
		c4b1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4b1.setBounds(102, 234, 36, 20);
		contentPane.add(c4b1);

		JSpinner c4r2 = new JSpinner();
		c4r2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4r2.setBounds(159, 234, 36, 20);
		contentPane.add(c4r2);

		JSpinner c4g2 = new JSpinner();
		c4g2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4g2.setBounds(205, 234, 36, 20);
		contentPane.add(c4g2);

		JSpinner c4b2 = new JSpinner();
		c4b2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c4b2.setBounds(251, 234, 36, 20);
		contentPane.add(c4b2);

		preview4b = new JPanel();
		preview4b.setBounds(159, 209, 43, 14);
        preview4b.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview4b);

		preview4a = new JPanel();
		preview4a.setBounds(95, 209, 43, 14);
        preview4a.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview4a);

		JLabel lblStartscroll = new JLabel("Scroll");
		lblStartscroll.setBounds(10, 265, 65, 14);
		contentPane.add(lblStartscroll);

		JSpinner c5r1 = new JSpinner();
		c5r1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5r1.setBounds(10, 290, 36, 20);
		contentPane.add(c5r1);

		JSpinner c5g1 = new JSpinner();
		c5g1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5g1.setBounds(56, 290, 36, 20);
		contentPane.add(c5g1);

		JSpinner c5b1 = new JSpinner();
		c5b1.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5b1.setBounds(102, 290, 36, 20);
		contentPane.add(c5b1);

		JSpinner c5r2 = new JSpinner();
		c5r2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5r2.setBounds(159, 290, 36, 20);
		contentPane.add(c5r2);

		JSpinner c5g2 = new JSpinner();
		c5g2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5g2.setBounds(205, 290, 36, 20);
		contentPane.add(c5g2);

		JSpinner c5b2 = new JSpinner();
		c5b2.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		c5b2.setBounds(251, 290, 36, 20);
		contentPane.add(c5b2);

		preview5b = new JPanel();
		preview5b.setBounds(159, 265, 43, 14);
        preview5b.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview5b);

		preview5a = new JPanel();
		preview5a.setBounds(95, 265, 43, 14);
        preview5a.setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
		contentPane.add(preview5a);
	}

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
        paletteOffset = findPaletteOffset();
        if (paletteOffset == -1) {
            System.err.println("Could not find palette offset!");
        }
        nameOffset = findNameOffset();
        if (nameOffset == -1) {
            System.err.println("Could not find palette name offset!");
        }
        updateUiFromRom();
    }

    private int selectedPalette() {
        int palette = kitSelector.getSelectedIndex();
        assert palette >= 0;
        assert palette < paletteCount;
        return palette;
    }

    // Returns color scaled to 0, 255.
    private java.awt.Color color(int offset) {
        // gggrrrrr 0bbbbbgg
        int r = (romImage[offset] & 0b11111) << 3;
        int g = ((romImage[offset + 1] & 0b11) << 6) | ((romImage[offset] & 0b11100000) >> 2);
        int b = (romImage[offset + 1] << 1) & 0b11111000;
        return new java.awt.Color(r, g, b);
    }

    private java.awt.Color firstColor(int colorSet) {
        assert colorSet >= 0;
        assert colorSet < 5;
        int offset = paletteOffset + selectedPalette() * paletteSize + colorSet * colorSetSize;
        return color(offset);
    }

    private java.awt.Color secondColor(int colorSet) {
        assert colorSet >= 0;
        assert colorSet < 5;
        int offset = paletteOffset + selectedPalette() * paletteSize + colorSet * colorSetSize + 3 * 2;
        return color(offset);
    }

    private String paletteName(int palette) {
        assert palette >= 0;
        assert palette < paletteCount;
        String s = new String();
        s += (char)romImage[nameOffset + palette * 5];
        s += (char)romImage[nameOffset + palette * 5 + 1];
        s += (char)romImage[nameOffset + palette * 5 + 2];
        s += (char)romImage[nameOffset + palette * 5 + 3];
        return s;
    }

    private void populateKitSelector() {
        kitSelector.removeAllItems();
        for (int i = 0; i < paletteCount; ++i) {
            kitSelector.addItem(paletteName(i));
        }
    }

    private void updateUiFromRom() {
        populateKitSelector();  // Needs to be done first.

        preview1a.setBackground(firstColor(0));
        preview1b.setBackground(secondColor(0));
        preview2a.setBackground(firstColor(1));
        preview2b.setBackground(secondColor(1));
        preview3a.setBackground(firstColor(2));
        preview3b.setBackground(secondColor(2));
        preview4a.setBackground(firstColor(3));
        preview4b.setBackground(secondColor(3));
        preview5a.setBackground(firstColor(4));
        preview5b.setBackground(secondColor(4));
    }

    private int findNameOffset() {
        // Palette names are in bank 27.
        int i = 0x4000 * 27;
        while (i < 0x4000 * 28) {
            // The first screen background start with 17 zeroes
            // followed by three 72's.
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
                        return i + 30;
                    }
            ++i;
        }
        return -1;
    }

    private int findPaletteOffset() {
        // Finds the palette location by searching for the screen
        // backgrounds, which are defined directly after the palettes
        // in bank 1.
        int i = 0x4000;
        while (i < 0x8000) {
            // The first screen background start with 17 zeroes
            // followed by three 72's.
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
                    romImage[i + 19] == 72) {
                return i - paletteCount * paletteSize;
            }
            ++i;
        }
        return -1;
    }
}
