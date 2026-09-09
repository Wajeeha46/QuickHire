package com.wajeeha.quickhire;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wajeeha.quickhire.adapters.WorkerAdapter;
import com.wajeeha.quickhire.models.Worker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkersActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerClickListener {
    private static final int HIRE_REQUEST_CODE = 1001;
    private WorkerAdapter adapter;
    private FirebaseFirestore db;
    private String currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workers);

        currentCategory = getIntent().getStringExtra("category");
        setTitle(currentCategory);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.workersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkerAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadWorkers(currentCategory);
    }

    private void loadWorkers(String category) {
        db.collection("workers")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Worker> workers = new ArrayList<>();
                        Date now = new Date();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Worker worker = document.toObject(Worker.class);
                            worker.setId(document.getId());

                            // Handle missing userId - use hiredBy if available
                            String userId = document.getString("userId");
                            if (userId == null) {
                                userId = document.getString("hiredBy");
                            }
                            worker.setUserId(userId);

                            // Manual availability check
                            if (worker.getHireExpiryTime() != null &&
                                    worker.getHireExpiryTime().before(now)) {
                                worker.setAvailable(true);
                            }

                            workers.add(worker);
                        }
                        adapter.updateWorkers(workers);
                    }
                });
    }
    @Override
    public void onWorkerClick(Worker worker) {
        Intent intent = new Intent(this, WorkerProfileActivity.class);
        intent.putExtra("worker", worker);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HIRE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the worker list after successful hire
            loadWorkers(currentCategory);
        }
    }
}