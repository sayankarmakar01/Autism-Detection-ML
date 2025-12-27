package com.example.cognisite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText etUserName, etUserEmail, etUserMobile;
    EditText etChildName, etChildAge, etChildGender, etChildRelation;
    Button btnUpdate;

    FirebaseAuth auth;
    FirebaseFirestore db;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        etUserName = findViewById(R.id.et_user_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserMobile = findViewById(R.id.et_user_mobile);
        etChildName = findViewById(R.id.et_child_name);
        etChildAge = findViewById(R.id.et_child_age);
        etChildGender = findViewById(R.id.et_child_gender);
        etChildRelation = findViewById(R.id.et_child_relation);
        btnUpdate = findViewById(R.id.btn_update);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();
        etUserEmail.setText(user.getEmail());

        // Load existing data
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etUserName.setText(doc.getString("name"));
                        etUserMobile.setText(doc.getString("mobile"));
                    }
                });

        db.collection("children").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etChildName.setText(doc.getString("childName"));
                        etChildAge.setText(String.valueOf(doc.getLong("childAgeInMonths")));
                        etChildGender.setText(doc.getString("gender"));
                        etChildRelation.setText(doc.getString("relation"));
                    }
                });

        // Handle update
        btnUpdate.setOnClickListener(v -> {
            String name = etUserName.getText().toString().trim();
            String mobile = etUserMobile.getText().toString().trim();
            String childName = etChildName.getText().toString().trim();
            String childAgeStr = etChildAge.getText().toString().trim();
            String gender = etChildGender.getText().toString().trim();
            String relation = etChildRelation.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty() || childName.isEmpty() || childAgeStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int childAge = Integer.parseInt(childAgeStr);

            // Update user
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("mobile", mobile);

            db.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show());

            // Update child
            Map<String, Object> childMap = new HashMap<>();
            childMap.put("childName", childName);
            childMap.put("childAgeInMonths", childAge);
            childMap.put("gender", gender);
            childMap.put("relation", relation);

            db.collection("children").document(uid)
                    .set(childMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Child updated", Toast.LENGTH_SHORT).show());
        });
    }
}
