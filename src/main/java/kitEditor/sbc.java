package kitEditor;

// Sample bank creator.

class sbc {

    public static void compile(byte[] dst, Sample[] samples, int[] byteLength, boolean gameBoyAdvancePolarity) {
        int offset = 0x60; //don't overwrite sample bank info!
        for (int sampleIt = 0; sampleIt < samples.length; sampleIt++) {
            Sample sample = samples[sampleIt];
            if (sample == null) {
                break;
            }

            sample.seekStart();
            int sampleLength = sample.lengthInSamples();

            int addedBytes = 0;
            int[] outputBuffer = new int[32];
            int outputCounter = 0;
            for (int i = 0; i < sampleLength; i++) {
                int s = sample.read();
                s = (int)(Math.round((double)s / (256 * 16) + 7.5));
                s = Math.min(0xf, Math.max(0, s));
                if (!gameBoyAdvancePolarity) {
                    // Use DMG polarity, where 0xf = -1.0 and 0 = 1.0.
                    s = 0xf - s;
                }

                // Starting from LSDj 9.2.0, first sample is skipped to compensate for wave refresh bug.
                // This rotates the wave frame rightwards.
                outputBuffer[(outputCounter + 1) % 32] = s;

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
        while (offset < 0x4000) {
            dst[offset++] = -1; // rst opcode
        }
    }
}
