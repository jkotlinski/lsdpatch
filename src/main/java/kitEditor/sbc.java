package kitEditor;

/**
 * Copyright (C) 2001-2011 by Johan Kotlinski
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Sample bank creator.

class sbc {

    static int DITHER_VAL = 0x8;

    //outfile=dst, inSample=8bit unsigned sample 11468 kHz
    public static void handle(byte dst[], Sample samples[], int byteLength[]) {
        int offset = 0x60; //don't overwrite sample bank info!
        for (int sampleIt = 0; sampleIt < samples.length; sampleIt++) {
            Sample sample = samples[sampleIt];
            if (sample == null) {
                break;
            }

            sample.seekStart();
            int sampleLength = sample.length();
            // Trims the end of the sample to make it a multiple of 0x10.
            sampleLength -= sampleLength % 0x10;

            int addedBytes = 0;

            int outputBuffer[] = new int[32];
            int outputCounter = 0;
            for (int i = 0; i < sampleLength; i++) {
                outputBuffer[outputCounter] = sample.read();
                if (sample.mayDither()) {
                    outputBuffer[outputCounter] += Math.random() * DITHER_VAL - DITHER_VAL / 2;
                }
                //outputBuffer[outputCounter]+=outputCounter%2*DITHER_VAL-DITHER_VAL/2;
                //throw away 4 LSB
                outputBuffer[outputCounter] /= 16;

                /*
                //this is to get middle at 7.5 instead of 8.0
                outputBuffer[outputCounter]*=0xe;
                outputBuffer[outputCounter]/=0xf;
                */

                //range check
                outputBuffer[outputCounter] = Math.min(0xf, outputBuffer[outputCounter]);
                outputBuffer[outputCounter] = Math.max(0, outputBuffer[outputCounter]);

                if (outputCounter == 31) {
                    for (int j = 0; j != 32; j += 2) {
                        dst[offset++] = (byte) (outputBuffer[j] * 0x10 + outputBuffer[j + 1]);
                    }
                    outputCounter = -1;
                    addedBytes += 0x10;
                }
                outputCounter++;
            }

            byteLength[sampleIt] = addedBytes;
        }
    }
}
