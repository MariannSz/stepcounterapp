package com.example.mariann.servicetest;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    Intent mServiceIntent;
    private SensorService mSensorService;
    // private boolean bound = false;
    Context ctx;

    //static final String TEXT_NUM_STEPS = "Number of Steps: "; //string to display step number
    //int numSteps; // step number


    public Context getCtx() {
        return ctx;
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_main);

        mSensorService = new SensorService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }

        Firebase.setAndroidContext(this); //set context for firebase


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if (firebaseAuth.getCurrentUser() == null){ // if there is no user logged in
                    Log.i("MAINACT","no user logged in");
                    Intent loginIntent = new Intent((getCtx()), LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //users won't be able to go back
                    startActivity(loginIntent); // redirect to register
                }else{
                    Intent accIntent = new Intent ((getCtx()), AccountActivity.class);
                    accIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(accIntent);
                }
                /*}else{
                    Log.i("MAINACT","user already logged in");
                    Intent loginIntent = new Intent((getCtx()), LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //users won't be able to go back
                    startActivity(loginIntent); // redirect to register
                }*/
            }
        };

        // make it always go to the register,
        // and make a button on the register activity to redirect to login,
        // if the person already has an account? - for testing purposes (include a value on registering needs imp and test)

        // make it go to the account after they registered once - final version - 05/12/17

    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (bound) {
            mSensorService.setCallbacks(null);
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            mSensorService = binder.getService();
            bound = true;
            mSensorService.setCallbacks(AccountActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
           bound = false;
        }
    };*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
    }


}
