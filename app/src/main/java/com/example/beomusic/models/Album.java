package com.example.beomusic.models;

import java.util.Date;

public class Album {
    private String albumId;
    private String title;
    private String description;
    private String coverImageUrl; // URL hình ảnh bìa album
    private Date createdDate;
    private Date updatedDate; // Thời gian cập nhật gần nhất
    private String userId; // ID người tạo album
    private int songCount; // Số lượng bài hát trong album

    public Album() { } // Constructor rỗng cần thiết cho Firebase

    // Constructor cơ bản
    public Album(String albumId, String title, String description, Date createdDate, String userId) {
        this.albumId = albumId;
        this.title = title;
        this.description = description;
        this.createdDate = createdDate;
        this.updatedDate = createdDate; // Ban đầu, thời gian cập nhật = thời gian tạo
        this.userId = userId;
        this.songCount = 0;
    }

    // Constructor đầy đủ
    public Album(String albumId, String title, String description, String coverImageUrl,
                Date createdDate, Date updatedDate, String userId, int songCount) {
        this.albumId = albumId;
        this.title = title;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.userId = userId;
        this.songCount = songCount;
    }

    // Getters
    public String getAlbumId() { return albumId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Date getCreatedDate() { return createdDate; }
    public Date getUpdatedDate() { return updatedDate; }
    public String getUserId() { return userId; }
    public int getSongCount() { return songCount; }

    // Setters
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setTitle(String title) { 
        this.title = title;
        this.updatedDate = new Date(); // Cập nhật thời gian khi thay đổi
    }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedDate = new Date(); // Cập nhật thời gian khi thay đổi
    }
    public void setCoverImageUrl(String coverImageUrl) { 
        this.coverImageUrl = coverImageUrl;
        this.updatedDate = new Date(); // Cập nhật thời gian khi thay đổi
    }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public void setUpdatedDate(Date updatedDate) { this.updatedDate = updatedDate; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setSongCount(int songCount) { this.songCount = songCount; }

    // Phương thức tiện ích
    public void incrementSongCount() {
        this.songCount++;
        this.updatedDate = new Date();
    }

    public void decrementSongCount() {
        if (this.songCount > 0) {
            this.songCount--;
            this.updatedDate = new Date();
        }
    }

    // Phương thức kiểm tra xem album có trống không
    public boolean isEmpty() {
        return songCount == 0;
    }
}
