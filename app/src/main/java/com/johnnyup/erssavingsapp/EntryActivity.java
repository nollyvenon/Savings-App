package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.SessionManager;

import org.snowcorp.login.R;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // create sqlite database
        DatabaseHandler db = new DatabaseHandler(this);

        // session manager
        SessionManager session = new SessionManager(this);

        // check user is already logged in
        if (session.isLoggedIn()) {
            Intent i = new Intent(EntryActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }

        findViewById(R.id.customer).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.agent).setOnClickListener(v -> {
            Intent intent = new Intent(this, MarketerLoginActivity.class);
            startActivity(intent);
        });
    }
}