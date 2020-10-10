package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

class SampleTest {

    @Test
    void createFromWav() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sine1s44khz.wav");
        assert url != null;
        File file = new File(url.getFile());
        Sample sample = null;
        try {
            sample = Sample.createFromWav(file);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        Assertions.assertNotNull(sample);
        Assertions.assertEquals("sine1s44khz.wav", sample.getName());
        Assertions.assertEquals(11469, sample.length());
    }
}