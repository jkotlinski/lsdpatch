package structures;

/**
 * Base class to centralize the concept of having an access to a ROM's binary data for edition.
 */
public abstract class ROMDataManipulator {

    protected int dataOffset = 0;
    protected byte[] romImage = null;

    public void setRomImage(byte[] romImage) {
        this.romImage = romImage;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public  void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }
}
