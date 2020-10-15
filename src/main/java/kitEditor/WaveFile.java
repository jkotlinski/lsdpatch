package kitEditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WaveFile {
    public static void write(short[] pcm, File f) throws IOException {
        RandomAccessFile wavFile = new RandomAccessFile(f, "rw");

        int payloadSize = pcm.length * 2;
        int fileSize = pcm.length * 2 + 0x2c;
        int waveSize = fileSize - 8;

        byte[] header = {
                0x52, 0x49, 0x46, 0x46,  // RIFF
                (byte) waveSize,
                (byte) (waveSize >> 8),
                (byte) (waveSize >> 16),
                (byte) (waveSize >> 24),
                0x57, 0x41, 0x56, 0x45,  // WAVE
                // --- fmt chunk
                0x66, 0x6D, 0x74, 0x20,  // fmt
                16, 0, 0, 0,  // fmt size
                1, 0,  // pcm
                1, 0,  // channel count
                (byte) 0xcc, 0x2c, 0, 0,  // freq (11468 hz)
                (byte) 0xcc, 0x2c, 0, 0,  // avg. bytes/sec
                1, 0,  // block align
                16, 0,  // bits per sample
                // --- data chunk
                0x64, 0x61, 0x74, 0x61,  // data
                (byte) payloadSize,
                (byte) (payloadSize >> 8),
                (byte) (payloadSize >> 16),
                (byte) (payloadSize >> 24)
        };

        wavFile.write(header);

        byte[] byteBuffer = new byte[pcm.length * 2];
        int dst = 0;
        for (short sample : pcm) {
            byteBuffer[dst++] = (byte) sample;
            byteBuffer[dst++] = (byte) (sample >> 8);
        }
        wavFile.write(byteBuffer);
        wavFile.close();
    }
}
