package net.mls.modelserving.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by char on 2/26/18.
 */
public final class ZipFile {

    public static String unpack(File zipFile) throws IOException {
        String filePath = zipFile.getAbsolutePath();
        String outputFolder = filePath.substring(0, filePath.lastIndexOf('.'));

        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                File file = new File(outputFolder, entry.getName());

                if(entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if(!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[1024];
                        int location;
                        while((location = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, location);
                        }
                    }
                }
                entry = zis.getNextEntry();
            }
        }
        return outputFolder;
    }
}
