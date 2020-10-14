package kitEditor;

import java.io.*;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

class Sample {
    private final String name;
    private final short[] samples; // Signed 16-bit PCM.
    private int readPos;

    private Sample(short[] iBuf, String iName) {
        for (int j : iBuf) {
            assert (j >= Short.MIN_VALUE);
            assert (j <= Short.MAX_VALUE);
        }
        samples = iBuf;
        name = iName;
    }

    String getName() {
        return name;
    }

    int length() {
        return samples.length;
    }

    void seekStart() {
        readPos = 0;
    }

    short read() {
        return samples[readPos++];
    }

    // ------------------

    static Sample createFromNibbles(byte[] nibbles, String name) {
        short[] buf = new short[nibbles.length * 2];
        for (int nibbleIt = 0; nibbleIt < nibbles.length; ++nibbleIt) {
            buf[2 * nibbleIt] = (byte) (nibbles[nibbleIt] & 0xf0);
            buf[2 * nibbleIt + 1] = (byte) ((nibbles[nibbleIt] & 0xf) << 4);
        }
        for (int bufIt = 0; bufIt < buf.length; ++bufIt) {
            short s = (byte)(buf[bufIt] - 0x80);
            s *= 256;
            buf[bufIt] = s;
        }
        return new Sample(buf, name);
    }

    // ------------------

    static Sample createFromWav(File file, boolean dither) throws IOException, UnsupportedAudioFileException {
        ArrayList<Short> samples = readSamples(file);
        normalize(samples);
        if (dither) {
            dither(samples);
        }
        short[] shortSamples = new short[samples.size()];
        for (int i = 0; i < samples.size(); ++i) {
            shortSamples[i] = samples.get(i);
        }

        /* Due to Game Boy audio bug, the first sample in a frame is played
         * back using the same value as the last completed sample in previous
         * frame. To reduce error, average these samples.
         */
        for (int i = 0x20; i < shortSamples.length; i += 0x20) {
            int n = 2; // Tested on DMG-01 with 440 Hz sine wave.
            short avg = (short) ((shortSamples[i] + shortSamples[i - n]) / 2);
            shortSamples[i] = avg;
            shortSamples[i - n] = avg;
        }

        return new Sample(shortSamples, file.getName());
    }

    private static ArrayList<Short> readSamples(File file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat outFormat = new AudioFormat(11468, 16, 1, true, false);
        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(outFormat, ais);
        ArrayList<Short> samples = new ArrayList<>();
        while (true) {
            byte[] buf = new byte[2];
            if (convertedAis.read(buf) < 2) {
                break;
            }
            short sample = buf[1];
            sample *= 256;
            sample += (int)buf[0] & 0xff;
            samples.add(sample);
        }
        return samples;
    }

    private static void dither(ArrayList<Short> samples) {
        PinkNoise pinkNoise = new PinkNoise(1);
        for (int i = 0; i < samples.size(); ++i) {
            int s = samples.get(i);
            final double noiseLevel = 256 * 4; // ad hoc.
            s += pinkNoise.nextValue() * noiseLevel;
            s = Math.max(Short.MIN_VALUE, Math.min(s, Short.MAX_VALUE));
            samples.set(i, (short)s);
        }
    }

    private static void normalize(ArrayList<Short> samples) {
        double peak = Double.MIN_VALUE;
        for (Short sample : samples) {
            double s = sample;
            s = s < 0 ? s / Short.MIN_VALUE : s / Short.MAX_VALUE;
            peak = Math.max(s, peak);
        }
        if (peak == 0) {
            return;
        }
        for (int i = 0; i < samples.size(); ++i) {
            samples.set(i, (short)(samples.get(i) / peak));
        }
    }
}
