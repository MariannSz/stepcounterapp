package com.example.mariann.servicetest;

import android.accounts.Account;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.*;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.ServiceConnection;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mariann on 11/30/2017.
 */

public class AccountActivity extends AppCompatActivity implements ServiceCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int WOODisplay = 30;
    private TextView mStepNumber;
    private ServiceCallbacks serviceCallbacks;
    private boolean bound = false;
    private SensorService mSensorService;
    private boolean isOnAccount;

    private Button mSend; //leaderboard button
    private Button mSynch; // synch with db button

    private Firebase mSynchRef;
    private Firebase mStepsref;
    //private Firebase mRefLat;
    //private Firebase mRefLong;

    private String item;

    //private int counter = 0;

    ////int save = 0;
    ////String currentUser;
    ////String currentNumber = "-> step number:";
    ////int number = 1;

    //private FusedLocationProviderClient mFusedLocationClient;

    //private LocationRequest mLocationRequest;

    //private LocationSettingsRequest mLocationSettingsRequest;

    //private static final int REQUEST_CHECK_SETTINGS = 0x1; ??

    //private LocationCallback mLocationCallback;

    ////private boolean mRequestingLocationUpdates;

    private SettingsClient mSettingsClient;

    //private Location mCurrentLocation;

    private double prevLat;
    private double prevLong;
    private double currLat;
    private double currLong;

    //// private String mLastUpdateTime;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private String user_id;
    private int count = 0;
    //private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account); // set layout
        isOnAccount = true;
        Firebase.setAndroidContext(AccountActivity.this);
        //Firebase.goOnline();

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ////String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName(); //find a way to access their name they used to register
        //// or a way to use their emails as id when registering -21/01/18
        ////mRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + username);

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //mSettingsClient = LocationServices.getSettingsClient(this);


        mStepsref = new Firebase("https://servicetest-439a4.firebaseio.com/Steps/");

        mSend = (Button) findViewById(R.id.button9);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mStepsref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Long> allSteps = (HashMap<String, Long>) dataSnapshot.getValue(); //WHY ID THIS LONG???? 02-03-2018
                        Set<Map.Entry<String, Long>> set = allSteps.entrySet();
                        List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(set);
                        Collections.sort( list, new Comparator<Map.Entry<String, Long>>(){
                            @Override
                            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                                return (o2.getValue()).compareTo( o1.getValue() );
                            }
                        });

                        for(int i = 0; i<LeaderArray.leaders.length; i++){
                            item = list.get(i).getKey() + ":" + list.get(i).getValue();
                            LeaderArray.leaders[i]=item;
                            ////arrayCounter++;
                        }
                    }


                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.i("LEADERB", "not working");
                    }

                });


                Intent leaderIntent = new Intent(AccountActivity.this, LeaderboardActivity.class);
                startActivity(leaderIntent); //put it back after "calibrating" 20/04/18
                Log.i("AFTER_ST","button");

            }
        });

        //createLocationCallback();
        //createLocationRequest();
        //buildLocationSettingsRequest();
        //startLocationUpdates();


        //locationPrint = (Button) findViewById(R.id.button3);

        //locationPrint.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {

                //printCoordinates();
                //startLocationUpdates();
            //}
        //});


    }

    /*private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(10000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(5000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                System.out.println("LOCATION_CALLBACK: Lat == " + mCurrentLocation.getLatitude());
                System.out.println("LOCATION_CALLBACK: Long == " + mCurrentLocation.getLongitude());

                if (mCurrentLocation != null && counter < 300){
                    mRefLat = new Firebase("https://servicetest-439a4.firebaseio.com/LoggedLatitudes/");
                    mRefLong = new Firebase("https://servicetest-439a4.firebaseio.com/LoggedLongitudes/");
                    mRefLat.child("Lat" + counter).setValue(mCurrentLocation.getLatitude());
                    mRefLong.child("Long" + counter).setValue(mCurrentLocation.getLongitude());
                    counter++;
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        //mRequestingLocationUpdates = false;
                        //updateUI();
                        break;
                }
                break;
        }
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        
                        printCoordinates();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(AccountActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(AccountActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                //mRequestingLocationUpdates = false;
                        }

                        printCoordinates();
                    }
                });
    }

    private void printCoordinates() {
        if (mCurrentLocation != null){
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();

            System.out.println("LOCATION_UPDATES: Lat == " + latitude);
            System.out.println("LOCATION_UPDATES: Long == " + longitude);

        }else{
            System.out.println("PRINT_COORD: Location == NULL");
        }
    }*/

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    @Override
    public void Display(final SensorService current) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_account);
                if (isOnAccount){
                    if (current.dispSteps < 10){ //dailyDisplay
                        TextView TvSteps = (TextView) findViewById(R.id.tv_steps);
                        TvSteps.setText(" " + current.dispSteps + " "); //dailyDisplay
                    }else if (current.dispSteps < 100){ // dailyDisplay
                        TextView TvSteps = (TextView) findViewById(R.id.tv_steps);
                        TvSteps.setText("" + current.dispSteps + " "); //dailyDisplay
                    }else{
                        TextView TvSteps = (TextView) findViewById(R.id.tv_steps);
                        TvSteps.setText("" + current.dispSteps); //dailyDisplay
                    }

                    TextView daily = (TextView) findViewById(R.id.textView);
                    daily.setText("Total Celestial Points: " + current.dispTotal);
                    TextView avg = (TextView) findViewById(R.id.textView2);
                    avg.setText("Daily Average: " + current.avg); //

                    mSend = (Button) findViewById(R.id.button9); // had to include it here as a lame temp solution,
                    // bc otherwise it doesn't work AFTER a step was detected - 19/01/18
                    mSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mStepsref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, Long> allSteps = (HashMap<String, Long>) dataSnapshot.getValue();
                                    Set<Map.Entry<String, Long>> set = allSteps.entrySet();
                                    List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(set);
                                    Collections.sort( list, new Comparator<Map.Entry<String, Long>>(){
                                        @Override
                                        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                                            return (o2.getValue()).compareTo( o1.getValue() );
                                        }
                                    });

                                    for(int i = 0; i<LeaderArray.leaders.length; i++){
                                        item = list.get(i).getKey() + ": " + list.get(i).getValue() + " Celestial Enhancements Unlocked";
                                        LeaderArray.leaders[i]=item;
                                    }
                                }


                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.i("LEADERB", "not working");
                                }

                            });



                            Intent leaderIntent = new Intent(AccountActivity.this, LeaderboardActivity.class);
                            startActivity(leaderIntent); //put it back after "calibrating" 20/04/18
                            Log.i("AFTER_ST","button");
                            //System.out.println(LeaderArray.leaders[0]);
                            //System.out.println(LeaderArray.leaders[1]);
                            // updateLocationPrint();



                        }
                    });

                    mSynch = (Button) findViewById(R.id.button3);
                    mSynch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSynchRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id);
                            mSynchRef.child("steps").setValue(current.numSteps);
                            /*if (WOODisplay == 30){
                                WOODisplay = 0; //when they hit the sync button, reset the middle display
                            }else{
                                WOODisplay = 30; //when they hit the sync button, reset the middle display
                            }*/

                            Toast.makeText(AccountActivity.this, "Synchronizing... ", Toast.LENGTH_LONG).show();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AccountActivity.this, "Synchronization is complete", Toast.LENGTH_LONG).show();
                                }
                            }, 200);



                        }

                    });
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }



    @Override
    protected void onPause(){
        super.onPause();
        //Firebase.goOffline();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //Firebase.goOnline();
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
    };

}
