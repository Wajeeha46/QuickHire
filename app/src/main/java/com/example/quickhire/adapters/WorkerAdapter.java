package com.wajeeha.quickhire.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wajeeha.quickhire.GlideApp;
import com.wajeeha.quickhire.HireFormActivity;
import com.wajeeha.quickhire.R;
import com.wajeeha.quickhire.models.Worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.ViewHolder> {
    private static final String TAG = "WorkerAdapter";
    private List<Worker> workers;
    private final Context context;
    private final OnWorkerClickListener listener;

    public interface OnWorkerClickListener {
        void onWorkerClick(Worker worker);
    }

    public WorkerAdapter(Context context, List<Worker> workers, OnWorkerClickListener listener) {
        this.context = context;
        this.workers = filterAvailableWorkers(workers);
        this.listener = listener;
    }

    private List<Worker> filterAvailableWorkers(List<Worker> workers) {
        List<Worker> filtered = new ArrayList<>();
        if (workers == null) return filtered;

        Date now = new Date();
        for (Worker worker : workers) {
            // Use the worker's own availability logic
            if (worker.isAvailable()) {
                filtered.add(worker);
            }
        }
        return filtered;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_worker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Worker worker = workers.get(position);
        if (worker == null) {
            Log.w(TAG, "Worker object is null at position: " + position);
            return;
        }

        holder.workerName.setText(worker.getName() != null ? worker.getName() : "Unknown Worker");
        holder.workerRating.setText(String.format(Locale.getDefault(), "%.1f", worker.getRating()));
        holder.workerRate.setText(String.format(Locale.getDefault(), "%.0f PKR/hr", worker.getHourlyRate()));

        boolean isAvailable = worker.isAvailable();
        holder.availabilityDot.setVisibility(View.VISIBLE); // Always show dot
        holder.availabilityDot.setBackgroundResource(isAvailable ?
                R.drawable.available_dot : R.drawable.unavailable_dot);
        holder.hireButton.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
        holder.hireButton.setEnabled(isAvailable);

        String imageUrl = worker.getImageUrl();
        Log.d(TAG, "Loading image from URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            GlideApp.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.worker_placeholder)
                    .error(R.drawable.error_placeholder)
                    .override(200, 200)
                    .centerCrop()
                    .into(holder.workerImage);
        } else {
            holder.workerImage.setImageResource(R.drawable.worker_placeholder);
            Log.w(TAG, "Empty image URL for worker: " + worker.getName());
        }

        holder.hireButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HireFormActivity.class);
            intent.putExtra("worker", worker);
            context.startActivity(intent);
            ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkerClick(worker);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    public void updateWorkers(List<Worker> newWorkers) {
        this.workers = filterAvailableWorkers(newWorkers);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView workerImage;
        final View availabilityDot;
        final TextView workerName;
        final TextView workerRating;
        final TextView workerRate;
        final Button hireButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workerImage = itemView.findViewById(R.id.workerImage);
            availabilityDot = itemView.findViewById(R.id.availabilityDot);
            workerName = itemView.findViewById(R.id.workerName);
            workerRating = itemView.findViewById(R.id.workerRating);
            workerRate = itemView.findViewById(R.id.workerRate);
            hireButton = itemView.findViewById(R.id.hireButton);
        }
    }
}