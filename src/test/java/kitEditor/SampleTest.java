package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

class SampleTest {

    @Test
    void createFromWav() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sine1s44khz.wav")).getFile());
        Sample sample = null;
        try {
            sample = Sample.createFromWav(file);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        Assertions.assertNotNull(sample);
        Assertions.assertEquals("sine1s44khz.wav", sample.getName());
        Assertions.assertEquals(11469, sample.length());
        File f = new File("c:\\cygwin64\\home\\Johan\\lsdpatch\\test.raw");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            for (int i = 0; i < sample.length(); ++i) {
                fos.write(sample.read());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}