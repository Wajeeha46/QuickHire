package com.wajeeha.quickhire.models;

import java.io.Serializable;
import java.util.Date;

public class Worker implements Serializable {
    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private double rating;
    private double hourlyRate;
    private boolean available;
    private String hiredBy;
    private Date lastHired;
    private Date createdAt;
    private String email;
    private String phone;
    private String experience;
    private String bio;
    private String userId;
    private Date hireExpiryTime;

    public Worker() {}

    public Worker(String name, String category, String imageUrl,
                  double rating, double hourlyRate, String email,
                  String phone, String experience, String bio) {
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.hourlyRate = hourlyRate;
        this.available = true;
        this.createdAt = new Date();
        this.email = email;
        this.phone = phone;
        this.experience = experience;
        this.bio = bio;
    }

    public boolean isCurrentlyHired() {
        if (hireExpiryTime != null) {
            return new Date().before(hireExpiryTime);
        }
        return !available;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) {
        if (category != null) {
            this.category = category.substring(0, 1).toUpperCase() +
                    category.substring(1).toLowerCase();
        }
    }
    public String getImageUrl() {
        if (imageUrl == null || imageUrl.isEmpty()) return "";
        String corrected = imageUrl
                .replace("libb.co", "ibb.co")
                .replace("l.lbb.co", "i.ibb.co")
                .replace(" ", "")
                .replace("\n", "")
                .replace("\t", "")
                .trim();
        if (!corrected.startsWith("http")) corrected = "https://" + corrected;
        if (corrected.startsWith("http://")) corrected = corrected.replace("http://", "https://");
        return corrected.split("\\?")[0];
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public boolean isAvailable() {
        if (hireExpiryTime != null && hireExpiryTime.before(new Date())) {
            this.available = true;
            this.hireExpiryTime = null;
            this.hiredBy = null;
            return true;
        }
        return available;
    }
    public void setAvailable(boolean available) { this.available = available; }
    public String getHiredBy() { return hiredBy; }
    public void setHiredBy(String hiredBy) { this.hiredBy = hiredBy; }
    public Date getLastHired() { return lastHired; }
    public void setLastHired(Date lastHired) { this.lastHired = lastHired; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Date getHireExpiryTime() { return hireExpiryTime; }
    public void setHireExpiryTime(Date hireExpiryTime) { this.hireExpiryTime = hireExpiryTime; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
