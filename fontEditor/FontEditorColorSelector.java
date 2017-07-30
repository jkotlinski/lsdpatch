package fontEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fontEditor.ChangeEventListener.ChangeEventMouseSide;

public class FontEditorColorSelector {
	private static final long serialVersionUID = 7459644463721795475L;

	private JPanel darkButton;
	private JPanel mediumButton;
	private JPanel lightButton;

	private JPanel indicatorContainer;
	private JPanel foregroundColorIndicator;
	private JPanel backgroundColorIndicator;

	private ArrayList<ChangeEventListener> listeners;

	private class FontEditorColorListener implements MouseListener {
		JPanel button;
		FontEditorColorSelector selector;
		int color = 0;

		public FontEditorColorListener(FontEditorColorSelector selector, int color, JPanel button) {
			this.selector = selector;
			this.color = color;
			this.button = button;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e))
				selector.sendEvent(color, ChangeEventMouseSide.RIGHT);
			if (SwingUtilities.isLeftMouseButton(e))
				selector.sendEvent(color, ChangeEventMouseSide.LEFT);

		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	public FontEditorColorSelector(JPanel buttonPanel) {
		darkButton = new JPanel();
		darkButton.setBackground(Color.BLACK);
		darkButton.setForeground(Color.BLACK);
		darkButton.addMouseListener(new FontEditorColorListener(this, 3, darkButton));

		mediumButton = new JPanel();
		mediumButton.setBackground(Color.GRAY);
		mediumButton.addMouseListener(new FontEditorColorListener(this, 2, mediumButton));

		lightButton = new JPanel();
		lightButton.setBackground(Color.WHITE);
		lightButton.addMouseListener(new FontEditorColorListener(this, 1, lightButton));

		listeners = new ArrayList<ChangeEventListener>();

		indicatorContainer = new JPanel();
		indicatorContainer.setLayout(null);

		foregroundColorIndicator = new JPanel();
		foregroundColorIndicator.setBackground(Color.WHITE);
		foregroundColorIndicator.setForeground(Color.WHITE);
		foregroundColorIndicator.setPreferredSize(new Dimension(24, 24));
		foregroundColorIndicator.setBounds(8, 8, 24, 24);
		indicatorContainer.add(foregroundColorIndicator);

		backgroundColorIndicator = new JPanel();
		backgroundColorIndicator.setBackground(Color.BLACK);
		backgroundColorIndicator.setForeground(Color.BLACK);
		backgroundColorIndicator.setPreferredSize(new Dimension(24, 24));
		backgroundColorIndicator.setBounds(0, 0, 24, 24);
		indicatorContainer.add(backgroundColorIndicator);


		buttonPanel.add(indicatorContainer);
		buttonPanel.add(darkButton);
		buttonPanel.add(mediumButton);
		buttonPanel.add(lightButton);

		buttonPanel.setPreferredSize(new Dimension(200, 32));

	}

	protected void sendEvent(int color, ChangeEventMouseSide side)
	{
		Color buttonCol = Color.RED;
		switch(color) {
		case 1:				
			buttonCol = Color.WHITE;
			break;
		case 2 :
			buttonCol = Color.GRAY;
			break;
		case 3 :
			buttonCol = Color.BLACK;
			break;
		}

		for(ChangeEventListener listener : listeners) {
			listener.onChange(color, side);
		}
		if(side == ChangeEventMouseSide.LEFT)
			foregroundColorIndicator.setBackground(buttonCol);
		else
			backgroundColorIndicator.setBackground(buttonCol);
	}

	void addChangeEventListener(ChangeEventListener changeEventListener) {
		listeners.add(changeEventListener);
	}

}
