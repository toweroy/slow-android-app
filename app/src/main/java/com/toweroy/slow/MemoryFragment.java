package com.toweroy.slow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class MemoryFragment extends Fragment {

    private static final String LOG_TAG = MemoryFragment.class.getSimpleName();
    private BroadcastReceiver receiver;

    public MemoryFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Receiving intent...");
                // Get memory usage
                long availableMb = intent.getLongExtra(MemoryService.MEM_INFO_AVAILABLE_MB, 0L);
                double availablePerc = intent.getDoubleExtra(MemoryService.MEM_INFO_AVAILABLE_PERC, 0);
                // Get disk usage
                long availableDiskMb = intent.getLongExtra(MemoryService.DISK_AVAILABLE, 0L);
                long totalDiskMb = intent.getLongExtra(MemoryService.DISK_TOTAL, 0L);
                updateMemParams(availableMb, availablePerc, totalDiskMb, availableDiskMb);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button retry = (Button) rootView.findViewById(R.id.retry_btn);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getActivity(), MemoryService.class);
                getActivity().startService(serviceIntent);
            }
        });

        return rootView;
    }

    private void updateMemParams(long availableMb, double availablePerc, long totalDiskMb, long availableDiskMb) {
        Log.d(LOG_TAG, "Updating text views");
        View view = this.getView();
        TextView availableMbTxt = (TextView) view.findViewById(R.id.cpu_1_param_txt);
        TextView availablePercTxt = (TextView) view.findViewById(R.id.cpu_2_param_txt);
        TextView availableDiskMbTxt = (TextView) view.findViewById(R.id.available_mb_txt);
        TextView totalDiskMbTxt = (TextView) view.findViewById(R.id.total_mb_txt);
        availableMbTxt.setText(String.valueOf(availableMb));
        availablePercTxt.setText(String.valueOf(availablePerc));
        availableDiskMbTxt.setText(String.valueOf(availableDiskMb));
        totalDiskMbTxt.setText(String.valueOf(totalDiskMb));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Registering receiver");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver), new IntentFilter(MemoryService.MEM_INFO_AVAILABLE));
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "Unregistering receiver");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }
}
