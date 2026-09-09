package com.example.quickhire;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    private MediaPlayer buttonSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize the button sound
        buttonSound = MediaPlayer.create(this, R.raw.btn1);

        // Initialize all buttons
        Button categoriesButton = findViewById(R.id.categoriesButton);
        Button contactUsButton = findViewById(R.id.contactUsButton);
        Button aboutUsButton = findViewById(R.id.aboutUsButton);
        Button userButton = findViewById(R.id.userButton);

        // Set click listeners with sound effects
        categoriesButton.setOnClickListener(v -> {
            playButtonSound();
            startActivity(new Intent(MainPageActivity.this, CategoriesActivity.class));
        });

        contactUsButton.setOnClickListener(v -> {
            playButtonSound();
            startActivity(new Intent(MainPageActivity.this, ContactUsActivity.class));
        });

        aboutUsButton.setOnClickListener(v -> {
            playButtonSound();
            startActivity(new Intent(MainPageActivity.this, AboutUsActivity.class));
        });

        userButton.setOnClickListener(v -> {
            playButtonSound();
            startActivity(new Intent(MainPageActivity.this, UserActivity.class));
        });
    }

    private void playButtonSound() {
        if (buttonSound != null) {
            buttonSound.start();
            // Reset the sound to beginning for next play
            buttonSound.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer resources when activity is destroyed
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
    }
}