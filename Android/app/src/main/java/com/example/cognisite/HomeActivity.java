package com.example.cognisite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    Button btnLogout, btnStartTest, btnAboutUs, btnViewResult;
    ImageView profileIcon;
    int childAgeInMonths = -1;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnLogout = findViewById(R.id.btn_logout);
        btnStartTest = findViewById(R.id.btn_start_test);
        btnAboutUs = findViewById(R.id.btn_about_us);
        btnViewResult = findViewById(R.id.btn_view_result);
        profileIcon = findViewById(R.id.profileIcon);

        btnStartTest.setEnabled(false); // Disable until child age is loaded

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser().getUid();

        // Fetch child data
        db.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long age = documentSnapshot.getLong("childAgeInMonths");
                        if (age != null) {
                            childAgeInMonths = age.intValue();
                            btnStartTest.setEnabled(true);
                        } else {
                            Toast.makeText(this, "Child age not found. Please enter child details.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Child details missing. Please enter details.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch child details.", Toast.LENGTH_SHORT).show();
                });

        btnStartTest.setOnClickListener(view -> {
            if (childAgeInMonths != -1) {
                Intent intent;
                if (childAgeInMonths < 18) {
                    intent = new Intent(HomeActivity.this, QuestionnaireActivity.class);
                } else {
                    intent = new Intent(HomeActivity.this, AutismQuestionActivity.class);
                }
                intent.putExtra("childAgeInMonths", childAgeInMonths);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Child age is missing. Please enter child details first.", Toast.LENGTH_LONG).show();
            }
        });

        btnAboutUs.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
        });

        btnLogout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Show result button if test was completed
        SharedPreferences prefs = getSharedPreferences("CognisitePrefs", MODE_PRIVATE);
        boolean testCompleted = prefs.getBoolean("testCompleted", false);
        if (testCompleted) {
            btnViewResult.setVisibility(Button.VISIBLE);
        }

        btnViewResult.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ResultActivity.class));
        });

        // Open Profile Page
        profileIcon.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
