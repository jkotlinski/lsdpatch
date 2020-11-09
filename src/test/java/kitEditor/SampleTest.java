package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

class SampleTest {

    @Test
    void createFromWav() throws IOException, UnsupportedAudioFileException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sine1s44khz.wav");
        assert url != null;
        File file = new File(url.getFile());
        Sample sample = Sample.createFromWav(file, false, false, 0, 0, 0);
        Assertions.assertNotNull(sample);
        Assertions.assertEquals("sine1s44khz", sample.getName());
        Assertions.assertEquals(11467, sample.lengthInSamples());
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
        Assertions.assertEquals(-Short.MAX_VALUE, min);
        Assertions.assertEquals(Short.MAX_VALUE, max);
    }

    @Test
    void decreaseVolume() throws IOException, UnsupportedAudioFileException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sine1s44khz.wav");
        assert url != null;
        File file = new File(url.getFile());

        Sample sample = Sample.createFromWav(file, false, false, -20, 0, 0);

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
        Assertions.assertEquals(Short.MIN_VALUE / 10, min);
        Assertions.assertEquals(Short.MAX_VALUE / 10, max);
    }

    @Test
    void trim() throws IOException, UnsupportedAudioFileException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sine1s44khz.wav");
        assert url != null;
        File file = new File(url.getFile());

        Sample sample = Sample.createFromWav(file, false, false, 0, 0, 0);
        Assertions.assertEquals(11467, sample.untrimmedLengthInSamples());
        Assertions.assertEquals(11467, sample.lengthInSamples());

        sample = Sample.createFromWav(file, false, false, 0, 1, 0);
        Assertions.assertEquals(11467, sample.untrimmedLengthInSamples());
        Assertions.assertEquals(11435, sample.lengthInSamples());

        sample = Sample.createFromWav(file, false, false, 0, 10000, 0);
        Assertions.assertEquals(11467, sample.untrimmedLengthInSamples());
        Assertions.assertEquals(32, sample.lengthInSamples());
    }

    @Test
    void pitch() throws IOException, UnsupportedAudioFileException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sine1s44khz.wav");
        assert url != null;
        File file = new File(url.getFile());

        Sample sample = Sample.createFromWav(file, false, false, 0, 0, 0);
        Assertions.assertEquals(11467, sample.lengthInSamples());

        // octave down
        sample = Sample.createFromWav(file, false, false, 0, 0, -12);
        Assertions.assertEquals(11467 * 2 + 1, sample.lengthInSamples());

        // octave up
        sample = Sample.createFromWav(file, false, false, 0, 0, 12);
        Assertions.assertEquals(11467 / 2, sample.lengthInSamples());
    }
}