package com.example.cognisite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AutismQuestionActivity extends AppCompatActivity {

    private LinearLayout questionsLayout;
    private RadioGroup[] radioGroups;
    private List<String[]> questionList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int childAgeInMonths = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autism_questions); // Make sure this matches your layout

        childAgeInMonths = getIntent().getIntExtra("childAgeInMonths", -1);

        questionsLayout = findViewById(R.id.questions1Layout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        questionList = loadCSVFromAssets();
        radioGroups = new RadioGroup[questionList.size()];

        for (int i = 0; i < questionList.size(); i++) {
            String[] q = questionList.get(i);
            String question = q[0];

            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + question);
            tv.setPadding(0, 16, 0, 8);
            questionsLayout.addView(tv);

            RadioGroup rg = new RadioGroup(this);
            rg.setOrientation(RadioGroup.HORIZONTAL);

            RadioButton rbYes = new RadioButton(this);
            rbYes.setText("Yes");
            rbYes.setId(View.generateViewId());

            RadioButton rbNo = new RadioButton(this);
            rbNo.setText("No");
            rbNo.setId(View.generateViewId());

            rg.addView(rbYes);
            rg.addView(rbNo);

            questionsLayout.addView(rg);
            radioGroups[i] = rg;
        }

        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        submitButton.setTextSize(18f);
        submitButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        submitButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.custom_button_color));
        submitButton.setPadding(14, 14, 14, 14);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 24, 0, 16);
        submitButton.setLayoutParams(layoutParams);
        submitButton.setElevation(4f);

        submitButton.setOnClickListener(v -> calculateAndStoreScore());
        questionsLayout.addView(submitButton);
    }

    private List<String[]> loadCSVFromAssets() {
        List<String[]> questionAnswerList = new ArrayList<>();

        try {
            InputStream is = getAssets().open("question.csv"); // CSV filename
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String question = parts[0].trim();
                    String yesCol = parts[1].trim();
                    String noCol = parts[2].trim();

                    String correctAnswer;
                    if (yesCol.equals("1")) {
                        correctAnswer = "Yes";
                    } else if (noCol.equals("1")) {
                        correctAnswer = "No";
                    } else {
                        correctAnswer = "Unanswered";
                    }

                    questionAnswerList.add(new String[]{question, correctAnswer});
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questionAnswerList;
    }

    private void calculateAndStoreScore() {
        int correctCount = 0;
        int totalQuestions = questionList.size();
        Map<String, String> answersMap = new HashMap<>();

        for (int i = 0; i < radioGroups.length; i++) {
            RadioGroup group = radioGroups[i];
            int selectedId = group.getCheckedRadioButtonId();
            String userAnswer = "Unanswered";

            if (selectedId != -1) {
                RadioButton selected = findViewById(selectedId);
                userAnswer = selected.getText().toString();
            }

            String correctAnswer = questionList.get(i)[1];

            if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                correctCount++;
            }

            answersMap.put("Q" + (i + 1), userAnswer);
        }

        int finalScore = correctCount;
        int finalPercentage = (correctCount * 100) / totalQuestions;

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("score", finalScore);
        resultData.put("percentage", finalPercentage);
        resultData.put("timestamp", System.currentTimeMillis());

        db.collection("responses_30")
                .document(userId)
                .set(resultData)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences.Editor editor = getSharedPreferences("CognisitePrefs", MODE_PRIVATE).edit();
                    editor.putBoolean("testCompleted", true);
                    editor.putInt("childAgeInMonths", childAgeInMonths);
                    editor.apply();

                    Toast.makeText(this, "Result saved! Score: " + finalPercentage + "%", Toast.LENGTH_LONG).show();

                    if (finalPercentage > 70) {
                        Intent intent = new Intent(AutismQuestionActivity.this, DSMActivity.class);
                        intent.putExtra("score", finalScore);
                        intent.putExtra("percentage", finalPercentage);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(AutismQuestionActivity.this, ResultActivity.class);
                        intent.putExtra("score", finalScore);
                        intent.putExtra("percentage", finalPercentage);
                        intent.putExtra("total", totalQuestions);
                        intent.putExtra("testType", "csv_autism");
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
