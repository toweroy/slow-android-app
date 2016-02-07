package com.toweroy.slow;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class MemoryUtils {

    private static final String LOG_TAG = MemoryUtils.class.getSimpleName();

    public static boolean createFile(Context context, int sizeMb) {
        try {
            File filename = new File(getFilePath(context), "file-dummy");
            Log.d(LOG_TAG, "Creating random file [" + filename.getAbsolutePath() + "] of size (MB) [" + sizeMb + "]");
            RandomAccessFile f = new RandomAccessFile(filename, "rw");
            int fileLength = sizeMb * 1024 * 1024;
            f.setLength(fileLength);
            int offset = fileLength - 2;
            f.seek(offset);
            f.writeByte(0);
            f.close();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return false;
    }

    public static int calculateFileSize(long totalDiskMb, long diskUsedMb, double perc) {
        Log.d(LOG_TAG, "Calculating file size...");
        double leaveAloneMb = totalDiskMb * (perc / 100);
        double fileSizeMb = totalDiskMb - diskUsedMb - leaveAloneMb;
        return (int) fileSizeMb;
    }

    public static String getFilePath(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String appDir = context.getPackageName();

        try {
            PackageInfo p = packageManager.getPackageInfo(appDir, 0);
            appDir = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(LOG_TAG, "Error Package name not found ", e);
        }

        return appDir;
    }
}
