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

// Sample bank creator.

import java.io.*;

class sbc {

    public static int DITHER_VAL=0x8;

    //outfile=dst, inSample=8bit unsigned sample 11468 kHz
    public static void handle(byte dst[], Sample samples[], int byteLength[]) {
        int offset=0x60; //don't overwrite samplebank info!
        for (int sampleIt = 0; sampleIt < samples.length; sampleIt++) {
            Sample sample = samples[sampleIt];
            if (sample == null) {
                break;
            }

            sample.seekStart();
            int sampleLength = sample.length();
            // Trims the end of the sample to make it a multiple of 0x10.
            sampleLength -= sampleLength % 0x10;

            int addedBytes=0;

            int outbuf[]=new int[32];
            int outcounter=0;
            for (int i = 0; i < sampleLength; i++) {
                outbuf[outcounter] = sample.read();
                if (sample.mayDither) {
                    outbuf[outcounter] += Math.random()*DITHER_VAL-DITHER_VAL/2;
                }
                //outbuf[outcounter]+=outcounter%2*DITHER_VAL-DITHER_VAL/2;
                //throw away 4 LSB
                outbuf[outcounter]/=16;

                /*
                //this is to get middle at 7.5 instead of 8.0
                outbuf[outcounter]*=0xe;
                outbuf[outcounter]/=0xf;
                */

                //range check
                outbuf[outcounter]=Math.min(0xf,outbuf[outcounter]);
                outbuf[outcounter]=Math.max(0,outbuf[outcounter]);

                if (outcounter == 31) {
                    for(int j=0;j!=32;j+=2) {
                        dst[offset++]=(byte)(outbuf[j]*0x10+outbuf[j+1]);
                    }
                    outcounter = -1;
                    addedBytes += 0x10;
                }
                outcounter++;
            }

            byteLength[sampleIt] = addedBytes;
        }
    }
}
