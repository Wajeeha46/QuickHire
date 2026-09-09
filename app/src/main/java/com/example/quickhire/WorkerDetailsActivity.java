package com.wajeeha.quickhire;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.wajeeha.quickhire.models.Worker;

public class WorkerDetailsActivity extends AppCompatActivity {
    private MediaPlayer buttonSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        // Initialize sound effect
        buttonSound = MediaPlayer.create(this, R.raw.btn1);

        Worker worker = (Worker) getIntent().getSerializableExtra("worker");
        if (worker == null) {
            finish();
            return;
        }

        // Initialize views
        ImageView workerImage = findViewById(R.id.detailWorkerImage);
        TextView nameText = findViewById(R.id.detailNameText);
        TextView categoryText = findViewById(R.id.detailCategoryText);
        TextView emailText = findViewById(R.id.detailEmailText);
        TextView phoneText = findViewById(R.id.detailPhoneText);
        TextView experienceText = findViewById(R.id.detailExperienceText);
        TextView bioText = findViewById(R.id.detailBioText);
        Button hireButton = findViewById(R.id.detailHireButton);

        // Load worker image
        Glide.with(this)
                .load(worker.getImageUrl())
                .placeholder(R.drawable.worker_placeholder)
                .into(workerImage);

        // Set worker details
        nameText.setText(worker.getName());
        categoryText.setText(worker.getCategory());
        emailText.setText(worker.getEmail() != null ? worker.getEmail() : "Not provided");
        phoneText.setText(worker.getPhone() != null ? worker.getPhone() : "Not provided");
        experienceText.setText(worker.getExperience() != null ? worker.getExperience() : "Not specified");
        bioText.setText(worker.getBio() != null ? worker.getBio() : "No bio available");

        // Set up hire button with sound effect
        hireButton.setOnClickListener(v -> {
            playButtonSound();
            Intent intent = new Intent(this, HireFormActivity.class);
            intent.putExtra("worker", worker);
            startActivity(intent);
        });
    }

    private void playButtonSound() {
        if (buttonSound != null) {
            buttonSound.start();
            buttonSound.seekTo(0); // Reset to beginning for next play
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
    }
}