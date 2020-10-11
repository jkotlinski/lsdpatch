package Document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

class LSDSavFileTest {
    private LSDSavFile savFile;

    @BeforeEach
    void createLsdSavFile() {
        savFile = new LSDSavFile();
        Arrays.fill(savFile.workRam, (byte)-1); // Resets block allocation table.
        savFile.workRam[0] = 0; // Satisfies 64 kb SRAM check.
    }

    @Test
    @DisplayName("Add songs until out of blocks, validate all")
    void isValid_addSongsUntilOutOfBlocks() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("triangle_waves.lsdprj")).getFile());
        int addedSongs = 0;
        try {
            while (true) {
                savFile.addSongFromFile(file.getAbsolutePath(), null);
                ++addedSongs;
            }
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Out of blocks!");
        }
        Assertions.assertEquals(addedSongs, 19);
        for (int song = 0; song < addedSongs; ++song) {
            Assertions.assertTrue(savFile.isValid(song));
        }
    }

    @Test
    void testClone() throws CloneNotSupportedException {
        LSDSavFile savFile = new LSDSavFile();
        LSDSavFile clone = savFile.clone();
        Assertions.assertNotNull(clone);
        Assertions.assertNotSame(savFile, clone);
    }

    @Test
    void saveAs() throws Exception {
        LSDSavFile savFile = new LSDSavFile();
        File file = File.createTempFile("lsdpatcher", ".sav");
        file.deleteOnExit();
        savFile.saveAs(file.getAbsolutePath());
    }
}