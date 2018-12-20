package com.example.mariann.servicetest;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LeaderboardActivity extends AppCompatActivity {

    private Firebase leaderboardRef;
    private Firebase usernameRef;
    private String ceUnlocked;
    private Firebase mStepsref;
    private String listItems [] = {"kamu1", "kamu2"};
    private String item;
    private Button mRefresh;
    private int arrayCounter = 0;
    ArrayAdapter <String> view;
    private String user_id;
    private Firebase nameRef;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        //fillBoard();
    }


    @Override
    protected void onStart() {
        super.onStart();

        //fillBoard(); //half the time it displays the array before the data is fetched - 01/02/18

        //user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        name = preferences.getString("name", null);


        leaderboardRef = new Firebase("https://servicetest-439a4.firebaseio.com/Steps/" + name);
        leaderboardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ceUnlocked = String.valueOf(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //TextView CE = (TextView) findViewById(R.id.textView6);
        //CE.setText(ceUnlocked);

        mStepsref = new Firebase("https://servicetest-439a4.firebaseio.com/Steps/");

        mRefresh = (Button) findViewById(R.id.button5);

        mRefresh.setOnClickListener(new View.OnClickListener() {
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
                            item = i+1 +". " + list.get(i).getKey() + ": " + list.get(i).getValue() + " Celestial Enhancements Unlocked";
                            LeaderArray.leaders[i]=item;
                        }
                        fillBoard();

                        Toast.makeText(LeaderboardActivity.this, "Refreshing... ", Toast.LENGTH_LONG).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LeaderboardActivity.this, "Display is updated", Toast.LENGTH_LONG).show();
                            }
                        }, 200);
                    }


                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.i("LEADERB", "not working");
                    }


                });

                System.out.println("I'M_SUCH_A_BUTTON");
            }
        });

    }

    /*private void showLeaderboard() {

        mStepsref = new Firebase("https://servicetest-439a4.firebaseio.com/Steps/"); //get eeryone's name and steps; figure out how to sort it, display it - 30/01/18
        mStepsref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap <String, Long> allSteps = (HashMap<String, Long>) dataSnapshot.getValue();
                Set<Entry<String, Long>> set = allSteps.entrySet();
                List<Entry<String, Long>> list = new ArrayList<Entry<String, Long>>(set);
                Collections.sort( list, new Comparator<Entry<String, Long>>(){
                    @Override
                    public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                        return (o2.getValue()).compareTo( o1.getValue() );
                    }
                });

                for(int i = 0; i<listItems.length; i++){
                    item = list.get(i).getKey() + ":" + list.get(i).getValue();
                    listItems[i]=item;
                }
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i("LEADERB", "not working");
            }

        });

    }*/

    private void fillBoard() {
        view = new ArrayAdapter<String>(LeaderboardActivity.this, android.R.layout.simple_list_item_1, LeaderArray.leaders);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(view);
        TextView CE = (TextView) findViewById(R.id.textView6);
        CE.setText(ceUnlocked);
        System.out.println("board filled");
    }
}
