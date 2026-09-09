package com.example.quickhire;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupRedirectText;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private MediaPlayer buttonSound, focusSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize sounds
        buttonSound = MediaPlayer.create(this, R.raw.btn1);
        focusSound = MediaPlayer.create(this, R.raw.btn1);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        emailEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        // Set focus change listeners for EditText fields (placeholders)
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                playSound(focusSound);
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                playSound(focusSound);
            }
        });

        loginButton.setOnClickListener(v -> {
            playSound(buttonSound);
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInputs(email, password)) {
                loginUser(email, password);
            }
        });

        signupRedirectText.setOnClickListener(v -> {
            playSound(buttonSound);
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(0);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                startActivity(new Intent(LoginActivity.this, MainPageActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this,
                                        "Please verify your email first. Check your inbox.",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (buttonSound != null) {
            buttonSound.release();
        }
        if (focusSound != null) {
            focusSound.release();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}