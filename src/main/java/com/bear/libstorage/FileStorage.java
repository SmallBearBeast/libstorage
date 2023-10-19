package com.bear.libstorage;

import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileStorage {
    private static final int BUFFER_SIZE = 4096;

    public static boolean writeObjToJson(String path, Object object) {
        return writeStr(path, InternalUtil.toJson(object));
    }

    public static boolean writeStr(String path, String str) {
        byte[] buffer = str.getBytes(StandardCharsets.UTF_8);
        return writeStream(path, new ByteArrayInputStream(buffer));
    }

    public static boolean writeStream(String path, InputStream inputStream) {
        if (!InternalUtil.createFile(path)) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            InternalUtil.delete(path);
        } finally {
            InternalUtil.close(fos);
        }
        return false;
    }

    public static <T> T readObjFromJson(String path, TypeToken<T> token) {
        try{
            return InternalUtil.toObj(readStr(path), token);
        } catch (Exception e) {
            e.printStackTrace();
            InternalUtil.delete(path);
        }
        return null;
    }

    public static String readStr(String path) {
        // No need to close.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        readStream(path, new StreamCallback() {
            @Override
            public void success(byte[] data, int read) {
                baos.write(data, 0, read);
            }
        });
        return baos.toString();
    }

    public static void readStream(String path, StreamCallback streamCallback) {
        if (streamCallback == null) {
            return;
        }
        if (!InternalUtil.isFileExist(path)) {
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path));
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = fis.read(buffer, 0, buffer.length)) != -1) {
                streamCallback.success(buffer, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
            streamCallback.fail();
            InternalUtil.delete(path);
        } finally {
            InternalUtil.close(fis);
        }
    }
}
