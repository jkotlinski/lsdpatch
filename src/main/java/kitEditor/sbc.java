package kitEditor;

// Sample bank creator.

class sbc {

    public static void compile(byte[] dst, Sample[] samples, int[] byteLength) {
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

                /* Theoretically, 7.5 would be in the middle, but
                 * DMG noise spikes seem to go towards 6.
                 * 7 is a compromise between maximum dynamic range
                 * and lowered noise spikes on DMG.
                 * Kept as an integer due to the way software mixing
                 * is performed. (7+7=7)
                 */
                final int DC_CENTER = 7;

                // Game Boy audio signal is inverted.
                s = (int)(Math.round(DC_CENTER - (double)s / (256 * 16)));
                s = Math.min(0xf, Math.max(0, s));

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
