package com.bear.libstorage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class InternalUtil {
    private static boolean checkPath(String path) {
        return path != null && !path.trim().equals("");
    }

    static boolean createFile(String filePath) {
        if (!checkPath(filePath)) {
            return false;
        }
        return createFile(new File(filePath));
    }

    static boolean createFile(File file) {
        if (file == null) {
            return false;
        }
        return createFile(file.getParent(), file.getName());
    }

    static boolean createFile(String dirPath, String fileName) {
        if (!checkPath(dirPath) || !checkPath(fileName)) {
            return false;
        }
        if (createDir(dirPath)) {
            File file = new File(dirPath, fileName);
            if (!file.exists()) {
                try {
                    return file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    static boolean createDir(String dirPath) {
        if (!checkPath(dirPath)) {
            return false;
        }
        File dirFile = new File(dirPath);
        return createDir(dirFile);
    }

    static boolean createDir(File dirFile) {
        if (dirFile == null) {
            return false;
        }
        return dirFile.exists() || dirFile.mkdirs();
    }

    static boolean isFileExist(String path) {
        return isFileExist(new File(path));
    }

    static boolean isFileExist(File file) {
        return file != null && file.exists();
    }

    static String toJson(Object jsonObj){
        return new GsonBuilder().serializeNulls().create().toJson(jsonObj);
    }

    static <T> T toObj(String json, TypeToken<T> token){
        if(token.getType() == String.class) {
            return (T) json;
        }
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(json, token.getType());
    }

    /**
     * 安全关闭流，内部捕获异常
     */
    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            close(closeable);
        }
    }
    /**安全关闭流，内部捕获异常**/

    static boolean delete(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
}
