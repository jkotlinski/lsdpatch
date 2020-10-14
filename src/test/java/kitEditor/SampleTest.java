package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
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
            sample = Sample.createFromWav(file, false);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        Assertions.assertNotNull(sample);
        Assertions.assertEquals("sine1s44khz.wav", sample.getName());
        Assertions.assertEquals(11469, sample.length());

        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < sample.length(); ++i) {
            int s = sample.read();
            sum += s;
            min = Math.min(s, min);
            max = Math.max(s, max);
        }
        int avg = sum / sample.length();
        Assertions.assertEquals(0, avg);
        Assertions.assertEquals(Short.MIN_VALUE, min);
        Assertions.assertEquals(32765, max);
    }
}