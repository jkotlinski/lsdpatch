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
        void playSample();
        void deleteSample();
        void replaceSample();
        void renameSample(String s);
    }

    class Pad extends JToggleButton {
        int id;
        Pad(int id) {
            this.id = id;
            setPreferredSize(new Dimension(64, 64));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mouseClicked(e);
                    showPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseClicked(e);
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (!e.isPopupTrigger() || getText().equals("---")) {
                        return;
                    }
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem rename = new JMenuItem("Rename...");
                    menu.add(rename);
                    rename.addActionListener(e1 -> {
                        String name = JOptionPane.showInputDialog("Enter new sample name");
                        if (name != null) {
                            listener.renameSample(name);
                        }
                    });
                    JMenuItem replace = new JMenuItem("Replace...");
                    menu.add(replace);
                    replace.addActionListener(e1 -> listener.replaceSample());
                    JMenuItem delete = new JMenuItem("Delete");
                    menu.add(delete);
                    delete.addActionListener(e1 -> listener.deleteSample());
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
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
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        listener.playSample();
                    }
                }
            });
        }
    }

    @Override
    public void grabFocus() {
        pads.get(0).grabFocus();
    }

    private Pad createPad() {
        Pad pad = new Pad(pads.size());
        pad.setToolTipText("Play pads using keys: 1234 QWER ASDF ZXC");
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
            if (pad.isSelected()) {
                return pad.id;
            }
        }
        return selectedIndices.isEmpty() ? -1 : selectedIndices.first();
    }

    public ArrayList<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedIndices);
    }

    public void addListSelectionListener(Listener listener) {
        this.listener = listener;
    }
}
