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
        if (sample.localPath() != null) {
            comment += "|local_path=" + sample.localPath();
        }
        comment += "|half_speed=" + (sample.halfSpeed() ? "1" : "0");
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
                MetaData metaData = new MetaData(zipEntry);
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
                if (metaData.getBoolean("original_samples")) {
                    sample = Sample.createFromOriginalSamples(dstPcm,
                            metaData.getName(),
                            metaData.getLocalPath(),
                            metaData.getVolume(),
                            true,
                            metaData.getBoolean("half_speed"));
                } else {
                    sample = new Sample(dstPcm, metaData.getName());
                }
                samples[sampleIndex] = sample;
            }
        }
    }

    private static class MetaData {
        HashMap<String, String> hashMap = new HashMap<>();
        public MetaData(ZipEntry zipEntry) {
            for (String command : new String(zipEntry.getExtra()).split("\\|")) {
                String key = command.split("=")[0];
                String value = command.split("=")[1];
                hashMap.put(key, value);
            }
        }

        String getName() {
            return hashMap.getOrDefault("name", null);
        }

        File getLocalPath() {
            return new File(hashMap.get("local_path"));
        }

        boolean getBoolean(String key) {
            return hashMap.getOrDefault(key, "0").equals("1");
        }

        int getVolume() {
            return Integer.parseInt(hashMap.getOrDefault("volume", "0"));
        }
    }
}
