package kitEditor;

import java.io.*;
import java.util.ArrayList;
import javax.sound.sampled.*;

class Sample {
    private final String name;
    private ArrayList<Integer> originalSamples;
    private short[] processedSamples;
    private int readPos;
    private int volumeDb = 0;

    public Sample(short[] iBuf, String iName) {
        if (iBuf != null) {
            for (int j : iBuf) {
                assert (j >= Short.MIN_VALUE);
                assert (j <= Short.MAX_VALUE);
            }
            processedSamples = iBuf;
        }
        name = iName;
    }

    public String getName() {
        return name;
    }

    public int lengthInSamples() {
        return processedSamples.length;
    }

    public short[] workSampleData() {
        short[] samples = new short[lengthInSamples()];
        if (originalSamples != null) {
            for (int i = 0; i < lengthInSamples(); ++i) {
                samples[i] = (short) (int) originalSamples.get(i);
            }
        } else {
            if (lengthInSamples() >= 0) {
                System.arraycopy(processedSamples, 0, samples, 0, lengthInSamples());
            }
        }
        return samples;
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
        return processedSamples[readPos++];
    }

    public boolean canAdjustVolume() {
        return originalSamples != null;
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

    public static Sample createFromWav(File file, boolean dither) throws IOException, UnsupportedAudioFileException {
        Sample s = new Sample(null, file.getName());
        s.originalSamples = readSamples(file);
        s.processSamples(dither);
        return s;
    }

    public static Sample createFromOriginalSamples(short[] pcm, String name, int volume) {
        Sample sample = new Sample(null, name);
        sample.setVolumeDb(volume);
        ArrayList<Integer> intPcm = new ArrayList<>();
        for (short s : pcm) {
            intPcm.add((int)s);
        }
        sample.originalSamples = intPcm;
        sample.processSamples(true);
        return sample;
    }

    public void processSamples(boolean dither) {
        ArrayList<Integer> samples = new ArrayList<>(originalSamples);
        normalize(samples);
        if (dither) {
            dither(samples);
        }
        processedSamples = new short[samples.size()];
        for (int i = 0; i < samples.size(); ++i) {
            processedSamples[i] = (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, samples.get(i)));
        }
        blendWaveFrames();
    }

    /* Due to Game Boy audio bug, the first sample in a frame is played
     * back using the same value as the last completed sample in previous
     * frame. To reduce error, average these samples.
     */
    private void blendWaveFrames() {
        for (int i = 0x20; i < processedSamples.length; i += 0x20) {
            int n = 2; // Tested on DMG-01 with 440 Hz sine wave.
            short avg = (short) ((processedSamples[i] + processedSamples[i - n]) / 2);
            processedSamples[i] = avg;
            processedSamples[i - n] = avg;
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

    public void setVolumeDb(int value) {
        volumeDb = value;
    }
}
