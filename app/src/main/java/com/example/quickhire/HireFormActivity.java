package com.example.quickhire;

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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.quickhire.models.Worker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HireFormActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final String TAG = "HireFormActivity";

    private EditText etName, etAddress, etHours, etPhone, etEmail;
    private Worker worker;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private MediaPlayer buttonSound, focusSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire_form);

        // Initialize sound effects
        buttonSound = MediaPlayer.create(this, R.raw.btn1);
        focusSound = MediaPlayer.create(this, R.raw.btn1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Submitting hire request...");
        progressDialog.setCancelable(false);

        worker = (Worker) getIntent().getSerializableExtra("worker");
        if (worker == null) {
            Toast.makeText(this, "Worker information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etHours = findViewById(R.id.etHours);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);

        // Set focus change listeners for all EditText fields (placeholders)
        setupEditTextFocusSounds(etName);
        setupEditTextFocusSounds(etAddress);
        setupEditTextFocusSounds(etHours);
        setupEditTextFocusSounds(etPhone);
        setupEditTextFocusSounds(etEmail);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            etEmail.setText(currentUser.getEmail());
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        btnUseCurrentLocation.setOnClickListener(v -> {
            playSound(buttonSound);
            if (checkLocationPermission()) {
                getCurrentLocation();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            playSound(buttonSound);
            if (validateForm()) {
                submitHireRequest();
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
            mediaPlayer.seekTo(0); // Reset to beginning for next play
        }
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

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
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
                                etAddress.setText(addressText.toString());
                            } else {
                                etAddress.setText(String.format(Locale.getDefault(),
                                        "Lat: %.6f, Lng: %.6f",
                                        location.getLatitude(),
                                        location.getLongitude()));
                            }
                        } catch (IOException e) {
                            etAddress.setText(String.format(Locale.getDefault(),
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

    private boolean validateForm() {
        boolean valid = true;

        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String hours = etHours.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            valid = false;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            valid = false;
        }

        if (hours.isEmpty()) {
            etHours.setError("Hours required");
            valid = false;
        } else {
            try {
                int hoursValue = Integer.parseInt(hours);
                if (hoursValue <= 0) {
                    etHours.setError("Must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                etHours.setError("Invalid number");
                valid = false;
            }
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone required");
            valid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            etPhone.setError("Invalid phone number");
            valid = false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            valid = false;
        }

        return valid;
    }

    private void submitHireRequest() {
        progressDialog.show();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calculate expiry time
        int hours = Integer.parseInt(etHours.getText().toString());
        Date expiryTime = new Date(System.currentTimeMillis() + (hours * 3600 * 1000));

        // Create hire data
        Map<String, Object> hireData = new HashMap<>();
        hireData.put("customerName", etName.getText().toString().trim());
        hireData.put("address", etAddress.getText().toString().trim());
        hireData.put("hours", hours);
        hireData.put("phone", etPhone.getText().toString().trim());
        hireData.put("email", etEmail.getText().toString().trim());
        hireData.put("workerId", worker.getId());
        hireData.put("userId", currentUser.getUid());
        hireData.put("expiryTime", expiryTime);
        hireData.put("status", "active");
        hireData.put("createdAt", FieldValue.serverTimestamp());
        hireData.put("workerName", worker.getName());
        hireData.put("workerEmail", worker.getEmail());
        hireData.put("workerPhone", worker.getPhone());

        // Update worker status
        Map<String, Object> workerUpdates = new HashMap<>();
        workerUpdates.put("available", false);
        workerUpdates.put("hiredBy", currentUser.getUid());
        workerUpdates.put("lastHired", FieldValue.serverTimestamp());
        workerUpdates.put("hireExpiryTime", expiryTime);

        // First create hire document
        db.collection("hires").add(hireData)
                .addOnSuccessListener(hireRef -> {
                    // Then update worker status
                    db.collection("workers").document(worker.getId())
                            .update(workerUpdates)
                            .addOnSuccessListener(aVoid -> {
                                // Update hire document with its ID
                                hireRef.update("hireId", hireRef.getId())
                                        .addOnSuccessListener(aVoid1 -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Hired successfully!", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK);
                                            finish();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e(TAG, "Error updating worker: ", e);
                                hireRef.delete();
                                Toast.makeText(this, "Failed to update worker: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to create hire: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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
}