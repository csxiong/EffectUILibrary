package me.csxiong.uiux.ui.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public final class AssetsUtil {

    public static String AssetsRoot = "file://android_asset/";

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private AssetsUtil() {
        throw new IllegalAccessError();
    }

    /**
     * 复制assets指定文件夹到目标文件夹.
     * @param assetManager AssetManager.
     * @param srcPath assets指定文件夹路径.
     * @param destDir
     * @throws IOException
     */
    public static void copyDirectory(@NonNull final AssetManager assetManager,
                                     @NonNull final String srcPath,
                                     @NonNull final File destDir)  throws IOException {
        if (srcPath == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        doCopyDirectory(assetManager, srcPath, destDir, buffer);
    }

    private static void doCopyDirectory(@NonNull final AssetManager assetManager,
                                        @NonNull final String srcPath,
                                        @NonNull final File destDir,
                                        @Nullable final byte[] buffer) throws IOException {
        final String[] files = assetManager.list(srcPath);
        if (files != null && files.length > 0) {
            if (!destDir.exists() && !destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            String childPath;
            File destFile;
            for (final String file : files) {
                childPath = srcPath + "/" + file;
                destFile = new File(destDir, file);
                if (!copyAsset(assetManager, childPath, destFile, buffer)) {
                    doCopyDirectory(assetManager, childPath, destFile, buffer);
                }
            }
        }
    }

    private static boolean copyAsset(@NonNull AssetManager assetManager, @NonNull String srcFile,
                                     @NonNull File destFile, final byte[] buffer)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = assetManager.open(srcFile);
        } catch(Exception ignored) {
        }
        if (input == null) {
            if (!destFile.mkdirs() && !destFile.isDirectory()) {
                throw new IOException("Destination '" + destFile + "' directory cannot be created");
            }
        } else {
            final File parentFile = destFile.getParentFile();
            if (parentFile != null) {
                if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                    throw new IOException("Destination '" + parentFile + "' directory cannot be created");
                }
            }
            if (destFile.exists() && !destFile.canWrite()) {
                throw new IOException("Destination '" + destFile + "' exists but is read-only");
            }
            copy(input, destFile, buffer);
        }
        return input != null;
    }

    private static long copy(final InputStream input, final File destFile,
                             final byte[] buffer) throws IOException {
        long count = 0;
        int n;
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(destFile);
            while (EOF != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
        return count;
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final Throwable ioe) {
            // ignore
        }
    }

    /**
     * 读取文件
     *
     * @param context
     * @param name
     * @return
     */
    public static AssetFileDescriptor findFile(Context context, String name) {
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getAssets().openFd(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return assetFileDescriptor;
    }

}
