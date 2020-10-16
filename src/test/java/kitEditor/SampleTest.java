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
        Assertions.assertEquals("sine1s44khz", sample.getName());
        Assertions.assertEquals(11469, sample.lengthInSamples());
        Assertions.assertEquals(5728, sample.lengthInBytes());

        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < sample.lengthInSamples(); ++i) {
            int s = sample.read();
            sum += s;
            min = Math.min(s, min);
            max = Math.max(s, max);
        }
        int avg = sum / sample.lengthInSamples();
        Assertions.assertEquals(0, avg);
        Assertions.assertEquals(Short.MIN_VALUE, min);
        Assertions.assertEquals(32765, max);
    }

    @Test
    void normalize() {
        short[] buf = new short[2];
        buf[0] = 1;
        Sample sample = Sample.createFromOriginalSamples(buf, null, null, 0, false);
        Assertions.assertEquals(Short.MAX_VALUE, sample.read());
        Assertions.assertEquals(0, sample.read());

        sample = Sample.createFromOriginalSamples(buf, null, null, -20, false);
        Assertions.assertEquals(Short.MAX_VALUE / 10, sample.read());
        Assertions.assertEquals(0, sample.read());
    }
}