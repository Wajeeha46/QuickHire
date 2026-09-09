package com.wajeeha.quickhire;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.wajeeha.quickhire.models.Worker;
import com.google.firebase.firestore.FirebaseFirestore;

public class WorkerProfileActivity extends AppCompatActivity {
    private Worker worker;
    private FirebaseFirestore db;
    private MediaPlayer buttonSound;
    private MediaPlayer imageClickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);

        // Initialize sound effects
        buttonSound = MediaPlayer.create(this, R.raw.btn1);
        imageClickSound = MediaPlayer.create(this, R.raw.btn1);

        db = FirebaseFirestore.getInstance();
        worker = (Worker) getIntent().getSerializableExtra("worker");

        if (worker == null) {
            Toast.makeText(this, R.string.worker_data_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView workerImage = findViewById(R.id.workerImage);
        TextView nameText = findViewById(R.id.nameText);
        TextView categoryText = findViewById(R.id.categoryText);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView rateText = findViewById(R.id.rateText);
        Button hireButton = findViewById(R.id.hireButton);
        View availabilityDot = findViewById(R.id.availabilityDot);

        Glide.with(this)
                .load(worker.getImageUrl())
                .placeholder(R.drawable.worker_placeholder)
                .into(workerImage);

        nameText.setText(worker.getName());
        categoryText.setText(worker.getCategory());
        ratingBar.setRating((float) worker.getRating());
        rateText.setText(getString(R.string.hourly_rate_format, worker.getHourlyRate()));

        // Update availability status
        if (worker.isAvailable()) {
            availabilityDot.setBackgroundResource(R.drawable.available_dot);
            hireButton.setEnabled(true);
            hireButton.setText(R.string.hire_now);
            hireButton.setOnClickListener(v -> {
                playSound(buttonSound);
                openHireForm();
            });
        } else {
            availabilityDot.setBackgroundResource(R.drawable.unavailable_dot);
            hireButton.setEnabled(false);
            hireButton.setText(R.string.currently_hired);
        }

        // Add click listener to worker image with sound
        workerImage.setOnClickListener(v -> {
            playSound(imageClickSound);
            Intent intent = new Intent(this, WorkerDetailsActivity.class);
            intent.putExtra("worker", worker);
            startActivity(intent);
        });
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(0); // Reset to beginning for next play
        }
    }

    private void openHireForm() {
        Intent intent = new Intent(this, HireFormActivity.class);
        intent.putExtra("worker", worker);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
        if (imageClickSound != null) {
            imageClickSound.release();
            imageClickSound = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}