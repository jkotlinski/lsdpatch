package Document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

class LSDSavFileTest {
    @Test
    void isValid() {
        LSDSavFile savFile = new LSDSavFile();
        Arrays.fill(savFile.workRam, (byte)-1);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("empty.lsdprj")).getFile());
        int addedSongs = 0;
        try {
            while (true) {
                savFile.addSongFromFile(file.getAbsolutePath(), null);
                ++addedSongs;
            }
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Out of blocks!");
        }
        Assertions.assertEquals(addedSongs, 31);
        for (int song = 0; song < addedSongs; ++song) {
            Assertions.assertTrue(savFile.isValid(song));
        }
    }
}