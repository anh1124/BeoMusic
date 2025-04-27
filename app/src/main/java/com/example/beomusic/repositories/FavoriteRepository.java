package com.example.beomusic.repositories;

import com.example.beomusic.models.Album;
import com.example.beomusic.models.AlbumSong;
import com.example.beomusic.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

/**
 * Repository class for handling user favorites operations
 */
public class FavoriteRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String errorMessage);
    }

    public interface FavoritesCallback {
        void onSuccess(List<Song> favoriteSongs);
        void onError(String errorMessage);
    }

    public FavoriteRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Add a song to user's favorites
     * @param song Song to add to favorites
     * @param callback Callback for result
     */
    public void addToFavorites(Song song, FavoriteCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Bạn cần đăng nhập để thêm bài hát vào danh sách yêu thích");
            return;
        }

        String userId = currentUser.getUid();
        String favoriteAlbumId = Album.generateFavoritesAlbumId(userId);

        // Check if favorites album exists, create if not
        firestore.collection("albums").document(favoriteAlbumId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create favorites album
                        Album favoritesAlbum = new Album(userId);
                        firestore.collection("albums").document(favoriteAlbumId)
                                .set(favoritesAlbum)
                                .addOnSuccessListener(aVoid -> addSongToFavorites(userId, favoriteAlbumId, song, callback))
                                .addOnFailureListener(e -> callback.onError("Lỗi khi tạo album yêu thích: " + e.getMessage()));
                    } else {
                        // Album already exists, add song
                        addSongToFavorites(userId, favoriteAlbumId, song, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi khi kiểm tra album yêu thích: " + e.getMessage()));
    }

    /**
     * Remove a song from user's favorites
     * @param song Song to remove from favorites
     * @param callback Callback for result
     */
    public void removeFromFavorites(Song song, FavoriteCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Bạn cần đăng nhập để xóa bài hát khỏi danh sách yêu thích");
            return;
        }

        String userId = currentUser.getUid();
        String favoriteAlbumId = Album.generateFavoritesAlbumId(userId);
        String favoriteEntryId = AlbumSong.generateFavoriteEntryId(userId, song.getSongId());

        firestore.collection("album_songs").document(favoriteEntryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Decrement song count in album document
                    firestore.collection("albums").document(favoriteAlbumId)
                            .update("songCount", FieldValue.increment(-1))
                            .addOnSuccessListener(v -> callback.onSuccess(false))
                            .addOnFailureListener(e -> 
                                callback.onError("Lỗi khi cập nhật số lượng bài hát: " + e.getMessage())
                            );
                })
                .addOnFailureListener(e -> callback.onError("Lỗi khi xóa bài hát khỏi danh sách yêu thích: " + e.getMessage()));
    }

    /**
     * Check if a song is in user's favorites
     * @param song Song to check
     * @param callback Callback for result
     */
    public void isFavorite(Song song, FavoriteCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onSuccess(false);
            return;
        }

        String userId = currentUser.getUid();
        String favoriteEntryId = AlbumSong.generateFavoriteEntryId(userId, song.getSongId());

        firestore.collection("album_songs").document(favoriteEntryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot.exists()))
                .addOnFailureListener(e -> callback.onError("Lỗi khi kiểm tra bài hát yêu thích: " + e.getMessage()));
    }

    /**
     * Get all songs in user's favorites
     * @param callback Callback for result
     */
    public void getFavoriteSongs(FavoritesCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Bạn cần đăng nhập để xem danh sách yêu thích");
            return;
        }

        String userId = currentUser.getUid();
        String favoriteAlbumId = Album.generateFavoritesAlbumId(userId);
        
        Log.d("FavoriteRepository", "Getting favorites for user: " + userId + ", album: " + favoriteAlbumId);

        // Use a simple query without ordering to avoid index requirement
        firestore.collection("album_songs")
                .whereEqualTo("albumId", favoriteAlbumId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FavoriteRepository", "No favorite songs found for user");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> songIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        AlbumSong albumSong = doc.toObject(AlbumSong.class);
                        if (albumSong != null) {
                            songIds.add(albumSong.getSongId());
                        }
                    }
                    
                    Log.d("FavoriteRepository", "Found " + songIds.size() + " favorite entries");

                    // Try to get songs from songs collection first
                    getSongsByIds(songIds, new FavoritesCallback() {
                        @Override
                        public void onSuccess(List<Song> songs) {
                            if (!songs.isEmpty()) {
                                Log.d("FavoriteRepository", "Successfully retrieved " + songs.size() + " songs from songs collection");
                                callback.onSuccess(songs);
                            } else {
                                Log.d("FavoriteRepository", "No songs found in songs collection, checking individual documents");
                                // If no songs were found, try to fetch each song individually from album_songs
                                fetchIndividualSongs(songIds, callback);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("FavoriteRepository", "Error in batch retrieval: " + errorMessage);
                            // Try individual fetch as fallback
                            fetchIndividualSongs(songIds, callback);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FavoriteRepository", "Error querying album_songs: " + e.getMessage());
                    callback.onError("Lỗi khi lấy danh sách bài hát yêu thích: " + e.getMessage());
                });
    }

    // Fetch songs one by one as a fallback method
    private void fetchIndividualSongs(List<String> songIds, FavoritesCallback callback) {
        Log.d("FavoriteRepository", "Fetching songs individually");
        List<Song> result = new ArrayList<>();
        final int[] processed = {0};
        
        if (songIds.isEmpty()) {
            Log.d("FavoriteRepository", "No song IDs to fetch individually");
            callback.onSuccess(result);
            return;
        }
        
        for (String songId : songIds) {
            firestore.collection("songs").document(songId)
                    .get()
                    .addOnCompleteListener(task -> {
                        processed[0]++;
                        
                        if (task.isSuccessful() && task.getResult() != null) {
                            Song song = task.getResult().toObject(Song.class);
                            if (song != null) {
                                Log.d("FavoriteRepository", "Individually found song: " + song.getTitle());
                                result.add(song);
                            } else {
                                Log.w("FavoriteRepository", "No song found for ID: " + songId);
                            }
                        } else {
                            Log.e("FavoriteRepository", "Error fetching song: " + songId);
                        }
                        
                        // Check if all songs have been processed
                        if (processed[0] >= songIds.size()) {
                            Log.d("FavoriteRepository", "Individual fetching complete, found " + result.size() + " songs");
                            callback.onSuccess(result);
                        }
                    });
        }
    }

    /**
     * Toggle favorite status (add or remove)
     * @param song Song to toggle
     * @param callback Callback for result
     */
    public void toggleFavorite(Song song, FavoriteCallback callback) {
        isFavorite(song, new FavoriteCallback() {
            @Override
            public void onSuccess(boolean isFavorite) {
                if (isFavorite) {
                    removeFromFavorites(song, callback);
                } else {
                    addToFavorites(song, callback);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    // Private helper methods
    private void addSongToFavorites(String userId, String favoriteAlbumId, Song song, FavoriteCallback callback) {
        String favoriteEntryId = AlbumSong.generateFavoriteEntryId(userId, song.getSongId());
        
        // First, ensure the song exists in the songs collection
        firestore.collection("songs").document(song.getSongId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Song doesn't exist in Firestore, add it
                        firestore.collection("songs").document(song.getSongId())
                                .set(song)
                                .addOnSuccessListener(aVoid -> {
                                    // After song is added, create album_song entry
                                    createAlbumSongEntry(favoriteAlbumId, favoriteEntryId, song, callback);
                                })
                                .addOnFailureListener(e -> 
                                    callback.onError("Lỗi khi lưu bài hát: " + e.getMessage())
                                );
                    } else {
                        // Song already exists, just create album_song entry
                        createAlbumSongEntry(favoriteAlbumId, favoriteEntryId, song, callback);
                    }
                })
                .addOnFailureListener(e -> 
                    callback.onError("Lỗi khi kiểm tra bài hát: " + e.getMessage())
                );
    }

    private void createAlbumSongEntry(String favoriteAlbumId, String favoriteEntryId, Song song, FavoriteCallback callback) {
        AlbumSong favoriteEntry = new AlbumSong(favoriteAlbumId, song.getSongId());
        
        firestore.collection("album_songs").document(favoriteEntryId)
                .set(favoriteEntry)
                .addOnSuccessListener(aVoid -> {
                    // Increment song count in album document
                    firestore.collection("albums").document(favoriteAlbumId)
                            .update("songCount", FieldValue.increment(1))
                            .addOnSuccessListener(v -> callback.onSuccess(true))
                            .addOnFailureListener(e -> 
                                callback.onError("Lỗi khi cập nhật số lượng bài hát: " + e.getMessage())
                            );
                })
                .addOnFailureListener(e -> callback.onError("Lỗi khi thêm bài hát vào danh sách yêu thích: " + e.getMessage()));
    }

    private void getSongsByIds(List<String> songIds, FavoritesCallback callback) {
        if (songIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        
        Log.d("FavoriteRepository", "Fetching details for " + songIds.size() + " songs: " + songIds);
        
        List<Song> allSongs = new ArrayList<>();
        List<List<String>> batches = new ArrayList<>();
        
        // Split song IDs into batches of 10 (Firestore limitation)
        for (int i = 0; i < songIds.size(); i += 10) {
            batches.add(songIds.subList(i, Math.min(i + 10, songIds.size())));
        }
        
        Log.d("FavoriteRepository", "Split into " + batches.size() + " batches");
        
        final int[] completedBatches = {0};
        final boolean[] hasError = {false};
        
        for (List<String> batch : batches) {
            Log.d("FavoriteRepository", "Processing batch: " + batch);
            
            firestore.collection("songs")
                    .whereIn("songId", batch)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("FavoriteRepository", "Batch query returned " + queryDocumentSnapshots.size() + " documents");
                        
                        if (!hasError[0]) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Song song = doc.toObject(Song.class);
                                if (song != null) {
                                    Log.d("FavoriteRepository", "Found song: " + song.getTitle());
                                    allSongs.add(song);
                                } else {
                                    Log.w("FavoriteRepository", "Document exists but couldn't convert to Song: " + doc.getId());
                                }
                            }
                            
                            completedBatches[0]++;
                            Log.d("FavoriteRepository", "Completed " + completedBatches[0] + " of " + batches.size() + " batches");
                            
                            if (completedBatches[0] == batches.size()) {
                                Log.d("FavoriteRepository", "All batches complete, returning " + allSongs.size() + " songs");
                                callback.onSuccess(allSongs);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FavoriteRepository", "Error querying batch: " + e.getMessage());
                        if (!hasError[0]) {
                            hasError[0] = true;
                            callback.onError("Lỗi khi lấy thông tin bài hát: " + e.getMessage());
                        }
                    });
        }
    }
} 