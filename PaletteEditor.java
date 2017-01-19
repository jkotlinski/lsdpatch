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

		JComboBox kitSelector = new JComboBox();
		kitSelector.setBounds(10, 10, 140, 20);
		contentPane.add(kitSelector);

		Panel previewSong = new Panel();
		previewSong.setBounds(314, 10, 160, 144);
		contentPane.add(previewSong);

		Panel previewProject = new Panel();
		previewProject.setBounds(314, 164, 160, 144);
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

		JPanel preview1a = new JPanel();
		preview1a.setBounds(205, 41, 36, 14);
		contentPane.add(preview1a);

		JPanel preview1b = new JPanel();
		preview1b.setBounds(251, 41, 36, 14);
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

		JPanel preview2b = new JPanel();
		preview2b.setBounds(251, 97, 36, 14);
		contentPane.add(preview2b);

		JPanel preview2a = new JPanel();
		preview2a.setBounds(205, 97, 36, 14);
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

		JPanel preview3b = new JPanel();
		preview3b.setBounds(251, 153, 36, 14);
		contentPane.add(preview3b);

		JPanel preview3a = new JPanel();
		preview3a.setBounds(205, 153, 36, 14);
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

		JPanel preview4b = new JPanel();
		preview4b.setBounds(251, 209, 36, 14);
		contentPane.add(preview4b);

		JPanel preview4a = new JPanel();
		preview4a.setBounds(205, 209, 36, 14);
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

		JPanel preview5b = new JPanel();
		preview5b.setBounds(251, 265, 36, 14);
		contentPane.add(preview5b);

		JPanel preview5a = new JPanel();
		preview5a.setBounds(205, 265, 36, 14);
		contentPane.add(preview5a);
	}

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
    }
}
