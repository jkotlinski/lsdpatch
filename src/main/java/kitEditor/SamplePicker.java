package kitEditor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeSet;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class SamplePicker extends JPanel {
    private Listener listener;

    interface Listener {
        void selectionChanged();
        void delete();
    }

    class Pad extends JToggleButton {
        int id;
        Pad(int id) {
            this.id = id;
            setPreferredSize(new Dimension(64, 64));
        }

        public void select(boolean keepOldSelection) {
            if (!keepOldSelection) {
                selectedIndices.clear();
                for (int i = 0; i < 15; ++i) {
                    JToggleButton button = pads.get(i);
                    button.setSelected(false);
                }
            }
            selectedIndices.add(id);
            setSelected(true);
            grabFocus();
            listener.selectionChanged();
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

        for (Pad pad : pads) {
            pad.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    super.keyPressed(e);
                    boolean shiftDown = (e.getModifiersEx() & SHIFT_DOWN_MASK) != 0;
                    switch (e.getKeyCode()) {
                        case VK_LEFT:
                            if (getSelectedIndex() > 0) {
                                pads.get(getSelectedIndex() - 1).select(shiftDown);
                            }
                            break;
                        case VK_RIGHT:
                            if (getSelectedIndex() < 14) {
                                pads.get(getSelectedIndex() + 1).select(shiftDown);
                            }
                            break;
                        case VK_DOWN:
                            if (getSelectedIndex() < 11) {
                                pads.get(getSelectedIndex() + 4).select(shiftDown);
                            }
                            break;
                        case VK_UP:
                            if (getSelectedIndex() > 3) {
                                pads.get(getSelectedIndex() - 4).select(shiftDown);
                            }
                            break;
                        case VK_SPACE:
                            listener.selectionChanged();
                            break;
                        case VK_DELETE:
                            listener.delete();
                            break;
                        case VK_A:
                            if ((e.getModifiersEx() & CTRL_DOWN_MASK) != 0) {
                                selectAll();
                            }
                    }
                }
            });
        }
    }

    private void selectAll() {
        for (Pad pad : pads) {
            pad.setSelected(true);
            selectedIndices.add(pad.id);
        }
        listener.selectionChanged();
    }

    @Override
    public void grabFocus() {
        pads.get(0).grabFocus();
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
        for (Pad pad : pads) {
            if (pad.hasFocus()) {
                return pad.id;
            }
        }
        return selectedIndices.first();
    }

    public ArrayList<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedIndices);
    }

    public void addListSelectionListener(Listener listener) {
        this.listener = listener;
    }
}
