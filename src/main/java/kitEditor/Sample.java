package kitEditor;

import java.io.*;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

class Sample {
    private final File file;
    private final String name;
    private short[] samples;
    private int readPos;
    private int volumeDb = 0;

    private Sample(File file, short[] iBuf, String iName) {
        this.file = file;
        if (iBuf != null) {
            for (int j : iBuf) {
                assert (j >= Short.MIN_VALUE);
                assert (j <= Short.MAX_VALUE);
            }
            samples = iBuf;
        }
        name = iName;
    }

    public String getName() {
        return name;
    }

    public int lengthInSamples() {
        return samples.length;
    }

    public int lengthInBytes() {
        int l = lengthInSamples() / 2;
        l -= l % 0x10;
        return l;
    }

    public void seekStart() {
        readPos = 0;
    }

    public short read() {
        return samples[readPos++];
    }

    public boolean canAdjustVolume() {
        return file != null;
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
        return new Sample(null, buf, name);
    }

    // ------------------

    static Sample createFromWav(File file, boolean dither) throws IOException, UnsupportedAudioFileException {
        Sample s = new Sample(file, null, file.getName());
        s.readFromFile(dither);
        return s;
    }

    private void readFromFile(boolean dither) throws IOException, UnsupportedAudioFileException {
        ArrayList<Integer> samples = readSamples(file);
        normalize(samples);
        if (dither) {
            dither(samples);
        }
        this.samples = new short[samples.size()];
        for (int i = 0; i < samples.size(); ++i) {
            this.samples[i] = (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, samples.get(i)));
        }
        blendWaveFrames();
    }

    /* Due to Game Boy audio bug, the first sample in a frame is played
     * back using the same value as the last completed sample in previous
     * frame. To reduce error, average these samples.
     */
    private void blendWaveFrames() {
        for (int i = 0x20; i < samples.length; i += 0x20) {
            int n = 2; // Tested on DMG-01 with 440 Hz sine wave.
            short avg = (short) ((samples[i] + samples[i - n]) / 2);
            samples[i] = avg;
            samples[i - n] = avg;
        }
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
            samples.set(i, s);
        }
    }

    private void normalize(ArrayList<Integer> samples) {
        double peak = Double.MIN_VALUE;
        for (Integer sample : samples) {
            double s = sample;
            s = s < 0 ? s / Short.MIN_VALUE : s / Short.MAX_VALUE;
            peak = Math.max(s, peak);
        }
        if (peak == 0) {
            return;
        }
        double volumeAdjust = Math.pow(10, volumeDb / 20.0);
        for (int i = 0; i < samples.size(); ++i) {
            samples.set(i, (int)((samples.get(i) * volumeAdjust) / peak));
        }
    }

    public int volumeDb() {
        return volumeDb;
    }

    public void setVolumeDb(int value) throws IOException, UnsupportedAudioFileException {
        volumeDb = value;
        readFromFile(true);
    }
}
