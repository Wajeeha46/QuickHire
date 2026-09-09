package com.wajeeha.quickhire;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, messageEditText;
    private Button submitButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private MediaPlayer buttonSound, focusSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // Initialize sound effects
        buttonSound = MediaPlayer.create(this, R.raw.btn1);
        focusSound = MediaPlayer.create(this, R.raw.btn1);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        messageEditText = findViewById(R.id.messageEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up focus change listeners for input fields
        setupFocusListeners();

        submitButton.setOnClickListener(v -> {
            playSound(buttonSound);
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();

            if (validateInputs(name, email, message)) {
                if (isNetworkAvailable()) {
                    submitContactForm(name, email, message);
                } else {
                    Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupFocusListeners() {
        nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) playSound(focusSound);
        });
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) playSound(focusSound);
        });
        messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) playSound(focusSound);
        });
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(0); // Reset to beginning for next play
        }
    }

    private boolean validateInputs(String name, String email, String message) {
        boolean isValid = true;

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        } else {
            nameEditText.setError(null);
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        if (message.isEmpty()) {
            messageEditText.setError("Message is required");
            isValid = false;
        } else if (message.length() < 10) {
            messageEditText.setError("Message should be at least 10 characters");
            isValid = false;
        } else {
            messageEditText.setError(null);
        }

        return isValid;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void submitContactForm(String name, String email, String message) {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        Map<String, Object> contact = new HashMap<>();
        contact.put("name", name);
        contact.put("email", email);
        contact.put("message", message);
        contact.put("timestamp", System.currentTimeMillis());

        db.collection("contact_forms")
                .add(contact)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Thank you for contacting us! We'll get back to you soon.", Toast.LENGTH_SHORT).show();
                        clearForm();
                    } else {
                        handleSubmissionError(task.getException());
                    }
                });
    }

    private void handleSubmissionError(Exception e) {
        String errorMsg = "Failed to submit form: ";

        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
            switch (firestoreEx.getCode()) {
                case PERMISSION_DENIED:
                    errorMsg += "Authentication required. Please sign in.";
                    break;
                case UNAVAILABLE:
                    errorMsg += "Service unavailable. Please try again later.";
                    break;
                default:
                    errorMsg += "Technical issue. Please try again.";
            }
        } else {
            errorMsg += e.getMessage();
        }

        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        Log.e("ContactUs", "Form submission error", e);
    }

    private void clearForm() {
        nameEditText.setText("");
        emailEditText.setText("");
        messageEditText.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
        if (focusSound != null) {
            focusSound.release();
            focusSound = null;
        }
    }
}