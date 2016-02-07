package com.toweroy.slow;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MemoryService extends Service {

    private static final String LOG_TAG = MemoryService.class.getSimpleName();
    private static final double EXCLUDED_PERCENTAGE = 10.0;
    public static final String MEM_INFO_AVAILABLE = MemoryService.class.getSimpleName() + "." + "MEM_INFO_AVAILABLE";
    public static final String MEM_INFO_AVAILABLE_MB = MemoryService.class.getSimpleName() + "." + "MEM_INFO_AVAILABLE_MB";
    public static final String MEM_INFO_AVAILABLE_PERC = MemoryService.class.getSimpleName() + "." + "MEM_INFO_AVAILABLE_PERC";
    public static final String DISK_AVAILABLE = MemoryService.class.getSimpleName() + "." + "DISK_AVAILABLE";
    public static final String DISK_TOTAL = MemoryService.class.getSimpleName() + "." + "DISK_TOTAL";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    public void sendMemoryInfo() {
        Log.d(LOG_TAG, "Getting current memory/disk info");
        // Get memory usage
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMemMbs = mi.availMem / 1048576L;
        double memPercentAvail = ((double) mi.availMem / (double) mi.totalMem) * 100;
        // Get disk usage
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long availableDiskMbs = statFs.getAvailableBytes() / 1048576L;
        long totalDiskMbs = statFs.getTotalBytes() / 1048576L;

        Log.d(LOG_TAG, "Sending current memory/disk info");
        Intent intent = new Intent(MEM_INFO_AVAILABLE);
        intent.putExtra(MEM_INFO_AVAILABLE_MB, availableMemMbs);
        intent.putExtra(MEM_INFO_AVAILABLE_PERC, memPercentAvail);
        intent.putExtra(DISK_AVAILABLE, availableDiskMbs);
        intent.putExtra(DISK_TOTAL, totalDiskMbs);
        broadcaster.sendBroadcast(intent);

        if (totalDiskMbs > availableDiskMbs) {
            fillUp(totalDiskMbs, availableDiskMbs);
        }
    }

    private void fillUp(long totalDiskMbs, long availableDiskMbs) {
        float availableDiskPerc = ((float) availableDiskMbs / (float) totalDiskMbs) * 100;

        if (availableDiskPerc > EXCLUDED_PERCENTAGE) {
            Log.d(LOG_TAG, "Available perc: " + availableDiskPerc + ", Excluded percentage: " + EXCLUDED_PERCENTAGE);
            int sizeOfFile = MemoryUtils.calculateFileSize(totalDiskMbs, availableDiskMbs, EXCLUDED_PERCENTAGE);

            if (sizeOfFile > 0) {
                Log.d(LOG_TAG, "Size of file to create (MB) = " + sizeOfFile);
                MemoryUtils.createFile(getApplicationContext(), sizeOfFile);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sendMemoryInfo();
        return START_STICKY;
    }
}
