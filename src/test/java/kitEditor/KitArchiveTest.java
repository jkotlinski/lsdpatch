package kitEditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class KitArchiveTest {

    @Test
    void save() throws IOException {
        File tmpFile = File.createTempFile("lsdpatcher", ".kit");
        tmpFile.deleteOnExit();
        Sample[] originalSamples = new Sample[3];
        originalSamples[0] = new Sample(new short[10], "FOO");
        originalSamples[2] = Sample.createFromOriginalSamples(new short[10], "BAR", 1);
        KitArchive.save(originalSamples, tmpFile);

        Sample[] s = new Sample[3];
        KitArchive.load(tmpFile, s);

        Assertions.assertNull(s[1]);
        Assertions.assertEquals("FOO", s[0].getName());
        Assertions.assertEquals(10, s[0].lengthInSamples());
        Assertions.assertEquals("BAR", s[2].getName());
        Assertions.assertEquals(10, s[2].lengthInSamples());
        Assertions.assertEquals(1, s[2].volumeDb());
    }
}