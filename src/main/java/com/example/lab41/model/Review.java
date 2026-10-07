package com.example.lab41.model;

import java.time.Instant;

/**
 * Embedded document: nam ben trong Product.reviews, khong co collection rieng.
 * Chi phu hop khi so luong review nho va bounded (xem docs/E06).
 */
public class Review {

    private String userId;
    private int rating;
    private String comment;
    private Instant createdAt;

    public Review() {
    }

    public Review(String userId, int rating, String comment, Instant createdAt) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
