// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();
    private static final int PLAYBACK_RATE = 48000;

    private static byte[] unpackNibbles(byte[] gbSample) {
        byte[] waveData = new byte[gbSample.length * 2];
        int src = 0;
        int dst = 0;

        while (src < gbSample.length) {
            byte sample = gbSample[src++];
            waveData[dst++] = (byte) (0xf0 & sample);
            waveData[dst++] = (byte) ((0x0f & sample) << 4);
        }
        return waveData;
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
        byte[] waveData = to16Bit(resampleToPlaybackRate(sampleRate, unpackNibbles(gbSample)));
        Clip clip = getClip();
        clip.open(new AudioInputStream(new ByteArrayInputStream(waveData),
                new AudioFormat(PLAYBACK_RATE, 16, 1, true, false),
                waveData.length));
        clip.start();
    }

    static void stopAll() {
        for (Clip clip : clipPool) {
            clip.stop();
        }
    }

    private static byte[] to16Bit(float[] waveData) {
        byte[] dst = new byte[waveData.length * 2];
        for (int i = 0; i < waveData.length; ++i) {
            float sf = waveData[i];
            assert sf >= -1;
            assert sf <= 1;
            short si = (short)(sf * Short.MAX_VALUE);
            dst[i * 2] = (byte)si;
            dst[i * 2 + 1] = (byte)(si >> 8);
        }
        return dst;
    }

    // Bespoke resampling routine is a sad necessity since not all systems support
    // playback of 11468 Hz sample data.
    private static float[] resampleToPlaybackRate(int srcSampleRate, byte[] srcBuffer) {
        float[] dstBuffer = new float[(srcBuffer.length * PLAYBACK_RATE) / srcSampleRate];
        for (int dst = 0; dst < dstBuffer.length; ++dst) {
            double srcPos = (double)dst * srcSampleRate / PLAYBACK_RATE;
            int pos1 = (int)Math.floor(srcPos);
            int pos2 = 1 + pos1;
            float value = ((int)srcBuffer[pos1]) & 0xf0;
            if (pos2 < srcBuffer.length) {
                value *= (1 - srcPos % 1);
                value += (((int)srcBuffer[pos2]) & 0xf0) * (srcPos % 1);
            }
            dstBuffer[dst] = (value - 0x78) / 0x78;
            assert dstBuffer[dst] <= 1;
            assert dstBuffer[dst] >= -1;
        }
        return dstBuffer;
    }
}
