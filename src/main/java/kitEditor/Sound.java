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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public class Sound {

    private final ArrayList<Clip> previousClips = new ArrayList<>();

    public static byte[] preProcessNibblesIntoWaveData(byte[] gbSample) {
        byte upsampledData[] = new byte[(int) (gbSample.length * (48000 / 11468.)) * 2];
        for (int i = 0; i < upsampledData.length; i++) {
            double ratio = i / (double) (upsampledData.length);


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
        return upsampledData;
    }

    private Clip getFirstAvailableClip() throws LineUnavailableException {
        for (Clip clip : previousClips) {
            if (!clip.isRunning())
            {
                return clip;
            }
        }
        return AudioSystem.getClip();
    }

    /**
     * Plays 4-bit packed Game Boy sample. Returns unpacked data.
     *
     * @throws LineUnavailableException
     */
    @SuppressWarnings("JavaDoc")
    public byte[] play(byte[] gbSample) throws LineUnavailableException {
        AudioFormat upsampledFormat = new AudioFormat(48000, 8, 1, false, false);
        byte[] upsampledData = preProcessNibblesIntoWaveData(gbSample);
        byte data[] = new byte[gbSample.length * 2];
        for (int i = 0; i < gbSample.length; ++i) {
            data[i * 2] = (byte) (0xf0 & gbSample[i]);
            data[i * 2 + 1] = (byte) ((0xf & gbSample[i]) << 4);
        }

        // Play it!
        Clip clip = getFirstAvailableClip();
        clip.open(upsampledFormat, upsampledData, 0, upsampledData.length);
        clip.start();
        return data;
    }
}
