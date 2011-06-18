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

import javax.sound.sampled.*;

public class Sound {

    static void play(byte[] gbSample) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(11468, 8, 1, false, false);
        byte data[] = new byte[gbSample.length * 2];
        for (int i = 0; i < gbSample.length; ++i) {
            data[i * 2] = (byte)(0xf0 & gbSample[i]);
            data[i * 2 + 1] = (byte)((0xf & gbSample[i]) << 4);
        }
        Clip clip = AudioSystem.getClip();
        clip.open(format, data, 0, data.length);
        clip.start();
    }
}
