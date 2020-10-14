// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();

    private static byte[] nibblesToWaveData(byte[] gbSample) {
        byte[] waveData = new byte[gbSample.length * 4];
        int src = 0;
        int dst = 0;

        while (src < gbSample.length) {
            byte sample = gbSample[src++];
            waveData[dst++] = 0;
            waveData[dst++] = (byte)(0xf0 & sample);
            waveData[dst++] = 0;
            waveData[dst++] = (byte)((0x0f & sample) << 4);
        }
        for (int i = 1; i < waveData.length; i += 2) {
            waveData[i] += 128;
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

    /**
     * Plays 4-bit packed Game Boy sample. Returns unpacked data.
     *
     * @throws LineUnavailableException
     */
    @SuppressWarnings("JavaDoc")
    static void play(byte[] gbSample, boolean halfSpeed) throws LineUnavailableException {
        final int sampleRate = halfSpeed ? 5734 : 11468;
        byte[] waveData = nibblesToWaveData(gbSample);
        Clip clip = getClip();
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, false, false);
        clip.open(audioFormat, waveData, 0, waveData.length);
        clip.start();

        for (Clip otherClip : clipPool) {
            if (otherClip != clip) {
                otherClip.stop();
            }
        }
    }
}
