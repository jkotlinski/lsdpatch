package Document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class DocumentTest {

    @Test
    void savFile() {
        Document document = new Document();
        Assertions.assertNotNull(document.savFile());
    }

    @Test
    void setSavFile() throws IOException {
        Document document = new Document();
        LSDSavFile savFile = document.savFile();
        Assertions.assertNotNull(savFile);
        Assertions.assertFalse(document.isSavDirty());

        LSDSavFile newSavFile = new LSDSavFile();
        document.setSavFile(newSavFile);
        Assertions.assertFalse(document.isSavDirty());

        File tempFile = File.createTempFile("lsdpatcher", ".sav");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        for (int i = 0; i < 0x8000 * 4; ++i) {
            fos.write(1);
        }
        fos.close();
        newSavFile.loadFromSav(tempFile.getAbsolutePath());
        document.setSavFile(newSavFile);
        Assertions.assertTrue(newSavFile.equals(document.savFile()));
        Assertions.assertTrue(document.isSavDirty());

        try {
            document.loadSavFile("invalid_path");
            Assertions.fail("loadSavFile did not throw");
        } catch (FileNotFoundException ignored) {
        }

        document.setSavFile(null);
        Assertions.assertNull(document.savFile());
        Assertions.assertFalse(document.isSavDirty());
    }
}