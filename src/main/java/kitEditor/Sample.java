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

    static Sample createFromWav(File file) throws IOException, UnsupportedAudioFileException {
        ArrayList<Integer> samples = readSamples(file);
        normalize(samples);
        dither(samples);
        int[] samplesInt = new int[samples.size()];
        for (int i = 0; i < samples.size(); ++i) {
            samplesInt[i] = samples.get(i);
        }

        // Due to Game Boy audio bug, the first sample in a frame is played
        // back using the same value as a sample in previous frame.
        // To reduce error, let's try to average these samples.
        for (int i = 0x20; i < samplesInt.length; i += 0x20) {
            int n = 3; // Tested on DMG-01 with 240 Hz sine wave.
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
            /* The noise level was selected so that it will not
             * be heard during silent parts of the sample. It is
             * still enough to reduce bit-reduction harmonics by
             * a couple of decibels.
             */
            final double noiseLevel = 0.2 * 256;
            s += pinkNoise.nextValue() * noiseLevel;
            s = Math.max(Short.MIN_VALUE, Math.min(s, Short.MAX_VALUE));
            samples.set(i, s);
        }
    }

    private static void normalize(ArrayList<Integer> samples) {
        int peak = Integer.MIN_VALUE;
        for (Integer sample : samples) {
            peak = Math.max(peak, Math.abs(sample));
        }
        if (peak == 0) {
            return;
        }
        for (int i = 0; i < samples.size(); ++i) {
            int s = samples.get(i);
            // Adds DC offset to avoid dithering noise on silent samples.
            s *= Short.MAX_VALUE - 512;
            s /= peak;
            s += 256;
            samples.set(i, s);
        }
    }

    // ------------------

    void writeToWav(File f) {
        try {
            RandomAccessFile wavFile = new RandomAccessFile(f, "rw");

            int payloadSize = intBuf.length;
            int fileSize = intBuf.length + 0x2c;
            int waveSize = fileSize - 8;

            byte[] header = {
                    0x52, 0x49, 0x46, 0x46,  // RIFF
                    (byte) waveSize,
                    (byte) (waveSize >> 8),
                    (byte) (waveSize >> 16),
                    (byte) (waveSize >> 24),
                    0x57, 0x41, 0x56, 0x45,  // WAVE
                    // --- fmt chunk
                    0x66, 0x6D, 0x74, 0x20,  // fmt
                    16, 0, 0, 0,  // fmt size
                    1, 0,  // pcm
                    1, 0,  // channel count
                    (byte) 0xcc, 0x2c, 0, 0,  // freq (11468 hz)
                    (byte) 0xcc, 0x2c, 0, 0,  // avg. bytes/sec
                    1, 0,  // block align
                    8, 0,  // bits per sample
                    // --- data chunk
                    0x64, 0x61, 0x74, 0x61,  // data
                    (byte) payloadSize,
                    (byte) (payloadSize >> 8),
                    (byte) (payloadSize >> 16),
                    (byte) (payloadSize >> 24)
            };

            wavFile.write(header);

            byte[] unsigned = new byte[intBuf.length];
            for (int it = 0; it < intBuf.length; ++it) {
                unsigned[it] = (byte) (intBuf[it] / 256 + 0x80);
            }
            wavFile.write(unsigned);
            wavFile.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "File error : " +e.getCause(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

