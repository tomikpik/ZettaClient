package cz.eclub.zettaclient.zettaclient.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextDirectionHeuristic;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cz.eclub.zettaclient.zettaclient.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class LightProximityFragment extends Fragment {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    Runnable wifiScanThread;
    WifiManager wm;
    Long time = System.currentTimeMillis();
    Switch swWifiScan;
    TextView tvWifiScanResults;
    BroadcastReceiver broadcastReceiver;

    public LightProximityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_light_proximity, container, false);

        swWifiScan = (Switch) v.findViewById(R.id.switchWifiScan);
        tvWifiScanResults = (TextView) v.findViewById(R.id.tvWifiScanResults);



        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context c, Intent i) {
                WifiManager w = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
                Long j = System.currentTimeMillis();
                Log.d("WIFI", "Scan finished " + (j - time) + "ms");
                time = j;
                processWifiScanResults(w.getScanResults());
            }
        };


        swWifiScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableWifiScanReceiver(isChecked);


                if(isChecked){
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                           wm.startScan();
                        }
                    }, 0, 300, TimeUnit.MILLISECONDS);
                } else {
                    scheduler.shutdown();
                }
            }
        });

        return v;
    }

    public void enableWifiScanReceiver(boolean state){
        if(state){
            IntentFilter i = new IntentFilter();
            i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            this.getActivity().registerReceiver(broadcastReceiver, i);
        } else {
            this.getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        wm = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
    }


    public void processWifiScanResults(List<ScanResult> scanResults){

        String result = "";
        for(ScanResult sr:scanResults){
            result+=sr.SSID+" "+sr.level+"\n";
        }
        tvWifiScanResults.setText(result);
    }

}



