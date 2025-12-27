package com.example.cognisite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChildDetailActivity extends AppCompatActivity {

    private EditText etChildName, etChildAge;
    private Spinner spinnerRelation;
    private RadioGroup rgGender;
    private Button btnSubmit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        etChildName = findViewById(R.id.et_child_name);
        etChildAge = findViewById(R.id.et_child_age);
        spinnerRelation = findViewById(R.id.spinner_relation);
        rgGender = findViewById(R.id.rg_gender);
        btnSubmit = findViewById(R.id.btn_submit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Populate spinner with relation options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.relation_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelation.setAdapter(adapter);

        btnSubmit.setOnClickListener(view -> saveChildDetails());
    }

    private void saveChildDetails() {
        String childName = etChildName.getText().toString().trim();
        String childAgeStr = etChildAge.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem().toString();
        int selectedGenderId = rgGender.getCheckedRadioButtonId();

        // Input validation
        if (childName.isEmpty()) {
            etChildName.setError("Child name required");
            return;
        }
        if (childAgeStr.isEmpty()) {
            etChildAge.setError("Age required");
            return;
        }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        int childAgeYears;
        try {
            childAgeYears = Integer.parseInt(childAgeStr);
        } catch (NumberFormatException e) {
            etChildAge.setError("Enter a valid number");
            return;
        }

        int childAgeInMonths = childAgeYears * 12;
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString();

        // Prepare data
        Map<String, Object> childData = new HashMap<>();
        childData.put("childName", childName);
        childData.put("relation", relation);
        childData.put("gender", gender);
        childData.put("childAgeInMonths", childAgeInMonths);

        // Get UID and save to Firestore
        String userId = auth.getCurrentUser().getUid();
        db.collection("children")
                .document(userId)
                .set(childData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Child details saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("childAgeInMonths", childAgeInMonths); // Optional
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save child details", Toast.LENGTH_SHORT).show();
                });
    }
}
