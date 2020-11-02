package kitEditor;

import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;

class Sample {
    private File file;
    private final String name;
    private short[] originalSamples;
    private short[] processedSamples;
    private int untrimmedLengthInSamples = -1;
    private int readPos;
    private int volumeDb = 0;
    private int trim = 0;
    private boolean dither = true;

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

    public int untrimmedLengthInSamples() {
        return untrimmedLengthInSamples == -1 ? lengthInSamples() : untrimmedLengthInSamples;
    }

    public short[] workSampleData() {
        return (originalSamples != null ? originalSamples : processedSamples).clone();
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

    public static Sample createFromWav(File file, boolean dither, boolean halfSpeed, int volumeDb, int trim)
            throws IOException, UnsupportedAudioFileException {
        Sample s = new Sample(null, file.getName().split("\\.")[0]);
        s.file = file;
        s.dither = dither;
        s.volumeDb = volumeDb;
        s.trim = trim;
        s.reload(halfSpeed);
        return s;
    }

    public void reload(boolean halfSpeed) throws IOException, UnsupportedAudioFileException {
        if (file == null) {
            return;
        }
        originalSamples = readSamples(file, halfSpeed);
        processSamples(dither);
    }

    public void processSamples(boolean dither) {
        int[] intBuffer = toIntBuffer(originalSamples);
        normalize(intBuffer);
        intBuffer = trim(intBuffer);
        if (dither) {
            dither(intBuffer);
        }
        processedSamples = toShortBuffer(intBuffer);
        blendWaveFrames(processedSamples);
}

    private int[] trim(int[] intBuffer) {
        int headPos = headPos(intBuffer);
        int tailPos = tailPos(intBuffer);
        if (headPos > tailPos) {
            return intBuffer;
        }
        untrimmedLengthInSamples = tailPos + 1 - headPos;
        tailPos = Math.max(headPos, tailPos - trim * 32);
        int[] newBuffer = new int[tailPos + 1 - headPos];
        System.arraycopy(intBuffer, headPos, newBuffer, 0, newBuffer.length);
        return newBuffer;
    }

    final int SILENCE_THRESHOLD = Short.MAX_VALUE / 16;

    private int headPos(int[] buf) {
        int i;
        for (i = 0; i < buf.length; ++i) {
            if (Math.abs(buf[i]) >= SILENCE_THRESHOLD) {
                break;
            }
        }
        return i;
    }

    private int tailPos(int[] buf) {
        int i;
        for (i = buf.length - 1; i >= 0; --i) {
            if (Math.abs(buf[i]) >= SILENCE_THRESHOLD) {
                break;
            }
        }
        return i;
    }

    private short[] toShortBuffer(int[] intBuffer) {
        short[] shortBuffer = new short[intBuffer.length];
        for (int i = 0; i < intBuffer.length; ++i) {
            int s = intBuffer[i];
            s = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, s));
            shortBuffer[i] = (short)s;
        }
        return shortBuffer;
    }

    private int[] toIntBuffer(short[] shortBuffer) {
        int[] intBuffer = new int[shortBuffer.length];
        for (int i = 0; i < shortBuffer.length; ++i) {
            intBuffer[i] = shortBuffer[i];
        }
        return intBuffer;
    }

    /* Due to the Game Boy wave refresh bug, the first sample in a frame is
     * played back using the last completed sample in previous frame.
     * To reduce the resulting noise, average these samples.
     *
     * Note: This code introduces noise on emulators that do not implement
     * the Game Boy wave refresh bug.
     */
    private static void blendWaveFrames(short[] samples) {
        for (int i = 0x20; i < samples.length; i += 0x20) {
            int n = 2; // Tested on DMG-01 with 440 Hz sine wave.
            samples[i - n] = (short) ((samples[i] + samples[i - n]) / 2);
        }
    }

    private static short[] readSamples(File file, boolean halfSpeed) throws UnsupportedAudioFileException, IOException {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        float inSampleRate = ais.getFormat().getSampleRate();
        AudioFormat outFormat = new AudioFormat(inSampleRate, 16, 1, true, false);
        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(outFormat, ais);
        byte[] b = new byte[convertedAis.available()];
        int read = convertedAis.read(b);
        assert read == b.length;
        short[] samples = new short[b.length / 2];
        for (int i = 0; i < samples.length; ++i) {
            samples[i] = (short) ((b[i * 2 + 1] * 256) + ((short)b[i * 2] & 0xff));
        }
        convertedAis.close();
        ais.close();

        float outSampleRate = halfSpeed ? 5734 : 11468;
        return Sound.resample(inSampleRate, outSampleRate, samples);
    }

    // Adds triangular probability density function dither noise.
    private void dither(int[] samples) {
        Random random = new Random();
        float state = random.nextFloat();
        for (int i = 0; i < samples.length; ++i) {
            int value = samples[i];
            float r = state;
            state = random.nextFloat();
            int noiseLevel = 256 * 16;
            value += (r - state) * noiseLevel;
            samples[i] = value;
        }
    }

    private void normalize(int[] samples) {
        double peak = Double.MIN_VALUE;
        for (int sample : samples) {
            double s = sample;
            s = s < 0 ? s / Short.MIN_VALUE : s / Short.MAX_VALUE;
            peak = Math.max(s, peak);
        }
        if (peak == 0) {
            return;
        }
        double volumeAdjust = Math.pow(10, volumeDb / 20.0);
        for (int i = 0; i < samples.length; ++i) {
            samples[i] = (int)((samples[i] * volumeAdjust) / peak);
        }
    }

    public int volumeDb() {
        return volumeDb;
    }

    public void setVolumeDb(int value) {
        volumeDb = value;
    }

    public File getFile() {
        return file;
    }

    public void setTrim(int value) {
        trim = value;
    }

    public int getTrim() {
        return trim;
    }
}
