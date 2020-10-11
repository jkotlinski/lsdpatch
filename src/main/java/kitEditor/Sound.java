// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();

    private static byte[] preProcessNibblesIntoWaveData(byte[] gbSample) {
        byte[] waveData = new byte[gbSample.length * 2];
        int src = 0;
        int dst = 0;

        while (src < gbSample.length) {
            byte sample = gbSample[src++];
            waveData[dst++] = (byte) (0xf0 & sample);
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
    static void play(byte[] gbSample, float volume) throws LineUnavailableException {
        AudioFormat upsampledFormat = new AudioFormat(11468, 8, 1, false, false);
        byte[] upsampledData = preProcessNibblesIntoWaveData(gbSample);

        // Play it!
        Clip clip = getClip();
        clip.open(upsampledFormat, upsampledData, 0, upsampledData.length);
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float result = 20f * (float) Math.log10(volume);
        control.setValue(result);
        clip.start();
    }
}
