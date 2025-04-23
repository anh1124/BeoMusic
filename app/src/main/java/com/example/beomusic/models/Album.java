package com.example.beomusic.models;

public class Album {
    private String albumId;
    private String userId;

    // Empty constructor required for Firebase
    public Album() {}

    // Constructor for creating a favorites album for a user
    public Album(String userId) {
        this.userId = userId;
        this.albumId = generateFavoritesAlbumId(userId);
    }

    // Getters
    public String getAlbumId() { return albumId; }
    public String getUserId() { return userId; }

    // Setters
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Utility method to generate a consistent album ID for a user's favorites
    public static String generateFavoritesAlbumId(String userId) {
        return userId + "_favorites";
    }
    
    // Helper method to check if an album is a favorites album
    public boolean isFavoritesAlbum() {
        return albumId != null && albumId.endsWith("_favorites");
    }
    
    // Helper method to get a user's favorites album ID
    public static String getFavoritesAlbumId(String userId) {
        return generateFavoritesAlbumId(userId);
    }
}
