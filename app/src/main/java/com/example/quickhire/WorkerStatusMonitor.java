package com.example.quickhire;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class WorkerStatusMonitor extends Service {
    private static final String TAG = "WorkerStatusMonitor";
    private Handler handler = new Handler();
    private Runnable runnable;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        startMonitoring();
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                checkWorkerAvailability();
                handler.postDelayed(this, 3600000); // Check every hour
            }
        };
        handler.post(runnable);
    }

    private void checkWorkerAvailability() {
        db.collection("workers")
                .whereEqualTo("available", false)
                .whereLessThan("hireExpiryTime", new Date())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Ensure we don't clear userId when making available
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("available", true);
                        updates.put("hiredBy", FieldValue.delete());
                        updates.put("hireExpiryTime", FieldValue.delete());

                        // Preserve the userId field
                        if (document.getString("userId") != null) {
                            updates.put("userId", document.getString("userId"));
                        }

                        db.collection("workers").document(document.getId()).update(updates)
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Worker " + document.getId() + " made available"))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error updating worker status", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking worker availability", e));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
