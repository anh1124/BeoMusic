package com.example.beomusic.models;

import java.util.Date;

public class FavoriteSong {
    private String id;
    private String userId;
    private String songId;
    private Date addedDate; // Ngày thêm vào danh sách yêu thích

    public FavoriteSong() { } // Constructor rỗng cần thiết cho Firebase

    // Constructor cơ bản
    public FavoriteSong(String userId, String songId) {
        this.userId = userId;
        this.songId = songId;
        this.addedDate = new Date();
        this.id = generateId(userId, songId);
    }

    // Constructor đầy đủ
    public FavoriteSong(String id, String userId, String songId, Date addedDate) {
        this.id = id;
        this.userId = userId;
        this.songId = songId;
        this.addedDate = addedDate;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getSongId() { return songId; }
    public Date getAddedDate() { return addedDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setSongId(String songId) { this.songId = songId; }
    public void setAddedDate(Date addedDate) { this.addedDate = addedDate; }

    // Phương thức tiện ích để tạo ID duy nhất từ userId và songId
    public static String generateId(String userId, String songId) {
        return userId + "_" + songId;
    }
}
