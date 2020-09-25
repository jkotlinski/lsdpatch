package kitEditor;/*
  Copyright (C) 2001-2011 by Johan Kotlinski
  <p>
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  <p>
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  <p>
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */

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

            // Emulates Game Boy sound chip bug. While changing waveform,
            // sound is played back at zero DC. This happens every 32'nd sample.
            if (sampleIndex % 32 == 0) {
                upsampledData[i] = 0x78;
            }
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
