package com.example.cognisite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnBeginTest, btnLogin, btnAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBeginTest = findViewById(R.id.btn_begin_test);
        btnLogin = findViewById(R.id.btn_login);
        btnAboutUs = findViewById(R.id.btn_about_us);

        btnBeginTest.setOnClickListener(v -> {
            // TODO: Replace Toast with actual intent to your test activity
            Toast.makeText(MainActivity.this, "Begin Test clicked", Toast.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(v -> {
            // TODO: Replace Toast with actual intent to your login activity
            Toast.makeText(MainActivity.this, "Login clicked", Toast.LENGTH_SHORT).show();
        });

        btnAboutUs.setOnClickListener(v -> {
            // TODO: Replace Toast with actual intent to your About Us activity
            Toast.makeText(MainActivity.this, "About Us clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
