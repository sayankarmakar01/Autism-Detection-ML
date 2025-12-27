package com.example.cognisite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResultActivity extends AppCompatActivity {

    private TextView tvConclusion;
    private Button btnHome, btnRetake;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvConclusion = findViewById(R.id.tv_conclusion);
        btnHome = findViewById(R.id.btn_home);
        btnRetake = findViewById(R.id.btn_retake);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        int score = getIntent().getIntExtra("score", -1);
        int percentage = getIntent().getIntExtra("percentage", -1);
        int total = getIntent().getIntExtra("total", -1);

        Log.d(TAG, "Intent Score: " + score + ", Percentage: " + percentage + ", Total: " + total);

        if (score != -1 && percentage != -1 && total != -1) {
            Toast.makeText(this, "Loaded from Intent", Toast.LENGTH_SHORT).show();
            displayConclusion(percentage);
        } else {
            Toast.makeText(this, "Loading from Firestore...", Toast.LENGTH_SHORT).show();
            loadResultFromFirestore();
        }

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnRetake.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, QuestionnaireActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadResultFromFirestore() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("CognisitePrefs", MODE_PRIVATE);
        int childAgeInMonths = prefs.getInt("childAgeInMonths", -1);
        int totalQuestions = childAgeInMonths < 18 ? 10 : 30;

        db.collection("final_results").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long percentage = documentSnapshot.getLong("final_percentage");
                        Long score = documentSnapshot.getLong("final_score");

                        // Fallback to old keys if necessary
                        if (percentage == null) percentage = documentSnapshot.getLong("percentage");
                        if (score == null) score = documentSnapshot.getLong("score");

                        if (percentage != null && score != null) {
                            Log.d(TAG, "Firestore Score: " + score + ", Percentage: " + percentage);
                            displayConclusion(percentage.intValue());
                        } else {
                            Toast.makeText(this, "Incomplete data in Firestore", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Missing 'final_score' or 'final_percentage'");
                        }
                    } else {
                        Toast.makeText(this, "No result found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayConclusion(int percentage) {
        String conclusion = "Unable to determine result.";

        if (percentage >= 70) {
            conclusion = "Possible Signs of Autism. Please consult a professional.";
        } else if (percentage >= 0) {
            conclusion = "No significant signs detected.";
        }

        tvConclusion.setText(conclusion);
    }
}
