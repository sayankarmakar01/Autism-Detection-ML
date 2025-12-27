package com.example.cognisite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DSMActivity extends AppCompatActivity {

    private RadioGroup[] radioGroups = new RadioGroup[10];
    private Button btnSubmit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private int previousScore = 0;
    private int previousPercentage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsm_questions);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get previous score and percentage from Intent
        previousScore = getIntent().getIntExtra("score", 0);
        previousPercentage = getIntent().getIntExtra("percentage", 0);

        // Link radio groups dynamically
        for (int i = 0; i < 10; i++) {
            String radioGroupID = "dsmquestion" + (i + 1) + "_radiogroup";
            int resID = getResources().getIdentifier(radioGroupID, "id", getPackageName());
            radioGroups[i] = findViewById(resID);
        }

        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(v -> {
            String[] answers = new String[10];
            int yesCount = 0;

            for (int i = 0; i < radioGroups.length; i++) {
                answers[i] = getSelectedAnswer(radioGroups[i]);
                if (answers[i] == null) {
                    Toast.makeText(this, "Please answer Question " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (answers[i].equalsIgnoreCase("Yes")) {
                    yesCount++;
                }
            }

            int dsmPercentage = (yesCount * 100) / 10;
            int totalScore = previousScore + yesCount;
            int totalPercentage = (previousPercentage + dsmPercentage) / 2;

            saveToFirestore(answers, yesCount, dsmPercentage, totalScore, totalPercentage);
        });
    }

    private String getSelectedAnswer(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return null;

        RadioButton selected = findViewById(selectedId);
        return selected.getText().toString();
    }

    private void saveToFirestore(String[] answers, int dsmScore, int dsmPercentage, int totalScore, int totalPercentage) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < answers.length; i++) {
            data.put("dsm_question" + (i + 1), answers[i]);
        }

        data.put("dsm_score", dsmScore);
        data.put("dsm_percentage", dsmPercentage);
        data.put("total_score", totalScore);
        data.put("total_percentage", totalPercentage);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("dsm_responses")
                .document(userId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Responses saved!", Toast.LENGTH_SHORT).show();

                    // Always go to ImageActivity now
                    Intent intent = new Intent(DSMActivity.this, ImageActivity.class);
                    intent.putExtra("score", totalScore);
                    intent.putExtra("percentage", totalPercentage);
                    intent.putExtra("total", 40);
                    intent.putExtra("testType", "long+DSM");
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
