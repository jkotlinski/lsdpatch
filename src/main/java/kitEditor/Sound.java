// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import com.laszlosystems.libresample4j.Resampler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();
    private static final int PLAYBACK_RATE = 48000;

    private static short[] unpackNibbles(byte[] gbSample) {
        byte[] waveData = new byte[gbSample.length * 2];
        int src = 0;
        int dst = 0;

        while (src < gbSample.length) {
            byte sample = gbSample[src++];
            waveData[dst++] = (byte) (0xf0 & sample);
            waveData[dst++] = (byte) ((0x0f & sample) << 4);
        }

        short[] s = new short[waveData.length];
        for (int i = 0; i < s.length; ++i) {
            int v = waveData[i] & 0xf0;
            v -= 0x78;
            v *= Short.MAX_VALUE;
            v /= 0x78;
            s[i] = (short)v;
        }
        return s;
    }

    private static Clip getClip() throws LineUnavailableException {
        for (Clip clip : clipPool) {
            if (!clip.isRunning()) {
                clip.close();
                return clip;
            }
        }
        Clip newClip = AudioSystem.getClip();
        clipPool.add(newClip);
        return newClip;
    }

    static void play(byte[] gbSample, boolean halfSpeed) throws LineUnavailableException, IOException {
        final int sampleRate = halfSpeed ? 5734 : 11468;
        byte[] b = toByteArray(resampleNearestNeighbor(sampleRate, PLAYBACK_RATE, unpackNibbles(gbSample)));
        Clip clip = getClip();
        clip.open(new AudioInputStream(new ByteArrayInputStream(b),
                new AudioFormat(PLAYBACK_RATE, 16, 1, true, false),
                b.length / 2));
        clip.start();
    }

    private static short[] resampleNearestNeighbor(int srcRate, int dstRate, short[] src) {
        short[] dst = new short[dstRate * src.length / srcRate];
        for (int i = 0; i < dst.length; ++i) {
            dst[i] = src[i * srcRate / dstRate];
        }
        return dst;
    }

    private static byte[] toByteArray(short[] waveData) {
        byte[] b = new byte[waveData.length * 2];
        for (int i = 0; i < waveData.length; ++i) {
            b[i * 2] = (byte)(waveData[i] & 0xff);
            b[i * 2 + 1] = (byte)(waveData[i] >> 8);
        }
        return b;
    }

    static void stopAll() {
        for (Clip clip : clipPool) {
            clip.stop();
        }
    }

    public static short[] resample(double inSampleRate, double outSampleRate, short[] samples) {
        if (inSampleRate == outSampleRate) {
            return samples;
        }
        float[] inBuf = new float[samples.length];
        float dcOffset = 0;
        for (int i = 0; i < inBuf.length; ++i) {
            inBuf[i] = (float) samples[i] / -Short.MIN_VALUE;
            dcOffset += inBuf[i] / inBuf.length;
        }

        // Removes DC offset.
        for (int i = 0; i < inBuf.length; ++i) {
            inBuf[i] -= dcOffset;
        }

        double factor = outSampleRate / inSampleRate;
        float[] outBuf = new float[(int)(inBuf.length * factor + 1)];
        Resampler resampler = new Resampler(true, factor, factor);
        Resampler.Result result = resampler.process(factor, inBuf, 0, inBuf.length, true, outBuf, 0, outBuf.length);

        // avoid clipping
        float peak = 0;
        for (float v : outBuf) {
            peak = Math.max(peak, Math.abs(v));
        }
        if (peak > 1) {
            for (int i = 0; i < outBuf.length; ++i) {
                outBuf[i] /= peak;
            }
        }

        short[] finalBuf = new short[result.outputSamplesGenerated];
        for (int i = 0; i < finalBuf.length; ++i) {
            finalBuf[i] = (short)(outBuf[i] * Short.MAX_VALUE);
        }
        return finalBuf;
    }

}
