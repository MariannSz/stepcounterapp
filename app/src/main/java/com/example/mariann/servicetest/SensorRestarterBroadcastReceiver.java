package com.example.mariann.servicetest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Mariann on 11/17/2017.
 */

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        Log.i("EXIT", "broadcast");

        //SensorService broadcast = new SensorService();
        //context.stopService(new Intent (context, SensorService.class));
        context.startService(new Intent(context, SensorService.class));
    }
}
