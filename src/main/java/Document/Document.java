package Document;

import utils.GlobalHolder;
import utils.RomUtilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

public class Document {
    private boolean romDirty;
    private byte[] romImage;
    private File romFile;

    private final List<IDocumentListener> documentListeners = new LinkedList<IDocumentListener>();

    public void subscribe(IDocumentListener documentListener) {
        documentListeners.add(documentListener);
    }

    public File romFile() {
        return romFile;
    }

    private void setRomDirty(boolean dirty) {
        romDirty = dirty;
        for (IDocumentListener documentListener : documentListeners) {
            documentListener.onRomDirty(dirty);
        }
    }

    public byte[] romImage() {
        return romImage == null ? null : romImage.clone();
    }

    public void setRomImage(byte[] romImage) {
        if (Arrays.equals(romImage, this.romImage)) {
            return;
        }
        this.romImage = romImage;
        setRomDirty(true);
    }

    public void loadRomImage(String romPath) {
        romFile = new File(romPath);
        romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
        try {
            RandomAccessFile f = new RandomAccessFile(romFile, "r");
            f.readFully(romImage);
            f.close();

            GlobalHolder.get(Preferences.class).put("path", romFile.getAbsolutePath());
        } catch (IOException ioe) {
            romImage = null;
        }
        setRomDirty(false);
    }

    public boolean isRomDirty() {
        return romDirty;
    }

    public boolean isDirty() {
        return romDirty;
    }
}
