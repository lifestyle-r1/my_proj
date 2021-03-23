package com.rohith.lifestyle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Handler gHandler;
    private static final int TIME_DELAY = 4000;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gHandler = new Handler();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser == null)
        {
            gHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toLogin();
                }
            },TIME_DELAY);
        }else{
            gHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toDashboard();
                }
            },TIME_DELAY);
        }


    }

    private void toDashboard() {
        Intent toDashboard = new Intent(MainActivity.this,dashboardPage.class);
        startActivity(toDashboard);
        finish();
    }

    private void toLogin() {
        Intent toLogin = new Intent(MainActivity.this,loginPage.class);
        startActivity(toLogin);
        finish();
    }
}