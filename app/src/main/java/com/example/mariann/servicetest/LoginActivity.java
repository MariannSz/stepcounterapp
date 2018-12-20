package com.example.mariann.servicetest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Mariann on 11/30/2017.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailField; //interface emelemts for the user to fill in
    private EditText mPasswordField;

    private Button mLoginBtn;

    private Button testBtn;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); //sets layout to be the login layout

        mAuth = FirebaseAuth.getInstance(); //obtains instance of firebaseauth class and saves it in mAuth variable

        mEmailField = (EditText) findViewById(R.id.emailLogin);
        mPasswordField = (EditText) findViewById(R.id.passwordLogin);

        mLoginBtn = (Button) findViewById(R.id.loginBtn);

        mAuthListener = new FirebaseAuth.AuthStateListener() { //listener called when there is a change in the auth state
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){ //if the current user is logged in

                    startActivity(new Intent(LoginActivity.this, AccountActivity.class)); //redirevt to account activity

                }

            }
        };

        testBtn = (Button) findViewById(R.id.button2);

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent makeaccountIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(makeaccountIntent);
                Log.i("LOGIN", "redirect to register");
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() { // listener for when the given button is clicked by user
            @Override
            public void onClick(View v) {

                startSignIn(); //log in user

            }
        });

    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void startSignIn() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) { // if email and password fields are filled out

            Toast.makeText(LoginActivity.this, "Fields are empty", Toast.LENGTH_LONG).show(); // message for user about empty fields

        }else{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() { //otherwise just log in
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) { // if login fails nessage user
                        Toast.makeText(LoginActivity.this, "Sign In Problem", Toast.LENGTH_LONG).show();
                    }else{
                        mAuth.addAuthStateListener(mAuthListener); // Registers a listener to changes in the user authentication state
                    }
                }
            });
        }

    }
}
