package fontEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fontEditor.ChangeEventListener.ChangeEventMouseSide;

class FontEditorColorSelector {

    private final JPanel foregroundColorIndicator;
    private final JPanel backgroundColorIndicator;

    private final ArrayList<ChangeEventListener> listeners;

    private static class FontEditorColorListener implements MouseListener {
        final FontEditorColorSelector selector;
        final int color;

        FontEditorColorListener(FontEditorColorSelector selector, int color) {
            this.selector = selector;
            this.color = color;
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
        JPanel darkButton = new JPanel();
        darkButton.setBackground(Color.BLACK);
        darkButton.setForeground(Color.BLACK);
        darkButton.addMouseListener(new FontEditorColorListener(this, 3));

        JPanel mediumButton = new JPanel();
        mediumButton.setBackground(Color.GRAY);
        mediumButton.addMouseListener(new FontEditorColorListener(this, 2));

        JPanel lightButton = new JPanel();
        lightButton.setBackground(Color.WHITE);
        lightButton.addMouseListener(new FontEditorColorListener(this, 1));

        listeners = new ArrayList<>();

        JPanel indicatorContainer = new JPanel();
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

    private void sendEvent(int color, ChangeEventMouseSide side) {
        Color buttonColor = Color.RED;
        switch (color) {
            case 1:
                buttonColor = Color.WHITE;
                break;
            case 2:
                buttonColor = Color.GRAY;
                break;
            case 3:
                buttonColor = Color.BLACK;
                break;
        }

        for (ChangeEventListener listener : listeners) {
            listener.onChange(color, side);
        }
        if (side == ChangeEventMouseSide.LEFT)
            foregroundColorIndicator.setBackground(buttonColor);
        else
            backgroundColorIndicator.setBackground(buttonColor);
    }

    void addChangeEventListener(ChangeEventListener changeEventListener) {
        listeners.add(changeEventListener);
    }

}
