package com.wajeeha.quickhire;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wajeeha.quickhire.adapters.WorkerAdapter;
import com.wajeeha.quickhire.models.Worker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerClickListener {
    private static final String TAG = "CategoriesActivity";
    private FirebaseFirestore db;
    private MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        Log.d(TAG, "Activity created");

        // Initialize sound
        clickSound = MediaPlayer.create(this, R.raw.btn1);

        db = FirebaseFirestore.getInstance();

        setupCategoryRecyclerView(R.id.maidRecyclerView, "Maid");
        setupCategoryRecyclerView(R.id.driverRecyclerView, "Driver");
        setupCategoryRecyclerView(R.id.hairdresserRecyclerView, "Hairdresser");
        setupCategoryRecyclerView(R.id.tailorRecyclerView, "Tailor");
        setupCategoryRecyclerView(R.id.chefRecyclerView, "Chef");
        setupCategoryRecyclerView(R.id.plumberRecyclerView, "Plumber");
    }

    private void setupCategoryRecyclerView(int recyclerViewId, String category) {
        RecyclerView recyclerView = findViewById(recyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        WorkerAdapter adapter = new WorkerAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadWorkers(category, adapter);
    }

    private void loadWorkers(String category, WorkerAdapter adapter) {
        Log.d(TAG, "Loading workers for category: " + category);
        db.collection("workers")
                .whereEqualTo("category", category)
                .whereEqualTo("available", true)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for category " + category, error);
                        return;
                    }

                    if (value == null) {
                        Log.d(TAG, "No workers found for category: " + category);
                        return;
                    }

                    List<Worker> workers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Worker worker = doc.toObject(Worker.class);
                        worker.setId(doc.getId());
                        workers.add(worker);
                        Log.d(TAG, "Loaded worker: " + worker.getName() +
                                " | Available: " + worker.isAvailable());
                    }
                    adapter.updateWorkers(workers);
                });
    }

    @Override
    public void onWorkerClick(Worker worker) {
        playClickSound();
        Log.d(TAG, "Worker clicked: " + worker.getName());
        Intent intent = new Intent(this, WorkerProfileActivity.class);
        intent.putExtra("worker", worker);
        startActivity(intent);
    }

    private void playClickSound() {
        if (clickSound != null) {
            clickSound.start();
            clickSound.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickSound != null) {
            clickSound.release();
            clickSound = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}