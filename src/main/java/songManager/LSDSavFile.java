package songManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import javax.swing.*;

public class LSDSavFile {
    final int blockSize = 0x200;
    final int bankSize = 0x8000;
    final int bankCount = 4;
    final int savFileSize = bankSize * bankCount;
    final int songCount = 0x20;
    final int fileNameLength = 8;

    final int fileNameStartPtr = 0x8000;
    final int fileVersionStartPtr = 0x8100;
    final int blockAllocTableStartPtr = 0x8141;
    final int blockStartPtr = 0x8200;
    final int activeFileSlot = 0x8140;
    final char emptySlotValue = (char) 0xff;

    boolean is64kb = false;
    boolean is64kbHasBeenSet = false;

    byte[] workRam;
    boolean fileIsLoaded = false;

    public LSDSavFile() {
        workRam = new byte[savFileSize];
    }

    private boolean isSixtyFourKbRam() {
        if (!fileIsLoaded) return false;
        if (is64kbHasBeenSet) return is64kb;

        for (int i = 0; i < 0x10000; ++i) {
            if (workRam[i] != workRam[0x10000 + i]) {
                is64kb = false;
                is64kbHasBeenSet = true;
                return false;
            }
        }
        is64kb = true;
        is64kbHasBeenSet = true;
        return true;
    }

    public int totalBlockCount() {
        // FAT takes one block.
        return isSixtyFourKbRam() ? 0xbf - 0x80 : 0xbf;
    }

    public void saveAs(String filePath) {
        try {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            if (isSixtyFourKbRam()) {
                System.arraycopy(workRam, 0, workRam, 65536, 0x10000);
            }
            file.write(workRam);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveWorkMemoryAs(String filePath) {
        try {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            file.write(workRam, 0, bankSize);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearSong(int index) {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;

        while (block < totalBlockCount()) {
            int tableValue = workRam[ramPtr];
            if (index == tableValue) {
                workRam[ramPtr] = (byte) emptySlotValue;
            }
            ramPtr++;
            block++;
        }

        clearFileName(index);
        clearFileVersion(index);

        if (index == getActiveFileSlot()) {
            clearActiveFileSlot();
        }
    }

    public int getBlocksUsed(int slot) {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;
        int blockCount = 0;

        while (block++ < totalBlockCount()) {
            if (slot == workRam[ramPtr++]) {
                blockCount++;
            }
        }
        return blockCount;
    }

    private void clearFileName(int index) {
        workRam[fileNameStartPtr + fileNameLength * index] = (byte) 0;
    }

    private void clearFileVersion(int index) {
        workRam[fileVersionStartPtr + index] = (byte) 0;
    }

    public int usedBlockCount() {
        return totalBlockCount() - freeBlockCount();
    }

    private byte getNewSongId() {
        for (byte slot = 0; slot < songCount; slot++) {
            if (0 == getBlocksUsed(slot)) {
                return slot;
            }
        }
        return -1;
    }

    private int getBlockIdOfFirstFreeBlock() {
        int blockAllocTableStartPtr = this.blockAllocTableStartPtr;
        int block = 0;

        while (block < totalBlockCount()) {
            int tableValue = workRam[blockAllocTableStartPtr++];
            if (tableValue < 0 || tableValue > 0x1f) {
                return block;
            }
            block++;
        }
        return -1;
    }

    /*
    public void debug_dump_fat()
    {
        int l_ram_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;

        while (l_block < getTotalBlockCount())
        {
            int l_table_value = m_work_ram[l_ram_ptr++];
            System.out.print(l_table_value + " " );
            l_block++;
        }
        System.out.println();
    }
    */

    public int freeBlockCount() {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;
        int freeBlockCount = 0;

        while (block < totalBlockCount()) {
            int tableValue = workRam[ramPtr++];
            if (tableValue < 0 || tableValue > 0x1f) {
                freeBlockCount++;
            }
            block++;
        }
        return freeBlockCount;
    }

    public boolean loadFromSav(String filePath) {
        RandomAccessFile savFile;
        int readBytes;

        try {
            savFile = new RandomAccessFile(filePath, "r");
            readBytes = savFile.read(workRam);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "Could not load .sav file",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (readBytes > savFileSize) {
            return false;
        }

        is64kbHasBeenSet = false;
        fileIsLoaded = true;
        return true;
    }

    public void populateSongList(JList<String> songList) {
        String[] songStringList = new String[songCount];
        songList.removeAll();

        for (int song = 0; song < songCount; song++) {
            int blocksUsed = getBlocksUsed(song);
            String songString = song + 1 + ". ";

            if (blocksUsed > 0) {
                songString += getFileName(song);
                songString += "." + version(song);
                songString += " " + blocksUsed;
                if (!isValid(song)) {
                    songString += " \u26a0"; // warning sign
                }
            }

            songStringList[song] = songString;
        }

        songList.setListData(songStringList);
    }

    private static int convertLsdCharToAscii(int ch) {
        if (ch >= 65 && ch <= (65 + 25)) {
            //char
            return 'A' + ch - 65;
        }
        if (ch >= 48 && ch < 58) {
            //decimal number
            return '0' + ch - 48;
        }
        return 0 == ch ? 0 : ' ';
    }

    public String getFileName(int slot) {
        StringBuilder sb = new StringBuilder();
        int ramPtr = fileNameStartPtr + fileNameLength * slot;
        boolean endOfFileName = false;
        for (int fileNamePos = 0;
             fileNamePos < 8;
             fileNamePos++) {
            if (!endOfFileName) {
                char ch = (char) convertLsdCharToAscii((char)
                        workRam[ramPtr]);
                if (0 == ch) {
                    endOfFileName = true;
                } else {
                    sb.append(ch);
                }
            }
            ramPtr++;
        }
        return sb.toString();
    }

    public String version(int slot) {
        int ramPtr = fileVersionStartPtr + slot;
        String version = Integer.toHexString(workRam[ramPtr]);
        return version.substring(Math.max(version.length() - 2, 0)).toUpperCase();
    }

    public void exportSongToFile(int songId, String filePath, byte[] romImage) {
        assert (songId >= 0 && songId < 0x20);

        RandomAccessFile file;
        try {
            file = new RandomAccessFile(filePath, "rw");

            int fileNamePtr = fileNameStartPtr + songId * fileNameLength;
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr]);

            int fileVersionPtr = fileVersionStartPtr + songId;
            file.writeByte(workRam[fileVersionPtr]);

            writeSongBlocks(songId, file);
            writeKits(romImage, songId, file);

            file.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "Song export failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeKits(byte[] romImage, int songId, RandomAccessFile file) throws IOException {
        TreeSet<Integer> kitsToWrite = usedKits(songId);
        while (true) {
            Integer kit = kitsToWrite.pollFirst();
            if (kit == null) {
                break;
            }
            // because legacy, kits are in banks 8-26, 32-63.
            kit += 8;
            if (kit > 26) {
                kit += 5;
            }
            int kitOffset = kit * 0x4000;
            for (int i = 0; i < 0x4000; ++i) {
                file.writeByte(romImage[kitOffset + i]);
            }
        }
    }

    TreeSet<Integer> usedKits(int songId) {
        byte[] unpackedSong = unpackSong(songId);
        assert (unpackedSong != null);
        assert (unpackedSong.length == 0x8000);

        TreeSet<Integer> kits = new TreeSet<>();
        for (int instr = 0; instr < 0x40; ++instr) {
            int instrPtr = 0x3080 + instr * 0x10;
            if (unpackedSong[instrPtr] != 2) {
                continue; // Not kit instrument.
            }
            kits.add(unpackedSong[instrPtr + 2] & 0x3f);
            kits.add(unpackedSong[instrPtr + 9] & 0x3f);
        }
        return kits;
    }

    void writeSongBlocks(int songId, RandomAccessFile file) throws IOException {
        int blockId = 0;
        int blockAllocTablePtr = blockAllocTableStartPtr;

        while (blockId < totalBlockCount()) {
            if (songId == workRam[blockAllocTablePtr++]) {
                int blockPtr = blockStartPtr + blockId * blockSize;
                for (int byteIndex = 0; byteIndex < blockSize; byteIndex++) {
                    file.writeByte(workRam[blockPtr++]);
                }
            }
            blockId++;
        }
    }

    /**
     * Decodes a song. Returns 32 kB with decoded song data, or null on failure.
     */
    private byte[] unpackSong(int songId) {
        byte[] dstBuffer = new byte[0x8000];
        int dstPos = 0;

        int blockId = 0;
        int blockAllocTablePtr = blockAllocTableStartPtr;

        while (blockId < totalBlockCount()) {
            if (songId == workRam[blockAllocTablePtr++]) {
                break;
            }
            blockId++;
        }

        int srcPtr = blockStartPtr + blockSize * blockId;

        try {
            while (true) {
                switch (workRam[srcPtr]) {
                    case (byte) 0xc0:
                        srcPtr++;
                        if (workRam[srcPtr] == (byte) 0xc0) {
                            srcPtr++;
                            dstBuffer[dstPos++] = (byte) 0xc0;
                        } else {
                            // rle
                            byte b = workRam[srcPtr++];
                            byte count = workRam[srcPtr++];
                            while (count-- != 0) {
                                dstBuffer[dstPos++] = b;
                            }
                        }
                        break;

                    case (byte) 0xe0:
                        byte count;
                        srcPtr++;
                        switch (workRam[srcPtr]) {
                            case (byte) 0xe0: // e0
                                srcPtr++;
                                dstBuffer[dstPos++] = (byte) 0xe0;
                                break;

                            case (byte) 0xff: // done!
                                return dstPos == 0x8000 ? dstBuffer : null;

                            case (byte) 0xf0: //wave
                                srcPtr++;
                                count = workRam[srcPtr++];
                                while (count-- != 0) {
                                    dstBuffer[dstPos++] = (byte) 0x8e;
                                    dstBuffer[dstPos++] = (byte) 0xcd;
                                    dstBuffer[dstPos++] = (byte) 0xcc;
                                    dstBuffer[dstPos++] = (byte) 0xbb;
                                    dstBuffer[dstPos++] = (byte) 0xaa;
                                    dstBuffer[dstPos++] = (byte) 0xa9;
                                    dstBuffer[dstPos++] = (byte) 0x99;
                                    dstBuffer[dstPos++] = (byte) 0x88;
                                    dstBuffer[dstPos++] = (byte) 0x87;
                                    dstBuffer[dstPos++] = (byte) 0x76;
                                    dstBuffer[dstPos++] = (byte) 0x66;
                                    dstBuffer[dstPos++] = (byte) 0x55;
                                    dstBuffer[dstPos++] = (byte) 0x54;
                                    dstBuffer[dstPos++] = (byte) 0x43;
                                    dstBuffer[dstPos++] = (byte) 0x32;
                                    dstBuffer[dstPos++] = (byte) 0x31;
                                }
                                break;

                            case (byte) 0xf1: //instr
                                srcPtr++;
                                count = workRam[srcPtr++];
                                while (count-- != 0) {
                                    dstBuffer[dstPos++] = (byte) 0xa8;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = (byte) 0xff;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 3;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = (byte) 0xd0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = (byte) 0xf3;
                                    dstBuffer[dstPos++] = 0;
                                    dstBuffer[dstPos++] = 0;
                                }
                                break;

                            default: // block switch
                                byte block = workRam[srcPtr];
                                srcPtr = 0x8000 + blockSize * block;
                                break;
                        }
                        break;

                    default:
                        dstBuffer[dstPos++] = workRam[srcPtr++];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public boolean isValid(int songId) {
        return unpackSong(songId) != null;
    }

    static class AddSongException extends Exception {
        AddSongException(String message) {
            super(message);
        }
    }

    public void addSongFromFile(String filePath, byte[] romImage) throws Exception {
        final byte songId = getNewSongId();
        if (songId == -1) {
            throw new AddSongException("Out of song slots!");
        }

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            writeFileNameAndVersion(fileInputStream, songId);
            copySongToWorkRam(fileInputStream, songId);
            patchKits(fileInputStream, songId, romImage);
        } catch (Exception e) {
            clearSong(songId);
            throw e;
        }
    }

    private void writeFileNameAndVersion(FileInputStream fileInputStream, byte songId) throws IOException {
        byte[] fileName = new byte[8];
        int read = fileInputStream.read(fileName);
        assert(read == fileName.length);
        byte fileVersion = (byte)fileInputStream.read();

        int fileNamePtr = fileNameStartPtr + songId * fileNameLength;
        for (int i = 0; i < 8; ++i) {
            workRam[fileNamePtr++] = fileName[i];
        }

        int fileVersionPtr = fileVersionStartPtr + songId;
        workRam[fileVersionPtr] = fileVersion;
    }

    private void patchKits(FileInputStream fileInputStream,
                           byte songId,
                           byte[] romImage) throws IOException, AddSongException {
        ArrayList<byte[]> lsdSngKits = new ArrayList<>();
        while (true) {
            byte[] kit = new byte[0x4000];
            if (fileInputStream.read(kit) != kit.length) {
                break;
            }
            lsdSngKits.add(kit);
        }

        if (lsdSngKits.size() == 0) {
            return;
        }

        // Check if kits are already in ROM. If so, they should be reused.
        int[] newKits = new int[lsdSngKits.size()];
        for (int romKit = 0; romKit < romImage.length / 0x4000; ++romKit) {
            for (int kit = 0; kit < lsdSngKits.size(); ++kit) {
                boolean kitsAreEqual = true;
                for (int i = 0; i < 0x4000; ++i) {
                    if (lsdSngKits.get(kit)[i] != romImage[romKit * 0x4000 + i]) {
                        kitsAreEqual = false;
                        break;
                    }
                }
                if (kitsAreEqual) {
                    newKits[kit] = romKit;
                }
            }
        }

        addMissingKits(romImage, lsdSngKits, newKits);
        adjustInstruments(songId, newKits);
    }

    private List<Integer> instrumentKitLocations(int songId) {
        int songPos = 0;
        int blockId = 0;
        int blockAllocTablePtr = blockAllocTableStartPtr;
        List<Integer> instrumentKitLocations = new LinkedList<>();

        while (blockId < totalBlockCount()) {
            if (songId == workRam[blockAllocTablePtr++]) {
                break;
            }
            blockId++;
        }

        int srcPtr = blockStartPtr + blockSize * blockId;
        boolean isKit = false;

        try {
            while (true) {
                switch (workRam[srcPtr]) {
                    case (byte) 0xc0:
                        srcPtr++;
                        if (workRam[srcPtr] == (byte) 0xc0) {
                            srcPtr++;
                            songPos++;
                        } else {
                            srcPtr++;
                            byte count = workRam[srcPtr++];
                            while (count-- != 0) {
                                songPos++;
                            }
                        }
                        break;

                    case (byte) 0xe0:
                        byte count;
                        srcPtr++;
                        switch (workRam[srcPtr]) {
                            case (byte) 0xe0: // e0
                                srcPtr++;
                                songPos++;
                                break;

                            case (byte) 0xff: // done!
                                return instrumentKitLocations;

                            case (byte) 0xf0: //wave
                            case (byte) 0xf1: //instr
                                srcPtr++;
                                count = workRam[srcPtr++];
                                while (count-- != 0) {
                                    songPos += 16;
                                }
                                break;

                            default: // block switch
                                byte block = workRam[srcPtr];
                                srcPtr = 0x8000 + blockSize * block;
                                break;
                        }
                        break;

                    default:
                        // Regular byte write.
                        boolean isInstrumentWrite = songPos >= 0x3080 && songPos < 0x3480;
                        if (isInstrumentWrite) {
                            switch (songPos % 16) {
                                case 0:
                                    isKit = workRam[srcPtr] == 2;
                                    break;
                                case 2:
                                case 9:
                                    if (isKit) {
                                        instrumentKitLocations.add(srcPtr);
                                    }
                                    break;
                            }
                        }
                        ++songPos;
                        ++srcPtr;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private void adjustInstruments(int songId, int[] newKits) {
        List<Integer> instrumentKitLocations = instrumentKitLocations(songId);

        TreeSet<Integer> lsdSngKits = new TreeSet<>();
        for (Integer instrumentKitLocation : instrumentKitLocations) {
            int kitId = workRam[instrumentKitLocation] & 0x3f;
            lsdSngKits.add(kitId);
        }

        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < newKits.length; ++i) {
            int oldKitValue = lsdSngKits.pollFirst();
            int newKitValue = newKits[i];
            if (newKitValue > 26) {
                newKitValue -= 5;
            }
            newKitValue -= 8;
            map.put(oldKitValue, newKitValue);
        }

        for (Integer instrumentKitLocation : instrumentKitLocations) {
            int value = workRam[instrumentKitLocation];
            int newValue = (value & ~0x3f) | map.get(value & 0x3f);
            workRam[instrumentKitLocation] = (byte)newValue;
        }
    }

    private void addMissingKits(byte[] romImage, ArrayList<byte[]> lsdSngKits, int[] newKits) throws AddSongException {
        for (int kit = 0; kit < newKits.length; ++kit) {
            if (newKits[kit] != 0) {
                continue;
            }
            int newKit = findFreeKit(romImage);
            if (newKit == -1) {
                throw new AddSongException("Not enough space for kits! Remove some and try again!");
            }
            newKits[kit] = newKit;
            // Copy kit.
            System.arraycopy(lsdSngKits.get(kit), 0, romImage, newKit * 0x4000, 0x4000);
        }
    }

    private int findFreeKit(byte[] romImage) {
        for (int i = 0; i < romImage.length / 0x4000; ++i) {
            boolean empty = true;
            for (int j = 0; j < 0x4000; ++j) {
                if (romImage[i * 0x4000 + j] != 0) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                return i;
            }
        }
        return -1;
    }

    private void copySongToWorkRam(FileInputStream fileInputStream, byte songId) throws IOException, AddSongException {
        int nextBlockIdPtr = 0;
        while (true) {
            int blockId = getBlockIdOfFirstFreeBlock();
            if (blockId == -1) {
                throw new AddSongException("Out of blocks!");
            }

            if (0 != nextBlockIdPtr) {
                //add one to compensate for unused FAT block
                workRam[nextBlockIdPtr] = (byte) (blockId + 1);
            }
            workRam[blockAllocTableStartPtr + blockId] = songId;
            int blockPtr = blockStartPtr + blockId * blockSize;
            for (int i = 0; i < blockSize; ++i) {
                workRam[blockPtr++] = (byte)fileInputStream.read();
            }
            nextBlockIdPtr = getNextBlockIdPtr(blockId);
            if (nextBlockIdPtr == -1) {
                return;
            }
        }
    }

    private void clearActiveFileSlot() {
        workRam[activeFileSlot] = (byte) 0xff;
    }

    private byte getActiveFileSlot() {
        return workRam[activeFileSlot];
    }

    /* Returns address of next block id pointer (E0 XX), if one exists in block.
     * If there is none, return -1.
     */
    private int getNextBlockIdPtr(int block) throws AddSongException {
        int ramPtr = blockStartPtr + blockSize * block;
        int byteCounter = 0;

        while (byteCounter < blockSize) {
            if (workRam[ramPtr] == (byte) 0xc0) {
                ramPtr++;
                byteCounter++;
                if (workRam[ramPtr] != (byte) 0xc0) {
                    //rle
                    ramPtr++;
                    byteCounter++;
                }
            } else if (workRam[ramPtr] == (byte) 0xe0) {
                switch (workRam[ramPtr + 1]) {
                    case (byte) 0xe0:
                        ramPtr++;
                        byteCounter++;
                        break;
                    case (byte) 0xff:
                        return -1;
                    case (byte) 0xf0: //wave
                    case (byte) 0xf1: //instr
                        ramPtr += 2;
                        byteCounter += 2;
                        break;
                    default:
                        return ramPtr + 1;
                }
            }
            ramPtr++;
            byteCounter++;
        }
        // If the pointer to next block is missing, and this is not the last
        // block of a song, the song is most likely corrupted.
        throw new AddSongException("Song corrupted!");
    }

    public void import32KbSavToWorkRam(String a_file_path) {
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(a_file_path, "r");

            int bytesRead = file.read(workRam, 0, bankSize);

            if (bytesRead < bankSize) {
                return;
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        clearActiveFileSlot();
    }

}