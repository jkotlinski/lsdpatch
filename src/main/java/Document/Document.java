package Document;

import utils.EditorPreferences;
import utils.RomUtilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Document {
    private boolean romDirty;
    private byte[] romImage;
    private File romFile;

    private boolean savDirty;
    private LSDSavFile savFile = new LSDSavFile();

    private final List<IDocumentListener> documentListeners = new LinkedList<>();

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

    private void setSavDirty(boolean dirty) {
        savDirty = dirty;
        for (IDocumentListener documentListener : documentListeners) {
            documentListener.onSavDirty(dirty);
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

    public void loadRomImage(String romPath) throws IOException {
        romFile = new File(romPath);
        romImage = new byte[RomUtilities.BANK_SIZE * RomUtilities.BANK_COUNT];
        setRomDirty(false);
        try {
            RandomAccessFile f = new RandomAccessFile(romFile, "r");
            f.readFully(romImage);
            f.close();
            EditorPreferences.setLastPath("gb", romPath);
        } catch (IOException ioe) {
            romImage = null;
            throw ioe;
        }
    }

    public void loadSavFile(String savPath) throws IOException {
        savDirty = false;
        try {
            savFile = new LSDSavFile();
            savFile.loadFromSav(savPath);
            EditorPreferences.setLastPath("sav", savPath);
        } catch (IOException e) {
            savFile = null;
            throw e;
        }
    }

    public LSDSavFile savFile() {
        if (savFile == null) {
            return null;
        }
        try {
            return savFile.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void setSavFile(LSDSavFile savFile) {
        if (savFile == null) {
            this.savFile = null;
            setSavDirty(false);
            return;
        }
        if (this.savFile != null && savFile.equals(this.savFile)) {
            return;
        }
        this.savFile = savFile;
        setSavDirty(true);
    }

    public boolean isSavDirty() {
        return savDirty;
    }

    public boolean isRomDirty() {
        return romDirty;
    }

    public boolean isDirty() {
        return romDirty || savDirty;
    }

    public void setRomFile(File file) {
        romFile = file;
    }

    public void clearSavDirty() {
        setSavDirty(false);
    }

    public void clearRomDirty() {
        setRomDirty(false);
    }
}
