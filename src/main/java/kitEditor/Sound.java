// Copyright (C) 2001, Johan Kotlinski

package kitEditor;

import java.util.ArrayList;
import javax.sound.sampled.*;

public class Sound {

    private static final ArrayList<Clip> clipPool = new ArrayList<>();

    private static final long WAV_SAMPLE_RATE = 48000L;
    private static final long LSDJ_SAMPLE_RATE = 11468L;

    private static byte[] preProcessNibblesIntoWaveData(byte[] gbSample) {
        long numSamples = ((long)gbSample.length * WAV_SAMPLE_RATE);
        long numNibblePairsToWrite = (numSamples/LSDJ_SAMPLE_RATE)*2L;

        byte[] upsampledData = new byte[(int) (numNibblePairsToWrite * 2L)];

        for (int i = 0; i < upsampledData.length/2; i++) {
            double ratio = i / (double) (upsampledData.length/2);

            int sampleIndex = (int) (ratio * gbSample.length);
            int nibble = (int) (ratio * gbSample.length * 2.) % 2;
            byte sample = gbSample[sampleIndex];
            if (nibble == 0)
                upsampledData[i] = (byte) (0xf0 & sample);
            else
                upsampledData[i] = (byte) ((0x0F & sample) << 4);
        }
        for (int i = upsampledData.length/2; i < upsampledData.length; ++i) {
            upsampledData[i] = -128;
        }
        return upsampledData;
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
        AudioFormat upsampledFormat = new AudioFormat(48000, 8, 1, false, false);
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
