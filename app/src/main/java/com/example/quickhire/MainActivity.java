package com.wajeeha.quickhire;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer introSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Play app intro sound
        playIntroSound();

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            // Play button click sound
            playButtonClickSound();

            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });
    }

    private void playIntroSound() {
        introSound = MediaPlayer.create(this, R.raw.intro3);
        introSound.setOnCompletionListener(mp -> {
            mp.release();
            introSound = null;
        });
        introSound.start();
    }

    private void playButtonClickSound() {
        MediaPlayer buttonSound = MediaPlayer.create(this, R.raw.btn1);
        buttonSound.setOnCompletionListener(MediaPlayer::release);
        buttonSound.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (introSound != null) {
            introSound.release();
            introSound = null;
        }
    }
}