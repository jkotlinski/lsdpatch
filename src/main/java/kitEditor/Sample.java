package kitEditor;

import java.io.*;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

class Sample {
    private final String name;
    private final int[] intBuf; // Signed 16-bit PCM.
    private int readPos;

    private Sample(int[] iBuf, String iName) {
        for (int j : iBuf) {
            assert (j >= Short.MIN_VALUE);
            assert (j <= Short.MAX_VALUE);
        }
        intBuf = iBuf;
        name = iName;
    }

    String getName() {
        return name;
    }

    int length() {
        return intBuf.length;
    }

    void seekStart() {
        readPos = 0;
    }

    int readInt() {
        return intBuf[readPos++];
    }

    // ------------------

    static Sample createFromNibbles(byte[] nibbles, String name) {
        int[] buf = new int[nibbles.length * 2];
        for (int nibbleIt = 0; nibbleIt < nibbles.length; ++nibbleIt) {
            buf[2 * nibbleIt] = (byte) (nibbles[nibbleIt] & 0xf0);
            buf[2 * nibbleIt + 1] = (byte) ((nibbles[nibbleIt] & 0xf) << 4);
        }
        for (int bufIt = 0; bufIt < buf.length; ++bufIt) {
            int s = (byte)(buf[bufIt] - 0x80);
            s *= 256;
            buf[bufIt] = s;
        }
        return new Sample(buf, name);
    }

    // ------------------

    static Sample createFromWav(File file, boolean dither) throws IOException, UnsupportedAudioFileException {
        ArrayList<Integer> samples = readSamples(file);
        normalize(samples);
        if (dither) {
            dither(samples);
        }
        int[] samplesInt = new int[samples.size()];
        for (int i = 0; i < samples.size(); ++i) {
            samplesInt[i] = samples.get(i);
        }

        /* Due to Game Boy audio bug, the first sample in a frame is played
         * back using the same value as the last completed sample in previous
         * frame. To reduce error, average these samples.
         */
        for (int i = 0x20; i < samplesInt.length; i += 0x20) {
            int n = 2; // Tested on DMG-01 with 440 Hz sine wave.
            int avg = (samplesInt[i] + samplesInt[i - n]) / 2;
            samplesInt[i] = avg;
            samplesInt[i - n] = avg;
        }

        return new Sample(samplesInt, file.getName());
    }

    private static ArrayList<Integer> readSamples(File file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat outFormat = new AudioFormat(11468, 16, 1, true, false);
        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(outFormat, ais);
        ArrayList<Integer> samples = new ArrayList<>();
        while (true) {
            byte[] buf = new byte[2];
            if (convertedAis.read(buf) < 2) {
                break;
            }
            int sample = buf[1];
            sample *= 256;
            sample += (int)buf[0] & 0xff;
            samples.add(sample);
        }
        return samples;
    }

    private static void dither(ArrayList<Integer> samples) {
        PinkNoise pinkNoise = new PinkNoise(1);
        for (int i = 0; i < samples.size(); ++i) {
            int s = samples.get(i);
            final double noiseLevel = 256 * 4; // ad hoc.
            s += pinkNoise.nextValue() * noiseLevel;
            s = Math.max(Short.MIN_VALUE, Math.min(s, Short.MAX_VALUE));
            samples.set(i, s);
        }
    }

    private static void normalize(ArrayList<Integer> samples) {
        double peak = Double.MIN_VALUE;
        for (Integer sample : samples) {
            double s = sample;
            s = s < 0 ? s / Short.MIN_VALUE : s / Short.MAX_VALUE;
            peak = Math.max(s, peak);
        }
        if (peak == 0) {
            return;
        }
        for (int i = 0; i < samples.size(); ++i) {
            samples.set(i, (int)(samples.get(i) / peak));
        }
    }
}
