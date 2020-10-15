package kitEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeSet;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class SamplePicker extends JPanel {
    private Listener listener;

    interface Listener {
        void selectionChanged();
    }

    static class Pad extends JToggleButton {
        int id;
        Pad(int id) {
            this.id = id;
            setPreferredSize(new Dimension(64, 64));
        }
    }

    private final ArrayList<Pad> pads;
    private final TreeSet<Integer> selectedIndices = new TreeSet<>();

    SamplePicker() {
        setLayout(new MigLayout());
        pads = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Pad pad = createPad();
            pad.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    if ((e.getModifiersEx() & SHIFT_DOWN_MASK) == 0) {
                        selectedIndices.clear();
                        for (int i = 0; i < 15; ++i) {
                            JToggleButton button = pads.get(i);
                            button.setSelected(false);
                        }
                    }

                    Pad sender = (Pad)e.getSource();
                    int min = Math.min(sender.id, selectedIndices.isEmpty() ? Integer.MAX_VALUE : selectedIndices.first());
                    int max = Math.max(sender.id, selectedIndices.isEmpty() ? Integer.MIN_VALUE : selectedIndices.last());
                    for (int i = 0; i < 15; ++i) {
                        if (i >= min && i <= max) {
                            selectedIndices.add(i);
                            pads.get(i).setSelected(true);
                        }
                    }
                    listener.selectionChanged();
                }
            });
        }
    }

    private Pad createPad() {
        Pad pad = new Pad(pads.size());
        pads.add(pad);
        add(pad, (pads.size() % 4) == 0 ? "wrap, sg button" : "");
        return pad;
    }

    public void setListData(String[] listData) {
        assert listData.length == pads.size();
        for (int i = 0; i < pads.size(); ++i) {
            pads.get(i).setText(listData[i]);
        }
    }

    public void setSelectedIndex(int selectedIndex) {
        selectedIndices.clear();
        for (int i = 0; i < 15; ++i) {
            JToggleButton button = pads.get(i);
            button.setSelected(false);
        }
        if (selectedIndex == -1) {
            return;
        }
        selectedIndices.add(selectedIndex);
        pads.get(selectedIndex).setSelected(true);
        listener.selectionChanged();
    }

    public int getSelectedIndex() {
        return selectedIndices.first();
    }

    public ArrayList<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedIndices);
    }

    public void addListSelectionListener(Listener listener) {
        this.listener = listener;
    }
}
