package kitEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class KitArchive {
    static public void save(Sample[] samples, File file) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < samples.length; ++i) {
                Sample sample = samples[i];
                if (sample == null) {
                    continue;
                }
                ZipEntry zipEntry = new ZipEntry(Integer.toString(i));
                writeMetaData(sample, zipEntry);
                short[] workSampleData = sample.workSampleData();
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buf = new byte[workSampleData.length * 2];
                int dst = 0;
                for (short s : workSampleData) {
                    buf[dst++] = (byte) s;
                    buf[dst++] = (byte) (s >> 8);
                }
                zipOutputStream.write(buf);
                zipOutputStream.closeEntry();
            }
        }
    }

    private static void writeMetaData(Sample sample, ZipEntry zipEntry) {
        String comment;
        comment = "name=" + sample.getName();
        if (sample.canAdjustVolume()) {
            comment += "|volume=" + sample.volumeDb() + "|original_samples=1";
        }
        zipEntry.setExtra(comment.getBytes());
    }

    static public void load(File file, Sample[] samples) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            while (true) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null) {
                    return;
                }
                final int sampleIndex = Integer.parseInt(zipEntry.getName());
                HashMap<String, Object> metaData = parseComment(new String(zipEntry.getExtra()));
                ArrayList<Byte> inPcm = new ArrayList<>();
                while (true) {
                    int b = zipInputStream.read();
                    if (b == -1) {
                        break;
                    }
                    inPcm.add((byte)b);
                }
                short[] dstPcm = new short[inPcm.size() / 2];
                for (int i = 0; i < dstPcm.length; ++i) {
                    dstPcm[i] = (short) (inPcm.get(i * 2) + (inPcm.get(i * 2 + 1) << 8));
                }
                Sample sample;
                if (metaData.containsKey("original_samples")) {
                    sample = Sample.createFromOriginalSamples(dstPcm,
                            (String) metaData.get("name"),
                            (int) metaData.get("volume"));
                } else {
                    sample = new Sample(dstPcm, (String) metaData.get("name"));
                }
                samples[sampleIndex] = sample;
            }
        }
    }

    private static HashMap<String, Object> parseComment(String comment) {
        HashMap<String, Object> hashMap = new HashMap<>();
        for (String command : comment.split("\\|")) {
            String key = command.split("=")[0];
            String value = command.split("=")[1];
            switch (key) {
                case "name":
                    hashMap.put("name", value);
                    break;
                case "volume":
                    hashMap.put("volume", Integer.parseInt(value));
                    break;
                case "original_samples":
                    hashMap.put("original_samples", 1);
                    break;
            }
        }
        return hashMap;
    }
}
