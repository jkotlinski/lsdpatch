
/** Copyright (C) 2001-2011 by Johan Kotlinski

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE. */

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import sun.audio.AudioData;

public class Sound {

	private static ArrayList<Clip> previousClips = new ArrayList<Clip>();
	// Plays 4-bit packed Game Boy sample. Returns unpacked data.
	static byte[] play(byte[] gbSample) throws LineUnavailableException {
		AudioFormat upsampled_format = new AudioFormat(48000, 8, 1, false, false);
		byte upsampled_data[] = new byte[(int)(gbSample.length * (48000/ 11468.))*2];
		for (int i = 0; i < upsampled_data.length; i+=2) {
			double ratio = i / (double)(upsampled_data.length);

			
			int sampleIndex = (int) (ratio * gbSample.length);
			byte sample = gbSample[sampleIndex];
			upsampled_data[i] = (byte) (0xf0 & sample);
			upsampled_data[i + 1] = (byte) ((0xf & sample) << 4);
			
			
			// Emulates Game Boy sound chip bug. While changing waveform,
			// sound is played back at zero DC. This happens every 32'nd sample.
			if (sampleIndex % 32 == 0) {
				upsampled_data[i] = 0x78;
			}
		}

		byte data[] = new byte[gbSample.length * 2];
		for (int i = 0; i < gbSample.length; ++i) {
			data[i * 2] = (byte) (0xf0 & gbSample[i]);
			data[i * 2 + 1] = (byte) ((0xf & gbSample[i]) << 4);
		}

		// Play it!
		Clip clip = AudioSystem.getClip();
		clip.open(upsampled_format, upsampled_data, 0, upsampled_data.length);
		clip.start();
		previousClips.add(clip);
		for(int i = previousClips.size() - 2; i >= 0; i--) {
			if(!previousClips.get(i).isRunning()) {
				previousClips.get(i).close();
				previousClips.remove(i);
			}
		}

		return data;
	}
}
