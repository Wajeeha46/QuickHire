package com.example.quickhire;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView aboutText = findViewById(R.id.aboutText);
        LinearLayout aboutLayout = findViewById(R.id.aboutLayout);
        ImageView logo = findViewById(R.id.logoImage);

        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        aboutLayout.startAnimation(fadeIn);

        String content = "Welcome to QuickHire!\n\n" +
                "QuickHire is the fastest way to connect with skilled professionals for all your short-term service needs. " +
                "Whether you need a maid, a chef, or a driver — we bring trusted experts right to your fingertips.\n\n" +
                "🌟 Key Features:\n" +
                "• Instant access to verified professionals\n" +
                "• Real-time availability tracking\n" +
                "• Simple, secure, and fast hiring process\n" +
                "• Multiple categories tailored to your daily needs\n\n" +
                "💡 Our Mission:\n" +
                "To revolutionize the service industry by bridging the gap between customers and short-term service providers with ease, trust, and speed.\n\n";

        aboutText.setText(content);
    }
}
