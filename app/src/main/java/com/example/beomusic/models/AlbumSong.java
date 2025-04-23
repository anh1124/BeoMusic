package com.example.beomusic.models;

import java.util.Date;

public class AlbumSong {
    private String id;
    private String albumId;
    private String songId;

    // Empty constructor required for Firebase
    public AlbumSong() {}

    // Constructor for adding a song to an album (favorites)
    public AlbumSong(String albumId, String songId) {
        this.albumId = albumId;
        this.songId = songId;
        this.id = generateId(albumId, songId);
    }

    // Getters
    public String getId() { return id; }
    public String getAlbumId() { return albumId; }
    public String getSongId() { return songId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setSongId(String songId) { this.songId = songId; }

    // Utility method to generate a consistent ID
    public static String generateId(String albumId, String songId) {
        return albumId + "_" + songId;
    }
    
    // Helper method to check if a song is in favorites
    public static String generateFavoriteEntryId(String userId, String songId) {
        String favoritesAlbumId = Album.generateFavoritesAlbumId(userId);
        return generateId(favoritesAlbumId, songId);
    }
    
    // Helper method to check if this is a favorite song entry
    public boolean isFavoriteEntry() {
        return albumId != null && albumId.endsWith("_favorites");
    }
}
