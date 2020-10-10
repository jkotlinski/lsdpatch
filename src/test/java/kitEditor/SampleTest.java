package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

class SampleTest {

    @Test
    void createFromWav() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sine1s44khz.wav")).getFile());
        Sample sample = Sample.createFromWav(file);
        Assertions.assertNotNull(sample);
        Assertions.assertEquals("sine1s44khz.wav", sample.getName());
        Assertions.assertEquals(11468, sample.length());
    }
}