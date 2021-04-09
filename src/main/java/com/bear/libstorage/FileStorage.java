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

    public static boolean writeStream(String path, InputStream inputStream) {
        if (!InternalUtil.createFile(path)) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            InternalUtil.close(fos);
        }
        return false;
    }

    public static boolean writeObjToJson(String path, Object object) {
        if (!InternalUtil.createFile(path)) {
            return false;
        }
        return writeStr(path, InternalUtil.toJson(object));
    }

    public static boolean writeStr(String path, String str) {
        if (!InternalUtil.createFile(path)) {
            return false;
        }
        byte[] buffer = str.getBytes(StandardCharsets.UTF_8);
        return writeStream(path, new ByteArrayInputStream(buffer));
    }

    public static void readStream(String path, StreamCallback streamCallback) {
        if (streamCallback == null) {
            return;
        }
        if (!InternalUtil.createFile(path)) {
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
        } finally {
            InternalUtil.close(fis);
        }
    }

    public static <T> T readObjFromJson(String path, TypeToken<T> token) {
        if (!InternalUtil.createFile(path)) {
            return null;
        }
        return InternalUtil.toObj(readStr(path), token);
    }

    public static String readStr(String path) {
        if (!InternalUtil.createFile(path)) {
            return null;
        }
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
}
