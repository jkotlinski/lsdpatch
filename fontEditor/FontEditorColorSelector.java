package fontEditor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class FontEditorColorSelector extends ButtonGroup implements java.awt.event.ActionListener {
	private static final long serialVersionUID = 7459644463721795475L;

	private JRadioButton darkButton;
	private JRadioButton mediumButton;
	private JRadioButton lightButton;

	private ArrayList<ChangeEventListener> listeners;
	
	public FontEditorColorSelector(JPanel buttonPanel)
	{
		darkButton = new JRadioButton("Dark");
		darkButton.setBackground(Color.BLACK);
		darkButton.addActionListener(this);
		mediumButton = new JRadioButton("Medium");
		mediumButton.setBackground(Color.GRAY);
		mediumButton.addActionListener(this);
		lightButton = new JRadioButton("Light");
		lightButton.setBackground(Color.WHITE);
		lightButton.addActionListener(this);

		add(lightButton);
		add(mediumButton);
		add(darkButton);
		
		listeners = new ArrayList<ChangeEventListener>();
		
		buttonPanel.add(darkButton);
		buttonPanel.add(mediumButton);
		buttonPanel.add(lightButton);

	}
	
	public void setSelectedColor(int color)
	{
		switch(color)
		{
		case 1:
			lightButton.setSelected(true);
			break;
		case 2:
			mediumButton.setSelected(true);
			break;
		case 3:
			darkButton.setSelected(true);
			break;
		}
	}

	void addChangeEventListener(ChangeEventListener changeEventListener)
	{
		listeners.add(changeEventListener);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		int color = -1;

		if(event.getSource() == lightButton)
			color = 1;
		else if (event.getSource() == mediumButton)
			color = 2;
		else if (event.getSource() == darkButton)
			color = 3;
		
		for(ChangeEventListener listener : listeners)
			listener.onChange(color);
	}
}
