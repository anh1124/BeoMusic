package com.example.beomusic.models;

import java.util.Date;

public class AlbumSong {
    private String id; // ID duy nhất cho mối quan hệ album-song
    private String albumId;
    private String songId;
    private int order; // Thứ tự trong album
    private Date addedDate; // Ngày thêm vào album

    public AlbumSong() { } // Constructor rỗng cần thiết cho Firebase

    // Constructor cơ bản
    public AlbumSong(String albumId, String songId, int order) {
        this.albumId = albumId;
        this.songId = songId;
        this.order = order;
        this.addedDate = new Date();
    }
    public AlbumSong(String id, String albumId, String songId, Date addedDate) {
        this.id = id;
        this.albumId = albumId;
        this.songId = songId;
        this.order = 0; // Đặt giá trị mặc định hoặc truyền từ bên ngoài
        this.addedDate = addedDate;
    }

    // Constructor đầy đủ
    public AlbumSong(String id, String albumId, String songId, int order, Date addedDate) {
        this.id = id;
        this.albumId = albumId;
        this.songId = songId;
        this.order = order;
        this.addedDate = addedDate;
    }

    // Getters
    public String getId() { return id; }
    public String getAlbumId() { return albumId; }
    public String getSongId() { return songId; }
    public int getOrder() { return order; }
    public Date getAddedDate() { return addedDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setSongId(String songId) { this.songId = songId; }
    public void setOrder(int order) { this.order = order; }
    public void setAddedDate(Date addedDate) { this.addedDate = addedDate; }

    // Phương thức tiện ích để tạo ID duy nhất từ albumId và songId
    public static String generateId(String albumId, String songId) {
        return albumId + "_" + songId;
    }
}
