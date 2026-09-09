package com.example.quickhire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quickhire.adapters.WorkerAdapter;
import com.example.quickhire.databinding.FragmentFirstBinding;
import com.example.quickhire.models.Worker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private WorkerAdapter workerAdapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        workerAdapter = new WorkerAdapter(requireContext(), new ArrayList<>(), worker -> {
            // Handle worker click
            // Intent could be started here if needed
        });

        binding.workersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workersRecyclerView.setAdapter(workerAdapter);

        loadWorkers();
    }

    private void loadWorkers() {
        db.collection("workers")
                .whereEqualTo("available", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Worker> workers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Worker worker = document.toObject(Worker.class);
                            worker.setId(document.getId());
                            workers.add(worker);
                        }
                        workerAdapter.updateWorkers(workers);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}