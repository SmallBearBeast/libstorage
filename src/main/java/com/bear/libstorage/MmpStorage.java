package com.bear.libstorage;

import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class MmpStorage {
    private static final int BUFFER_SIZE = 4096;

    public static boolean writeStream(String path, InputStream inputStream) {
        if (!InternalUtil.createFile(path)) {
            return false;
        }
        RandomAccessFile raf = null;
        FileChannel fc = null;
        MappedByteBuffer mbb = null;
        File file = new File(path);
        try {
            raf = new RandomAccessFile(file, "rw");
            fc = raf.getChannel();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            int hasRead = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                mbb = fc.map(FileChannel.MapMode.READ_WRITE, hasRead, hasRead + read);
                mbb.put(buffer, 0, read);
                hasRead = hasRead + read;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            InternalUtil.close(fc, raf);
            unmap(mbb);
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
        RandomAccessFile raf = null;
        FileChannel fc = null;
        MappedByteBuffer mbb = null;
        try {
            raf = new RandomAccessFile(path, "rw");
            fc = raf.getChannel();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            int hasRead = 0;
            int size = (int) fc.size();
            while (hasRead < size) {
                read = hasRead + buffer.length < size ? buffer.length : size - hasRead;
                mbb = fc.map(FileChannel.MapMode.READ_WRITE, hasRead, hasRead + read);
                mbb.get(buffer, 0, read);
                streamCallback.success(buffer, read);
                hasRead = hasRead + read;
            }
        } catch (Exception e) {
            e.printStackTrace();
            streamCallback.fail();
        } finally {
            unmap(mbb);
            InternalUtil.close(fc, raf);
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

    private static void unmap(MappedByteBuffer mbb) {
        if (mbb == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("sun.nio.ch.FileChannelImpl");
            Method m = clazz.getDeclaredMethod("unmap", MappedByteBuffer.class);
            m.setAccessible(true);
            m.invoke(null, mbb);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
