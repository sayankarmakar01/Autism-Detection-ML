package com.example.cognisite;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VoiceMatchActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private TextView textViewResult;
    private Button btnStartVoice;

    private FirebaseFirestore firestore;

    private final String expectedText = "I want to play ball";
    private String recognizedText = "";
    private int voiceScore = 0;
    private int voicePercentage = 0;

    private int finalScore = 0;
    private int finalPercentage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_match);

        textViewResult = findViewById(R.id.textViewResult);
        btnStartVoice = findViewById(R.id.btnStartVoice);

        firestore = FirebaseFirestore.getInstance();

        btnStartVoice.setOnClickListener(v -> startVoiceInput());
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                recognizedText = result.get(0);
                textViewResult.setText("Recognized: " + recognizedText);

                calculateVoiceScore();
                updateAndSaveFinalResult();
            }
        }
    }

    private void calculateVoiceScore() {
        String[] expectedWords = expectedText.trim().toLowerCase().split("\\s+");
        String[] spokenWords = recognizedText.trim().toLowerCase().split("\\s+");

        int matchCount = 0;
        for (int i = 0; i < Math.min(expectedWords.length, spokenWords.length); i++) {
            if (expectedWords[i].equals(spokenWords[i])) {
                matchCount++;
            }
        }

        voiceScore = matchCount;
        voicePercentage = (matchCount * 100) / expectedWords.length;
    }

    private void updateAndSaveFinalResult() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("final_results").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long prevScore = documentSnapshot.getLong("final_score");
                        Long prevPercentage = documentSnapshot.getLong("final_percentage");

                        if (prevScore == null || prevPercentage == null) {
                            Toast.makeText(this, "Previous data incomplete", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        finalScore = prevScore.intValue() + voiceScore;
                        finalPercentage = (prevPercentage.intValue() + voicePercentage) / 2;

                        Map<String, Object> updatedResult = new HashMap<>();
                        updatedResult.put("final_score", finalScore);
                        updatedResult.put("final_percentage", finalPercentage);
                        updatedResult.put("voice_score", voiceScore);
                        updatedResult.put("voice_percentage", voicePercentage);
                        updatedResult.put("voice_text", recognizedText);
                        updatedResult.put("timestamp", System.currentTimeMillis());

                        firestore.collection("final_results").document(userId)
                                .update(updatedResult)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Voice result saved", Toast.LENGTH_SHORT).show();
                                    // ✅ Redirect to HomeActivity after saving
                                    Intent intent = new Intent(VoiceMatchActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Previous final result not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching final result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
