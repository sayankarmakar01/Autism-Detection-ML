package com.example.cognisite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean answeredBat = false;
    private boolean answeredCat = false;
    private boolean answeredBall = false;

    private boolean isBatCorrect = false;
    private boolean isCatCorrect = false;
    private boolean isBallCorrect = false;

    private int previousScore = 0;
    private int previousPercentage = 0;

    private Button buttonBat, buttonCat, buttonBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        previousScore = getIntent().getIntExtra("score", 0);
        previousPercentage = getIntent().getIntExtra("percentage", 0);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Buttons for correct answers
        buttonBat = findViewById(R.id.buttonClick1);
        buttonCat = findViewById(R.id.buttonClick3);
        buttonBall = findViewById(R.id.buttonClick8);

        // Other options (not correct)
        Button[] otherButtons = {
                findViewById(R.id.buttonClick),
                findViewById(R.id.buttonClick2),
                findViewById(R.id.buttonClick4),
                findViewById(R.id.buttonClick5),
                findViewById(R.id.buttonClick6),
                findViewById(R.id.buttonClick7)
        };

        // Assign correct answer listeners
        buttonBat.setOnClickListener(v -> {
            resetBatColors();
            buttonBat.setBackgroundColor(Color.parseColor("#4CAF50"));
            answeredBat = true;
            isBatCorrect = true;
            Toast.makeText(this, "Correct! It’s Bat 🏏", Toast.LENGTH_SHORT).show();
        });

        buttonCat.setOnClickListener(v -> {
            resetCatColors();
            buttonCat.setBackgroundColor(Color.parseColor("#4CAF50"));
            answeredCat = true;
            isCatCorrect = true;
            Toast.makeText(this, "Correct! It’s Cat 🐱", Toast.LENGTH_SHORT).show();
        });

        buttonBall.setOnClickListener(v -> {
            resetBallColors();
            buttonBall.setBackgroundColor(Color.parseColor("#4CAF50"));
            answeredBall = true;
            isBallCorrect = true;
            Toast.makeText(this, "Correct! It’s Ball ⚽", Toast.LENGTH_SHORT).show();
        });

        // Wrong answer listeners
        for (Button btn : otherButtons) {
            btn.setOnClickListener(v -> {
                String label = btn.getText().toString();
                btn.setBackgroundColor(Color.parseColor("#F44336"));
                Toast.makeText(this, label + " is not correct!", Toast.LENGTH_SHORT).show();

                if (btn.getId() == R.id.buttonClick || btn.getId() == R.id.buttonClick1 || btn.getId() == R.id.buttonClick2) {
                    answeredBat = true;
                    isBatCorrect = false;
                    resetBatColors();
                    btn.setBackgroundColor(Color.parseColor("#F44336"));
                } else if (btn.getId() == R.id.buttonClick3 || btn.getId() == R.id.buttonClick4 || btn.getId() == R.id.buttonClick5) {
                    answeredCat = true;
                    isCatCorrect = false;
                    resetCatColors();
                    btn.setBackgroundColor(Color.parseColor("#F44336"));
                } else {
                    answeredBall = true;
                    isBallCorrect = false;
                    resetBallColors();
                    btn.setBackgroundColor(Color.parseColor("#F44336"));
                }
            });
        }

        Button submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(v -> {
            if (!answeredBat || !answeredCat || !answeredBall) {
                Toast.makeText(this, "Please answer all questions before submitting!", Toast.LENGTH_SHORT).show();
                return;
            }

            int correctCount = 0;
            if (isBatCorrect) correctCount++;
            if (isCatCorrect) correctCount++;
            if (isBallCorrect) correctCount++;

            saveResults(correctCount);
        });
    }

    private void resetBatColors() {
        findViewById(R.id.buttonClick).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick1).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick2).setBackgroundColor(Color.parseColor("#6200EE"));
    }

    private void resetCatColors() {
        findViewById(R.id.buttonClick3).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick4).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick5).setBackgroundColor(Color.parseColor("#6200EE"));
    }

    private void resetBallColors() {
        findViewById(R.id.buttonClick6).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick7).setBackgroundColor(Color.parseColor("#6200EE"));
        findViewById(R.id.buttonClick8).setBackgroundColor(Color.parseColor("#6200EE"));
    }

    private void saveResults(int correctCount) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        int imagePercentage = (correctCount * 100) / 3;
        int finalTotalScore = previousScore + correctCount;
        int finalTotalPercentage = (previousPercentage + imagePercentage) / 2;

        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("previous_score", previousScore);
        finalResult.put("previous_percentage", previousPercentage);
        finalResult.put("image_score", correctCount);
        finalResult.put("image_percentage", imagePercentage);
        finalResult.put("final_score", finalTotalScore);
        finalResult.put("final_percentage", finalTotalPercentage);
        finalResult.put("timestamp", System.currentTimeMillis());

        db.collection("final_results")
                .document(userId)
                .set(finalResult)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Final result saved!", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("CognisitePrefs", MODE_PRIVATE);
                    int childAgeInMonths = prefs.getInt("childAgeInMonths", -1);

                    if (childAgeInMonths > 60) {
                        startActivity(new Intent(ImageActivity.this, VoiceMatchActivity.class));
                    } else {
                        startActivity(new Intent(ImageActivity.this, ResultActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving result: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
