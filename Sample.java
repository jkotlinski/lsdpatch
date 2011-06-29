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

import java.io.*;
import javax.swing.*;

public class Sample {
    private String name;
    private byte[] buf;
    private int readPos;
    private boolean mayDither;

    public Sample(byte[] iBuf, String iName, boolean iMayDither) {
        buf = iBuf;
        name = iName;
        mayDither = iMayDither;
    }

    public String getName() {
        return name;
    }

    public boolean mayDither() {
        return mayDither;
    }

    public int length() {
        return buf.length;
    }

    public void seekStart() {
        readPos = 0;
    }

    public int read() {
        int val = buf[readPos++];
        // Converts from signed to unsigned 8-bit.
        val += 0x80;
        return val;
    }

    // ------------------

    static Sample createFromNibbles(byte[] nibbles, String name) {
        byte[] buf = new byte[nibbles.length * 2];
        for (int nibbleIt = 0; nibbleIt < nibbles.length; ++nibbleIt) {
            buf[2 * nibbleIt] = (byte)(nibbles[nibbleIt] & 0xf0);
            buf[2 * nibbleIt + 1] = (byte)((nibbles[nibbleIt] & 0xf) << 4);
        }
        for (int bufIt = 0; bufIt < buf.length; ++bufIt) {
            buf[bufIt] -= 0x80;
        }
        return new Sample(buf, name, false);
    }

    // ------------------

    static Sample createFromWav(File file) {
        int ch = 0;
        long sampleRate = 0;
        int bits = 0;

        try {
            FileInputStream in = new FileInputStream( file.getAbsolutePath() );

            long riffId = readWord(in);
            if (riffId != 1380533830) {
                JOptionPane.showMessageDialog(null,
                "Missing RIFF id!",
                "Format error",
                JOptionPane.ERROR_MESSAGE);
            }

            readWord(in); //skip file size

            long waveId = readWord(in);
            if (waveId != 1463899717) {
                JOptionPane.showMessageDialog(null,
                        "Missing WAVE id!",
                        "Format error",
                        JOptionPane.ERROR_MESSAGE);
            }

            while ( in.available() != 0 )
            {
                long chunkId = readWord(in);
                long chunkSize = readEndianWord(in);

                if ( chunkId == 0x666D7420 ) // fmt
                {
                    int compression = readEndianShort(in);
                    if (compression != 1) {
                        JOptionPane.showMessageDialog(null,
                                "Sample is compressed. Only PCM .wav files are supported.",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    ch = readEndianShort(in);
                    if (ch > 2) {
                        JOptionPane.showMessageDialog(null,
                                "Unsupported number of channels!",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    sampleRate = readEndianWord(in);
                    readWord(in); //avg. bytes/second
                    readEndianShort(in);  // Block align.
                    bits = readEndianShort(in);
                    if (bits != 16 && bits != 8) {
                        JOptionPane.showMessageDialog(null,
                                "Only 8-bit and 16-bit .wav are supported!",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                }
                else if ( chunkId == 0x64617461 ) // data
                {
                    byte[] buf = new byte[(int)chunkSize];
                    in.read(buf);

                    if (ch == 2) {
                        int inIt = 0;
                        int outIt = 0;
                        while ( inIt < chunkSize )
                        {
                            buf[outIt++] = buf[inIt++];
                            buf[outIt++] = buf[inIt++];
                            inIt += 2;
                        }
                        chunkSize /= 2;
                        ch = 1;
                    }

                    if (bits == 16) {
                        // Convert from signed 16-bit to signed 8-bit
                        // by simply taking the most significant byte.
                        int inIt = 1;
                        int outIt = 0;

                        while ( inIt < chunkSize ) {
                            buf[outIt] = buf[inIt];
                            outIt++;
                            inIt+=2;
                        }
                        chunkSize /= 2;
                    } else if (bits == 8) {
                        // Converts unsigned 8-bit to signed 8-bit.
                        for (int it = 0; it < chunkSize; ++it) {
                            buf[it] += 128;
                        }
                    }

                    int frames = (int)chunkSize;

                    int outFreq = 11468;
                    int outFrames = ( outFreq * frames ) / (int)sampleRate;

                    double readPos = 0.0;
                    double advance = (double)sampleRate / (double)outFreq;

                    byte[] outBuf = new byte[outFrames];
                    int writePos = 0;

                    while ( writePos < outFrames )
                    {
                        byte val = buf[(int)readPos];
                        outBuf[writePos++] = val;
                        readPos += advance;
                    }

                    return new Sample(outBuf, file.getName(), true);
                }
                else
                {
                    in.skip(chunkSize);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    static private long readWord(FileInputStream in) throws IOException {
        long ret = 0;
        byte[] word = new byte[4];
        in.read ( word );

        ret += word[0];
        ret <<= 8;

        ret += word[1];
        ret <<= 8;

        ret += word[2];
        ret <<= 8;

        ret += word[3];

        return ret;
    }

    static private int readEndianShort(FileInputStream in) throws IOException {
        int ret = 0;
        byte[] word = new byte[2];
        in.read ( word );

        ret += signedToUnsigned(word[1]);
        ret <<= 8;
        ret += signedToUnsigned(word[0]);

        return ret;
    }

    static private int signedToUnsigned(byte b) {
        if ( b >= 0 ) {
            return b;
        }
        return 0x100 + b;
    }

    static private long readEndianWord(FileInputStream in) throws IOException {
        long ret = 0;
        byte[] word = new byte[4];
        in.read ( word );

        ret += signedToUnsigned(word[3]);
        ret <<= 8;

        ret += signedToUnsigned(word[2]);
        ret <<= 8;

        ret += signedToUnsigned(word[1]);
        ret <<= 8;

        ret += signedToUnsigned(word[0]);

        return ret;
    }

    // ------------------

    public void writeToWav(File f) {
        try {
            RandomAccessFile wavFile = new RandomAccessFile(f, "rw");

            int payloadSize = buf.length;
            int fileSize = buf.length + 0x2c;
            int waveSize = fileSize - 8;

            byte[] header = {
                0x52, 0x49, 0x46, 0x46,  // RIFF
                (byte)waveSize,
                (byte)(waveSize >> 8),
                (byte)(waveSize >> 16),
                (byte)(waveSize >> 24),
                0x57, 0x41, 0x56, 0x45,  // WAVE
                // --- fmt chunk
                0x66, 0x6D, 0x74, 0x20,  // fmt
                16, 0, 0, 0,  // fmt size
                1, 0,  // pcm
                1, 0,  // channel count
                (byte)0xcc, 0x2c, 0, 0,  // freq (11468 hz)
                (byte)0xcc, 0x2c, 0, 0,  // avg. bytes/sec
                1, 0,  // block align
                8, 0,  // bits per sample
                // --- data chunk
                0x64, 0x61, 0x74, 0x61,  // data
                (byte)payloadSize,
                (byte)(payloadSize >> 8),
                (byte)(payloadSize >> 16),
                (byte)(payloadSize >> 24)
            };

            wavFile.write(header);

            byte[] unsigned = new byte[buf.length];
            for (int it = 0; it < buf.length; ++it) {
                unsigned[it] = (byte)((int)buf[it] + 0x88);
            }
            wavFile.write(unsigned);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

