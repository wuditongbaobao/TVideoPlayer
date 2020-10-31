package com.tong.tvplay.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * File operation utils.
 * <br/>
 * Create/Delete/Copy/Read actions for folder/file or assets files.
 */
public final class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 递归删除文件、目录
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    public static byte[] getBytesFromAssetFile(Context ctx, String file) {
        AssetManager am = ctx.getAssets();
        return getBytesFromAssetFile(am, file);
    }

    public static byte[] getBytesFromAssetFile(AssetManager assetManager, String file) {
        byte[] buffer = null;
        InputStream in = null;
        try {
            in = assetManager.open(file);
            buffer = new byte[in.available()];
            in.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(in);
        }
        return buffer;
    }

    public static String getStringFromAssetFile(Context ctx, String file) {
        String result = "";
        try {
            byte[] buffer = getBytesFromAssetFile(ctx, file);
            if (null != buffer) {
                result = new String(buffer, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Copy large assets file to destination file. <br>
     * Copy 1024 bytes one time and copy all bytes to the destination at end. <br>
     * Use the length and last modified time to check
     * if the destination file has already been the newest version.
     * @param context  Context
     * @param srcFileName  source file name
     * @param destPath     destination file path
     * @param destFileName destination file name
     * @return boolean     success or not
     */
    public static boolean copyLargeAssetFile(Context context, String srcFileName,
                                             String destPath, String destFileName) {
        FileOutputStream out = null;
        InputStream input = null;
        try {
            AssetManager am = context.getResources().getAssets();
            input = am.open(srcFileName);

            File destFolder = new File(destPath);
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            File f = new File(destPath + File.separator + destFileName);
            // 文件长度相同且修改时间晚于apk最后的更新时间，认为文件已经是最新，不需要继续复制。
            if (f.exists() && input.available() == f.length()) {
                Log.d(TAG, "copyLargeAssetFile: already the newest version, fileName = " + destFileName);
                return true;
            }

            File parent = f.getParentFile();
            if (null != parent && !parent.isDirectory()) {
                parent.mkdirs();
            }

            if (f.exists()) {
                f.delete();
            }
            out = new FileOutputStream(f);

            byte[] b = new byte[10240];
            int readLen;
            while ((readLen = input.read(b, 0, 10240)) != -1 ) {
                out.write(b, 0, readLen);
                out.flush();
            }
            Log.d(TAG, "copyLargeAssetFile: copy \n" + srcFileName + "\n to \n" + f.getPath());
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            close(input);
            close(out);
        }

        return true;
    }

    public static String readTextFile(String strFilePath) {
        String path = strFilePath;

        // 打开文件
        File file = new File(path);
        return readTextFile(file, "utf-8");
    }

    public static String readTextFile(String strFilePath, String encode) {
        String path = strFilePath;
        // 打开文件
        File file = new File(path);
        return readTextFile(file, encode);
    }


    public static String readTextFile(File file, String encode) {
        StringBuffer sb = new StringBuffer();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, encode);
            br = new BufferedReader(isr);
            String line ;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return sb.toString();
    }

    public static File createFileIfNotExists(String filePath) {
        File file = new File(filePath);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static File createFileWhetherExists(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static byte[] inputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte[] imgdata = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    public static void makeFileDirectories(String updatePath) {
        if (TextUtils.isEmpty(updatePath)) {
            return;
        }

        File out = new File(updatePath);
        File dir = out.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

    }

    public static boolean fileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return new File(path).exists();
    }

    public static boolean writeToFile(File file, boolean append, String content) {
        return writeToFile(file, append, content, "UTF-8");
    }

    public static boolean writeToFile(File file, boolean append, String content, String charset) {
        BufferedWriter bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            bos = new BufferedWriter(new OutputStreamWriter(fos, charset));
            bos.write(content);
            bos.close();
            bos = null;
            fos.close();
            fos = null;
            return true;
        } catch (Exception e) {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                    bos = null;
                }
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                    bos = null;
                }
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean writeToFile(String path, boolean append, String content, String charset) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return writeToFile(file, append, content, charset);
    }

    public static void wirteToFile(File file, boolean append, byte[] bytes) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(bytes);
        } catch (Exception e) {
            if (fos != null) {
                fos.close();
                fos = null;
            }
            throw e;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception e) {
            // ignore
        }
    }

    public static boolean copy(String src, String dst) throws Exception {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            return false;
        }

        File dstFile = createFileIfNotExists(dst);
        return copy(srcFile, dstFile);
    }

    public static boolean copy(File src, File dst) throws Exception {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dst);
            in = inStream.getChannel();
            out = outStream.getChannel();

            // size太大，车机会卡住
            long position = 0;
            int maxCount = 8 * 1024 * 1024;
            long size = in.size();
            while (position < size) {
                long count = in.transferTo(position, maxCount, out);
                if (count > 0) {
                    position += count;
                }
            }
//            in.transferTo(0, in.size(), out);
            return true;
        } catch (Exception e) {
            throw e;
        } finally {
            close(inStream);
            close(in);
            close(outStream);
            close(out);
        }
    }

    public static String getFileExtName(File file) {
        String name = file.getName();
        if (name.length() > 0) {
            return name.substring(name.lastIndexOf(".") + 1);
        }
        return "";
    }

    public static long getFileSize(String path) {
        File file = new File(path);
        long length = 0;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] child = file.listFiles();
                for (File f : child) {
                    length += f.length();
                }
            } else {
                length = file.length();
            }
        }
        return length;
    }

    /**
     * 获取可用存储空间大小。
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableBytes(String rootPath) {
        try {
            StatFs stat = new StatFs(rootPath);
            return ((long) stat.getBlockSize()) * stat.getAvailableBlocks();
        } catch (Exception ex) {
            return -1;
        } catch (NoSuchMethodError error) {
            return -1;
        }
    }

    public static ArrayList<String> scanFiles(String path) {
        ArrayList<String> result = new ArrayList<>();
        File dir = new File(path);
        if (dir.exists() && dir.canRead()) {
            File[] subFiles = dir.listFiles();
            if (null != subFiles) {
                for (File file : subFiles) {
                    result.add(file.getAbsolutePath());
                }
            }
        }

        return result;
    }
}

