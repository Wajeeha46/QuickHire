package com.wajeeha.quickhire;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_CODE = 1001;

    private EditText emailEditText, passwordEditText, repeatPasswordEditText;
    private EditText nameEditText, phoneEditText, addressEditText;
    private Button signupButton;
    private ImageButton locationButton;
    private TextView loginRedirectText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private MediaPlayer buttonSound, focusSound;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize sounds
        buttonSound = MediaPlayer.create(this, R.raw.btn1);
        focusSound = MediaPlayer.create(this, R.raw.btn1);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup UI components
        initViews();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etpassword);
        repeatPasswordEditText = findViewById(R.id.et_repeat_password);
        nameEditText = findViewById(R.id.etName);
        phoneEditText = findViewById(R.id.etPhone);
        addressEditText = findViewById(R.id.etAddress);
        signupButton = findViewById(R.id.btnsignup);
        locationButton = findViewById(R.id.btnLocation);
        loginRedirectText = findViewById(R.id.tv_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Creating your account...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        // Set focus change listeners for EditText fields
        setupEditTextFocusSounds(emailEditText);
        setupEditTextFocusSounds(passwordEditText);
        setupEditTextFocusSounds(repeatPasswordEditText);
        setupEditTextFocusSounds(nameEditText);
        setupEditTextFocusSounds(phoneEditText);
        setupEditTextFocusSounds(addressEditText);

        locationButton.setOnClickListener(v -> {
            playSound(buttonSound);
            if (checkLocationPermission()) {
                getCurrentLocation();
            }
        });

        signupButton.setOnClickListener(v -> {
            playSound(buttonSound);
            attemptSignup();
        });

        loginRedirectText.setOnClickListener(v -> {
            playSound(buttonSound);
            navigateToLogin();
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        progressDialog.setMessage("Getting your location...");
        progressDialog.show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    progressDialog.dismiss();
                    if (location != null) {
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                StringBuilder addressText = new StringBuilder();
                                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                    addressText.append(address.getAddressLine(i));
                                    if (i < address.getMaxAddressLineIndex()) {
                                        addressText.append(", ");
                                    }
                                }
                                addressEditText.setText(addressText.toString());
                            } else {
                                addressEditText.setText(String.format(Locale.getDefault(),
                                        "Lat: %.6f, Lng: %.6f",
                                        location.getLatitude(),
                                        location.getLongitude()));
                            }
                        } catch (IOException e) {
                            addressEditText.setText(String.format(Locale.getDefault(),
                                    "Lat: %.6f, Lng: %.6f",
                                    location.getLatitude(),
                                    location.getLongitude()));
                            Toast.makeText(this, "Could not get address, using coordinates instead", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupEditTextFocusSounds(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                playSound(focusSound);
            }
        });
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(0);
        }
    }

    private void attemptSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (validateInputs(email, password, repeatPassword, name, phone, address)) {
            signUpUser(email, password, name, phone, address);
        }
    }

    private boolean validateInputs(String email, String password, String repeatPassword,
                                   String name, String phone, String address) {
        boolean isValid = true;

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone is required");
            isValid = false;
        } else if (!phone.matches("^03\\d{9}$")) {
            phoneEditText.setError("Enter valid 11-digit number starting with 03");
            isValid = false;
        }

        if (address.isEmpty()) {
            addressEditText.setError("Address is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be ≥6 characters");
            isValid = false;
        }

        if (!password.equals(repeatPassword)) {
            repeatPasswordEditText.setError("Passwords must match");
            isValid = false;
        }

        return isValid;
    }

    private void signUpUser(String email, String password, String name, String phone, String address) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        handleSignupSuccess(email, name, phone, address);
                    } else {
                        handleSignupFailure(task.getException());
                    }
                });
    }

    private void handleSignupSuccess(String email, String name, String phone, String address) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(emailTask -> {
                        String message = emailTask.isSuccessful() ?
                                "Signup successful! Please verify your email." :
                                "Signup successful but verification email failed to send.";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    });

            saveUserToFirestore(user.getUid(), email, name, phone, address);
            navigateToLogin();
        }
    }

    private void handleSignupFailure(Exception exception) {
        String message = exception != null && exception.getMessage() != null
                ? exception.getMessage()
                : "Unable to create account. Please try again.";
        Toast.makeText(this, "Signup failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void saveUserToFirestore(String userId, String email, String name, String phone, String address) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("phone", phone);
        user.put("address", address);
        user.put("createdAt", FieldValue.serverTimestamp());
        user.put("verified", false);

        db.collection("users").document(userId)
                .set(user)
                .addOnFailureListener(e -> {
                    Log.w("Signup", "Error saving user", e);
                    Toast.makeText(this,
                            "Account created but profile setup incomplete.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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