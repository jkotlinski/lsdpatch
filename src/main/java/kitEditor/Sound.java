// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();

    private static byte[] nibblesToWaveData(byte[] gbSample) {
        byte[] waveData = new byte[gbSample.length * 2];
        int src = 0;
        int dst = 0;

        while (src < gbSample.length) {
            byte sample = gbSample[src++];

            // Emulates Game Boy sound chip bug. While changing waveform,
            // sound is played back at zero DC. This happens every 32'nd sample.
            if (dst % 32 == 0) {
                waveData[dst] = 0x78;
            } else {
                waveData[dst] = (byte) (0xf0 & sample);
            }
            ++dst;
            waveData[dst++] = (byte) ((0x0F & sample) << 4);
        }
        for (int i = waveData.length / 2; i < waveData.length; ++i) {
            waveData[i] = -128;
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
    static void play(byte[] gbSample, float volume, boolean halfSpeed) throws LineUnavailableException {
        final int sampleRate = halfSpeed ? 5734 : 11468;
        byte[] waveData = nibblesToWaveData(gbSample);
        Clip clip = getClip();
        AudioFormat audioFormat = new AudioFormat(sampleRate, 8, 1, false, false);
        clip.open(audioFormat, waveData, 0, waveData.length);
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float result = 20f * (float) Math.log10(volume);
        control.setValue(result);
        clip.start();
    }
}
