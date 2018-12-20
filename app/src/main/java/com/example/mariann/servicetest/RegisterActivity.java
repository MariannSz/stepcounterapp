package com.example.mariann.servicetest;

//import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mariann on 11/30/2017.
 */

public class RegisterActivity extends AppCompatActivity {
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mContinue;
    private Button mRedLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Firebase mRef;
    private Firebase mStepsref;

   // private ProgressDialog mProgress; // message for user, doesn't work

    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Firebase.setAndroidContext(this); //set context for firebase

        mAuth = FirebaseAuth.getInstance(); // entry point of the Firebase Authentication SDK, obtains an instance of the class

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users"); //gets given db reference


        //db reference saved in variable

        //mProgress = new ProgressDialog(this); //doesn't work

        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailLogin);
        mPasswordField = (EditText) findViewById(R.id.passwordLogin);
        mContinue = (Button) findViewById(R.id.contBtn);
        mRedLogin = (Button) findViewById(R.id.RedLogin);

        mAuthListener = new FirebaseAuth.AuthStateListener() { //listener (called when there is a change in the auth state)
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){ //if the current user has an account

                    Log.i("REGISTER", "no user, register one");
                    //startActivity(new Intent(RegisterActivity.this, LoginActivity.class)); //redirect to login

                }

            }
        };

        mContinue.setOnClickListener(new View.OnClickListener(){ // listener for when the given button is clicked by user
            @Override
            public void onClick(View view){
                startRegister(); // register user
            }
        });

        mRedLogin.setOnClickListener(new View.OnClickListener(){ // listener for when the given button is clicked by user
            @Override
            public void onClick(View view){
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //users won't be able to go back
                startActivity(loginIntent); // redirect to register
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); // call auth state listener on start
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private String[] specChar = {"§","!","@","#", "£", "¤", "$", "&", "/", "{", "(", "[", ")", "]", "=", "}", "?", "+", "_", "-", "."};

    private void startRegister() {
        boolean allowRegister = true;
        final String username = mNameField.getText().toString().trim(); // trim removes leading and tailing spaces

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", username);
        editor.apply();

        final String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();
        if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){ //if username, email and password fields are filled out

            for (int i = 0; i<specChar.length; i++){
                if (username.contains(specChar[i])) {
                    Toast.makeText(RegisterActivity.this, "Fields contain forbidden characters, please don't use special characters!", Toast.LENGTH_LONG).show();
                    allowRegister = false;
                    i=specChar.length;
                }
            }

            Toast.makeText(RegisterActivity.this, "Registering...", Toast.LENGTH_LONG).show();

            if (allowRegister){
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() { //create new user with parameters, call onComplete listener on completing AuthResult
                    // (result obtained from operations that affect the auth state
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) { // save users AS CHILD with their emails as ID
                        // and their name (for a value) plus
                        // a child for the dbSteps that
                        // gets updated from the account - 20/01/18

                        if(task.isSuccessful()){ //if authresult task is complete



                            String user_id = mAuth.getCurrentUser().getUid(); //save userid in string variable

                            mRef = new Firebase("https://servicetest-439a4.firebaseio.com//Users/" + user_id);
                            mStepsref = new Firebase("https://servicetest-439a4.firebaseio.com/Steps");

                            DatabaseReference current_user_db = mDatabase.child(user_id);
                            current_user_db.child("name").setValue(user_id);//figure out the difference bw using
                            // DatabaseReference and Firebase for paths - 29/01/18

                            mRef.child("steps").setValue(0);
                            mRef.child("name").setValue(username);
                            mRef.child("email").setValue(email); //make a user class instead - 24/01/18 (check doc.: Read and Write Data on Android)

                            mStepsref.child(username).setValue(0);


                            // add some sort of progress/loading screen - 29/01/18



                        }

                    }
                });

            }

        }else{
            Toast.makeText(RegisterActivity.this, "Fields are empty", Toast.LENGTH_LONG).show();
        }

    }

}
