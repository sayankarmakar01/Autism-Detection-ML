package com.example.cognisite;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    private ImageView logoImageView, backgroundImageView;
    private TextView titleTextView, contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Initialize Views
        backgroundImageView = findViewById(R.id.background_image);
        logoImageView = findViewById(R.id.logo);
        titleTextView = findViewById(R.id.about_title);
        contentTextView = findViewById(R.id.about_content);

        // Optionally set dynamic content (if needed)
        titleTextView.setText("About Us");

        contentTextView.setText("Cognisite is dedicated to early childhood cognitive and behavioral assessments. "
                + "Our mission is to provide accessible tools that help parents, educators, and healthcare providers "
                + "identify developmental patterns in children. We believe in using technology to empower better "
                + "understanding and earlier interventions.");
    }
}
