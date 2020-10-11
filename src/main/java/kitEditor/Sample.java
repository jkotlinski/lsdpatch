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
    private final byte[] buf;
    private int readPos;

    private Sample(byte[] iBuf, String iName) {
        buf = iBuf;
        name = iName;
    }

    String getName() {
        return name;
    }

    int length() {
        return buf.length;
    }

    void seekStart() {
        readPos = 0;
    }

    int read() {
        int val = buf[readPos++];
        // Converts from signed to unsigned 8-bit.
        val += 0x80;
        return val;
    }

    // ------------------

    static Sample createFromNibbles(byte[] nibbles, String name) {
        byte[] buf = new byte[nibbles.length * 2];
        for (int nibbleIt = 0; nibbleIt < nibbles.length; ++nibbleIt) {
            buf[2 * nibbleIt] = (byte) (nibbles[nibbleIt] & 0xf0);
            buf[2 * nibbleIt + 1] = (byte) ((nibbles[nibbleIt] & 0xf) << 4);
        }
        for (int bufIt = 0; bufIt < buf.length; ++bufIt) {
            buf[bufIt] -= 0x80;
        }
        return new Sample(buf, name);
    }

    // ------------------

    static Sample createFromWav(File file) throws IOException, UnsupportedAudioFileException {
        ArrayList<Integer> samples = readSamples(file);
        normalize(samples);
        dither(samples);

        byte[] buf = new byte[samples.size()];
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = (byte)(int)samples.get(i);
        }
        return new Sample(buf, file.getName());
    }

    private static ArrayList<Integer> readSamples(File file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat outFormat = new AudioFormat(11468, 8, 1, true, false);
        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(outFormat, ais);
        ArrayList<Integer> samples = new ArrayList<>();
        while (true) {
            int b = convertedAis.read();
            if (b == -1) {
                break;
            }
            b = (byte)b;
            samples.add(b);
        }
        return samples;
    }

    private static void dither(ArrayList<Integer> samples) {
        PinkNoise pinkNoise = new PinkNoise(1);
        for (int i = 0; i < samples.size(); ++i) {
            int s = samples.get(i);
            s += pinkNoise.nextValue() * 4;
            s = Math.max(-128, Math.min(s, 127));
            samples.set(i, s);
        }
    }

    private static void normalize(ArrayList<Integer> samples) {
        int peak = Integer.MIN_VALUE;
        for (Integer sample : samples) {
            peak = Math.max(peak, Math.abs(sample));
        }
        if (peak >= 127 || peak == 0) {
            return;
        }
        for (int i = 0; i < samples.size(); ++i) {
            int s = samples.get(i);
            s *= 127;
            s /= peak;
            samples.set(i, s);
        }
    }

    // ------------------

    void writeToWav(File f) {
        try {
            RandomAccessFile wavFile = new RandomAccessFile(f, "rw");

            int payloadSize = buf.length;
            int fileSize = buf.length + 0x2c;
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

            byte[] unsigned = new byte[buf.length];
            for (int it = 0; it < buf.length; ++it) {
                unsigned[it] = (byte) ((int) buf[it] + 0x88);
            }
            wavFile.write(unsigned);
            wavFile.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "File error : " +e.getCause(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

