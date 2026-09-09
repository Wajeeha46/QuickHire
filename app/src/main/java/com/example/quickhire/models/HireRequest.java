package com.wajeeha.quickhire.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HireRequest {
    private String customerName;
    private String address;
    private int hours;
    private String phone;
    private String email;
    private String workerId;
    private String userId;
    private String hireId;
    private Date expiryTime;
    private String workerName;
    private String workerEmail;
    private String workerPhone;
    private String status = "active";
    @ServerTimestamp
    private Date createdAt;

    public HireRequest() {}

    public HireRequest(String customerName, String address, int hours, String phone,
                       String email, String workerId, String userId) {
        this.customerName = customerName;
        this.address = address;
        this.hours = hours;
        this.phone = phone;
        this.email = email;
        this.workerId = workerId;
        this.userId = userId;
        this.status = "active";
    }

    // Set worker details after creating the hire request
    public void setWorkerDetails(Worker worker) {
        this.workerName = worker.getName();
        this.workerEmail = worker.getEmail();
        this.workerPhone = worker.getPhone();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("customerName", customerName);
        result.put("address", address);
        result.put("hours", hours);
        result.put("phone", phone);
        result.put("email", email);
        result.put("workerId", workerId);
        result.put("userId", userId);
        result.put("hireId", hireId);
        result.put("createdAt", createdAt);
        result.put("expiryTime", expiryTime);
        result.put("workerName", workerName);
        result.put("workerEmail", workerEmail);
        result.put("workerPhone", workerPhone);
        result.put("status", status);
        return result;
    }

    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getHireId() { return hireId; }
    public void setHireId(String hireId) { this.hireId = hireId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Date expiryTime) { this.expiryTime = expiryTime; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerEmail() { return workerEmail; }
    public void setWorkerEmail(String workerEmail) { this.workerEmail = workerEmail; }
    public String getWorkerPhone() { return workerPhone; }
    public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }

    public void setStatus(String status) {
        this.status = status;
    }

}