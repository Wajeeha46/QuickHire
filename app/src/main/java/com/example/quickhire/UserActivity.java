package com.wajeeha.quickhire;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";

    // Views
    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserAddress;
    private RecyclerView activeHiresRecyclerView;
    private TextView tvNoWorkerHired;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Adapter
    private ActiveHiresAdapter activeHiresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Initialize views
            initializeViews();

            // Load data
            loadUserData();
            checkActiveHires();

        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            tvUserName = findViewById(R.id.tvUserName);
            tvUserEmail = findViewById(R.id.tvUserEmail);
            tvUserPhone = findViewById(R.id.tvUserPhone);
            tvUserAddress = findViewById(R.id.tvUserAddress);
            activeHiresRecyclerView = findViewById(R.id.activeHiresRecyclerView);
            tvNoWorkerHired = findViewById(R.id.tvNoWorkerHired);

            // Setup RecyclerView
            activeHiresRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            activeHiresAdapter = new ActiveHiresAdapter();
            activeHiresRecyclerView.setAdapter(activeHiresAdapter);

        } catch (Exception e) {
            Log.e(TAG, "View initialization failed", e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // Set email from auth
            tvUserEmail.setText("Email: " + currentUser.getEmail());

            // Load additional data from Firestore
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                tvUserName.setText("Name: " + document.getString("name"));
                                tvUserPhone.setText("Phone: " + document.getString("phone"));
                                tvUserAddress.setText("Address: " + document.getString("address"));
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error loading user data", e);
        }
    }

    private void checkActiveHires() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("hires")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ActiveHire> activeHires = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Long hoursValue = document.getLong("hours");
                                int hours = hoursValue != null ? Math.max(1, hoursValue.intValue()) : 1;

                                ActiveHire hire = new ActiveHire(
                                        document.getString("workerId"),
                                        document.getString("customerName"),
                                        document.getString("address"),
                                        document.getString("phone"),
                                        document.getString("email"),
                                        document.getDate("expiryTime"),
                                        calculateArrivalTime(document),
                                        hours
                                );
                                activeHires.add(hire);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing hire document", e);
                            }
                        }

                        if (activeHires.isEmpty()) {
                            showNoHires();
                        } else {
                            showActiveHires(activeHires);
                        }
                    } else {
                        showNoHires();
                    }
                });
    }

    private Date calculateArrivalTime(QueryDocumentSnapshot document) {
        try {
            Date createdAt = document.getDate("createdAt");
            if (createdAt == null) {
                createdAt = new Date();
            }
            Long hours = document.getLong("hours");
            long safeHours = hours != null ? Math.max(1L, hours) : 1L;
            long arrivalMillis = (long) (safeHours * 3600000 * 0.02); // 2% of total time
            return new Date(createdAt.getTime() + arrivalMillis);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating arrival time", e);
            return new Date();
        }
    }

    private void showActiveHires(List<ActiveHire> activeHires) {
        tvNoWorkerHired.setVisibility(View.GONE);
        activeHiresRecyclerView.setVisibility(View.VISIBLE);
        activeHiresAdapter.setHires(activeHires);
    }

    private void showNoHires() {
        tvNoWorkerHired.setVisibility(View.VISIBLE);
        activeHiresRecyclerView.setVisibility(View.GONE);
    }

    private static class ActiveHire {
        private final String workerId;
        private String workerName;
        private String workerEmail;
        private String workerPhone;
        private final String customerName;
        private final String address;
        private final String phone;
        private final String email;
        private final Date expiryTime;
        private final Date arrivalTime;
        private final int hours;

        public ActiveHire(String workerId, String customerName, String address,
                          String phone, String email, Date expiryTime,
                          Date arrivalTime, int hours) {
            this.workerId = workerId;
            this.customerName = customerName;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.expiryTime = expiryTime;
            this.arrivalTime = arrivalTime;
            this.hours = hours;
        }

        // Getters and setters
        public String getWorkerId() { return workerId; }
        public String getWorkerName() { return workerName; }
        public void setWorkerName(String workerName) { this.workerName = workerName; }
        public String getWorkerEmail() { return workerEmail; }
        public void setWorkerEmail(String workerEmail) { this.workerEmail = workerEmail; }
        public String getWorkerPhone() { return workerPhone; }
        public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }
        public String getCustomerName() { return customerName; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public Date getExpiryTime() { return expiryTime; }
        public Date getArrivalTime() { return arrivalTime; }
        public int getHours() { return hours; }
    }

    private class ActiveHiresAdapter extends RecyclerView.Adapter<ActiveHiresAdapter.ViewHolder> {
        private List<ActiveHire> hires = new ArrayList<>();

        public void setHires(List<ActiveHire> hires) {
            this.hires = hires != null ? hires : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_active_hire, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActiveHire hire = hires.get(position);

            // Set worker info
            holder.workerName.setText("Worker: " + hire.getWorkerName());
            holder.workerEmail.setText("Email: " + hire.getWorkerEmail());
            holder.workerPhone.setText("Phone: " + hire.getWorkerPhone());

            // Set customer info
            holder.customerName.setText("Customer: " + hire.getCustomerName());
            holder.address.setText("Address: " + hire.getAddress());
            holder.phone.setText("Phone: " + hire.getPhone());
            holder.email.setText("Email: " + hire.getEmail());

            // Set arrival time
            if (hire.getArrivalTime() != null) {
                long remaining = hire.getArrivalTime().getTime() - System.currentTimeMillis();
                if (remaining > 0) {
                    startCountdown(holder.arrivalTime, holder.progressBar, remaining);
                } else {
                    holder.arrivalTime.setText("Worker has arrived!");
                    holder.progressBar.setProgress(100);
                }
            }

            // Load worker details if missing
            if (hire.getWorkerName() == null) {
                db.collection("workers").document(hire.getWorkerId())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                hire.setWorkerName(document.getString("name"));
                                hire.setWorkerEmail(document.getString("email"));
                                hire.setWorkerPhone(document.getString("phone"));
                                notifyItemChanged(position);
                            }
                        });
            }
        }

        @Override
        public int getItemCount() {
            return hires.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView workerName, workerEmail, workerPhone;
            TextView customerName, address, phone, email;
            TextView arrivalTime;
            ProgressBar progressBar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                workerName = itemView.findViewById(R.id.workerName);
                workerEmail = itemView.findViewById(R.id.workerEmail);
                workerPhone = itemView.findViewById(R.id.workerPhone);
                customerName = itemView.findViewById(R.id.customerName);
                address = itemView.findViewById(R.id.address);
                phone = itemView.findViewById(R.id.phone);
                email = itemView.findViewById(R.id.email);
                arrivalTime = itemView.findViewById(R.id.arrivalTime);
                progressBar = itemView.findViewById(R.id.progressBar);
            }
        }

        private void startCountdown(TextView textView, ProgressBar progressBar, long millis) {
            new CountDownTimer(millis, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    textView.setText(String.format("Arriving in %dm %ds", minutes, seconds));

                    int progress = (int) (100 - (millisUntilFinished * 100 / millis));
                    progressBar.setProgress(progress);
                }

                public void onFinish() {
                    textView.setText("Worker has arrived!");
                    progressBar.setProgress(100);
                }
            }.start();
        }
    }
}