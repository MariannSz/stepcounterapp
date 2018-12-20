package com.example.mariann.servicetest;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;


// Created by Mariann on 11/17/2017.


public class SensorService extends Service implements SensorEventListener, StepListener {

    private final IBinder binder = new LocalBinder();
    private ServiceCallbacks serviceCallbacks;


    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    Context serCtx;

    public Context getCtx() {
        return serCtx;
    }

    public SensorService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
        //user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //System.out.println("userID == " + user_id);
    }

    public SensorService() {
    }

    private StepDetector simpleStepDetector; // instance of stepdetector class
    private SensorManager sensorManager; // provides access to device sensors
    private Sensor accel; //creates an instance of a specific sensor
    public static final String TEXT_NUM_STEPS = "Number of Steps: "; //string to display step number
    public int numSteps = 0; // step number used for display and DB update
    public int dispSteps = 0; // steps to display
    public int dispTotal = 0; // total to display
    private int stepCounter = 0; // counting detected steps (based on the step detector class), incremented in the  step() function - make it start from the saved value or 0 if there was no save - 16/01/18

    private double firstLat = 0;
    private double lastLat = 0;
    private double firstLong = 0;
    private double lastLong = 0;
    private String user_id;
    private Firebase mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference DB;

    private boolean loggedIn = false;

    private LocationRequest mLocationRequest;

    private LocationSettingsRequest mLocationSettingsRequest;

    private LocationCallback mLocationCallback;

    private Location mCurrentLocation;

    private FusedLocationProviderClient mFusedLocationClient;


    private void buildLocationSettingsRequest() {
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
            }
        };
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Update the database with a boolean for false - have the account activity check this
            // and show a message asking the user to turn on the gps the next time it's open (SAME IN THE GAME) - 21/02/18
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
    }

    public void stepCount() { //method to start accelerometer
        //registers a sensor event listener for a given sensor at a given sampling frequency
        sensorManager.registerListener(SensorService.this, accel, SensorManager.SENSOR_DELAY_FASTEST); //SENSOR_DELAY_FASTEST (imported with sensor manager)
        // gets sensor data as fast as possible
    }

    public static boolean isInBackground(Context context) {
        boolean inBackground = true;
        ActivityManager Background = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = Background.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        inBackground = false;
                    }
                }
            }
        }
        return inBackground;
    }

    private boolean isLoggedIn(){
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            loggedIn = true;
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            System.out.println("There is a USER, imma set a booolean! USERID == " + user_id);
            DB.goOnline();
        } else {
            System.out.println("No user logged in, no setting the boolen to true, alright");
        }
        return loggedIn;
    }

    int save = 0;
    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    private double[] distances = new double[2];
    private int [] stepnumbers = new int[2];
    private int stepOutput = 0;
    private int timerCounter = 0;
    public int dailySteps = 0;
    public int dailyDisplay = 0;
    public long avg = 0;
    private  Firebase mDateRef;
    private Firebase mDailyRef;
    private Firebase mAvgRef;
    private String half = "3000";
    private String whole = "0000";
    private String noon = "131000";
    private String midnight = "000000";
    public String date = "yyyyMMdd";
    private boolean dailySave = false;

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                timerCounter++;

                SimpleDateFormat fmt = new SimpleDateFormat("mmss");
                SimpleDateFormat fmt0 = new SimpleDateFormat("mm");
                SimpleDateFormat fmt1 = new SimpleDateFormat("HHmmss");
                SimpleDateFormat fmt2 = new SimpleDateFormat("yyyyMMdd");

                if (!date.equals("yyyyMMdd") && !date.equals(fmt2.format(new Date()))){
                    dailySave = true;
                }

                String time = fmt.format(new Date()); // mmss for finding halves and wholes
                String minutes = fmt0.format(new Date());
                String nodeTime = fmt1.format(new Date()); // HHmmss for logging steps in every half-hour interval
                date = fmt2.format(new Date()); //yyyyMMdd for current day

                System.out.println("MINUTES == " + time);
                System.out.println("TIME == " + nodeTime);
                System.out.println("DATE == " + date);

                if (dispSteps == 0 || minutes.contains("6") || minutes.contains("9")) {
                    dispSteps = dailyDisplay;
                }

                if (dispTotal == 0 || minutes.contains("7")){
                    dispTotal = numSteps;
                }

                try {
                    serviceCallbacks.Display(SensorService.this);
                    Log.i("DETECTED_STEPS", String.valueOf(numSteps));
                } catch (NullPointerException e) {
                    Log.i("NOT_ACCOUNT", "no display here");
                }

                System.out.println("DAILYDISPLAY == " + dailyDisplay);

                isLoggedIn();

                if (loggedIn){

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mariann.servicetest.SensorService", getApplicationContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    System.out.println("if logged in code");
                    System.out.println("TIMERCOUNTErrrR == " + timerCounter);
                    if(timerCounter >= 5){
                        System.out.println("if timer counter is 5 code");
                        timerCounter = 0;
                        double totalDistance = 0;
                        int totalStepnr = 0;
                        double Distance;
                        double distToCheck;
                        double threshold = 0.0003;

                        if (mCurrentLocation != null){
                            System.out.println("current location is NOT empty code");
                            firstLat = mCurrentLocation.getLatitude();
                            firstLong = mCurrentLocation.getLongitude();

                            if (lastLat == 0 && lastLong == 0){
                                lastLat = firstLat;
                                lastLong = firstLong;
                            }

                            for (int i = 0; i<distances.length; i++){
                                totalDistance = totalDistance + distances[i];
                                totalStepnr = totalStepnr + stepnumbers[i];
                            }

                            Distance = Math.sqrt(Math.pow(lastLat - firstLat, 2) + Math.pow(lastLong - firstLong, 2));

                            if (stepCounter == 0){
                                Distance = 0.0000001;
                            }

                            distToCheck = Distance + totalDistance;
                            System.out.println("DISTENCE_TO_CHECK == " + distToCheck);
                            System.out.println("NUMSTEPS == " + numSteps);

                            if (distToCheck > threshold) {
                                stepOutput = stepCounter + totalStepnr;
                                System.out.println("STEP_OUTPUT == " + stepOutput);
                                dailySteps = dailySteps + stepOutput;
                                dailyDisplay = dailyDisplay + stepOutput;

                                editor.putInt("daily", dailyDisplay);
                                editor.putInt("halfhourly", dailySteps);
                                editor.apply();

                                mDailyRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id + "/Daily Step Numbers");
                                mDailyRef.child("Daily Step Number " + date).setValue(prefs.getInt("daily", dailyDisplay));

                                numSteps = numSteps + stepOutput;

                                distances[0] = 0;
                                distances[1] = 0;
                                stepnumbers[0] = 0;
                                stepnumbers[1] = 0;
                                System.out.println("STEP_OUTPUT == " + stepOutput);
                                System.out.println("STEP_COUNTER == " + stepCounter);
                                if (!isInBackground(SensorService.this)) {
                                    try {
                                        serviceCallbacks.Display(SensorService.this);
                                        Log.i("DETECTED_STEPS", String.valueOf(numSteps));
                                    } catch (NullPointerException e) {
                                        Log.i("NOT_ACCOUNT", "no display here");
                                    }
                                }
                            }else{
                                if (distances[0] == 0 && stepnumbers[0] == 0){
                                    distances[0] = Distance;
                                    stepnumbers[0] = stepCounter;
                                }else if ((distances[1] != 0 && stepnumbers [1] != 0) || Distance == 0.0000001){
                                    distances[0] = distances[1];
                                    stepnumbers[0] = stepnumbers[1];
                                    distances[1] = Distance;
                                    stepnumbers[1] = stepCounter;
                                }else{
                                    distances[1] = Distance;
                                    stepnumbers[1] = stepCounter;
                                }
                            }

                            stepCounter = 0;
                            firstLat = lastLat;
                            firstLong = lastLong;
                        }
                    }


                    mDateRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id + "/Steps During Every 30 min/" + date);

                    if (time.equals(half) || time.equals(whole) /*&& !nodeTime.equals(midnight)*/){
                        System.out.println("DAILY_STEPS == " + dailySteps);
                        System.out.println("MINUTES_RETURNED == " + time);
                        mDateRef.child(nodeTime).setValue(prefs.getInt("halfhourly", dailySteps));
                        dailySteps = 0;
                        editor.putInt("halfhourly", 0);
                    }else if (dailySave || nodeTime.equals(midnight)){
                        dailyDisplay = 0;
                        editor.putInt("daily", dailyDisplay);
                        System.out.println("NEW DAY");
                        dailySave = false;

                    }

                    mDailyRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id + "/Daily Step Numbers");
                    if (!date.equals("yyyyMMdd")){
                        mDailyRef.child("Daily Step Number " + date).setValue(prefs.getInt("daily", dailyDisplay));
                    }


                    if (nodeTime.equals(noon)){
                        mDailyRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id + "/Daily Step Numbers");
                        mDailyRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                HashMap<String, Long> dailyStepNrs = (HashMap<String, Long>) dataSnapshot.getValue();
                                int elementNr = dailyStepNrs.size();
                                int sum = 0;
                                for (Map.Entry<String, Long> entry : dailyStepNrs.entrySet()) {
                                    System.out.println(entry.getValue());
                                    sum += entry.getValue();
                                }
                                avg = sum / elementNr;
                                mAvgRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id);
                                mAvgRef.child("Avg").setValue(avg);
                                System.out.println("AVG == " + avg);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }

                    try {
                        editor.putInt("steps", numSteps);
                        editor.putInt("daily", dailyDisplay);
                        editor.putInt("halfhourly", dailySteps);
                        editor.putLong("avg", avg);
                        editor.apply();
                    } catch (NullPointerException e) {
                        Log.e(TAG, "error saving" + e.getMessage());
                    }
                    if (numSteps > save) { //check if the value currently saved is smaller then numSteps - if it is save in in the save variable
                        save = numSteps;
                        Firebase.setAndroidContext(SensorService.this);
                        try {
                            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid(); //get user id and save in string
                            mRef = new Firebase("https://servicetest-439a4.firebaseio.com/Users/" + user_id); //add userid to the url
                            mRef.child("steps").setValue(save);
                            //setSteps();
                        } catch (NullPointerException e) {
                            System.out.println("NO_USER_LOGGED_IN");
                        }


                        // IF THERE IS CONNECTION get their username by getting
                        // the value from under Users/currentuser_id/name
                        // save it in a string, add it to this db ref
                        // update the value of Steps/username with the stepnr (using the above reference) - 29/01/18 - DO THIS!! 21/02/18

                        Log.i("TIMER", "SAVES_RAN");
                    }

                }else{
                    System.out.println("NO USERS, NO STEP REGISTERING");
                }
            }
        };
    }

    /*private void setSteps() {
        mRef.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = (String) dataSnapshot.getValue();
                mStepsref = new Firebase("https://servicetest-439a4.firebaseio.com/Steps/");
                mStepsref.child(username).setValue(save);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i("STEPS_NODE", "no connection, no update");
            }
        });
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Firebase.setAndroidContext(this);
        DB = FirebaseDatabase.getInstance().getReference();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mariann.servicetest.SensorService", getApplicationContext().MODE_PRIVATE);
        dispSteps = dailyDisplay;
        numSteps = prefs.getInt("steps", 0);
        dailyDisplay = prefs.getInt("daily", 0);
        dailySteps = prefs.getInt("halfhourly", 0);
        avg = prefs.getLong("avg", 0);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        startTimer();//make a timer function that prints every x sec - implemented in the account in savetodb(); (later:saves every x minutes) and call it here
        stepCount();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Start building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        startLocationUpdates();

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        try {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mariann.servicetest.SensorService", getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("steps", numSteps);
            editor.putInt("daily", dailyDisplay);
            editor.putLong("avg", avg);
            editor.apply();
        } catch (NullPointerException e) {
            Log.e(TAG, "error saving" + e.getMessage());
        }

        /** should make a secondary storage to retrieve last working state from if it crashes on restart -25/11/17*/

        try {
            Intent broadcastIntent = new Intent("com.example.mariann.servicetest.RestartSensor");
            broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            Log.e(TAG, "error sending intent");
        }
        stopCount();

        stopService(new Intent(this, SensorService.class));
    }

    public void stopCount() {
        sensorManager.unregisterListener(SensorService.this); //unregisters a listener for the sensors with which it is registered
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel( //method in step detector class that calls the step listener if it detected a step (based its calculations)
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) { // only increase the numstep value here,
        // call the display on the timer (every 5 seconds, running the calculations and outputing a stepnr) - 21/02/18
            stepCounter++;

        //locChange = false;
    }


    public void setCallbacks (ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }
}
