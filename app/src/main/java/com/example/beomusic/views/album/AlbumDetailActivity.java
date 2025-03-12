package com.example.beomusic.views.album;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beomusic.R;
import com.example.beomusic.adapters.SongAdapter;
import com.example.beomusic.models.Album;
import com.example.beomusic.models.Song;
import com.example.beomusic.repositories.AlbumRepository;
import com.example.beomusic.views.album.SongDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying album details and songs
 */
public class AlbumDetailActivity extends AppCompatActivity implements SongAdapter.SongClickListener {

    private ImageView ivAlbumCover;
    private TextView tvAlbumTitle, tvAlbumDescription, tvCreatedDate, tvSongCount, tvEmptyState;
    private Button btnAddSongs;
    private RecyclerView rvSongs;
    private ProgressBar progressBar;
    
    private AlbumRepository albumRepository;
    private SongAdapter songAdapter;
    
    private String albumId;
    private String userId;
    private Album currentAlbum;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        
        // Initialize repository
        albumRepository = new AlbumRepository();
        
        // Get album ID and user ID from intent
        albumId = getIntent().getStringExtra("ALBUM_ID");
        userId = getIntent().getStringExtra("USER_ID");
        
        if (albumId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        ivAlbumCover = findViewById(R.id.ivAlbumCover);
        tvAlbumTitle = findViewById(R.id.tvAlbumTitle);
        tvAlbumDescription = findViewById(R.id.tvAlbumDescription);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvSongCount = findViewById(R.id.tvSongCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnAddSongs = findViewById(R.id.btnAddSongs);
        rvSongs = findViewById(R.id.rvSongs);
        progressBar = findViewById(R.id.progressBar);
        
        // Set up RecyclerView
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(new ArrayList<>(), this);
        rvSongs.setAdapter(songAdapter);
        
        // Set up click listener
        btnAddSongs.setOnClickListener(v -> navigateToAddSongs());
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.album_details);
        }
        
        // Load album data
        loadAlbumData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh album data when returning to this screen
        loadAlbumData();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            // Handle back button
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            // Handle edit album
            navigateToEditAlbum();
            return true;
        } else if (id == R.id.action_delete) {
            // Handle delete album
            showDeleteAlbumDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Load album data from repository
     */
    private void loadAlbumData() {
        showProgress(true);
        
        albumRepository.getAlbum(albumId, new AlbumRepository.AlbumCallback() {
            @Override
            public void onSuccess(Album album) {
                currentAlbum = album;
                updateUI();
                
                // Load songs in the album
                loadAlbumSongs();
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(AlbumDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Load songs in the album
     */
    private void loadAlbumSongs() {
        albumRepository.getAlbumSongs(albumId, new AlbumRepository.SongsCallback() {
            @Override
            public void onSuccess(List<Song> songs) {
                showProgress(false);
                
                if (songs.isEmpty()) {
                    // Show empty state
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvSongs.setVisibility(View.GONE);
                } else {
                    // Show songs
                    tvEmptyState.setVisibility(View.GONE);
                    rvSongs.setVisibility(View.VISIBLE);
                    songAdapter.updateSongs(songs);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(AlbumDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                
                // Show empty state
                tvEmptyState.setVisibility(View.VISIBLE);
                rvSongs.setVisibility(View.GONE);
            }
        });
    }
    
    /**
     * Update UI with album data
     */
    private void updateUI() {
        tvAlbumTitle.setText(currentAlbum.getTitle());
        
        if (currentAlbum.getDescription() != null && !currentAlbum.getDescription().isEmpty()) {
            tvAlbumDescription.setText(currentAlbum.getDescription());
            tvAlbumDescription.setVisibility(View.VISIBLE);
        } else {
            tvAlbumDescription.setVisibility(View.GONE);
        }
        
        if (currentAlbum.getCreatedDate() != null) {
            tvCreatedDate.setText(getString(R.string.created_date, 
                    dateFormat.format(currentAlbum.getCreatedDate())));
        }
        
        tvSongCount.setText(getString(R.string.song_count, currentAlbum.getSongCount()));
        
        // Load album cover image
        if (currentAlbum.getCoverImageUrl() != null && !currentAlbum.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentAlbum.getCoverImageUrl())
                    .placeholder(R.drawable.ic_album_placeholder)
                    .error(R.drawable.ic_album_placeholder)
                    .centerCrop()
                    .into(ivAlbumCover);
        } else {
            ivAlbumCover.setImageResource(R.drawable.ic_album_placeholder);
        }
        
        // Only show add songs button if the album belongs to the current user
        if (userId != null && userId.equals(currentAlbum.getUserId())) {
            btnAddSongs.setVisibility(View.VISIBLE);
        } else {
            btnAddSongs.setVisibility(View.GONE);
        }
    }
    
    /**
     * Navigate to add songs screen
     */
    private void navigateToAddSongs() {
        Intent intent = new Intent(this, AddSongsToAlbumActivity.class);
        intent.putExtra("ALBUM_ID", albumId);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
    
    /**
     * Navigate to edit album screen
     */
    private void navigateToEditAlbum() {
        Intent intent = new Intent(this, AlbumEditActivity.class);
        intent.putExtra("ALBUM_ID", albumId);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
    
    /**
     * Show dialog to confirm album deletion
     */
    private void showDeleteAlbumDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_album)
                .setMessage(R.string.delete_album_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAlbum())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    /**
     * Delete the album
     */
    private void deleteAlbum() {
        showProgress(true);
        
        albumRepository.deleteAlbum(albumId, new AlbumRepository.AlbumCallback() {
            @Override
            public void onSuccess(Album album) {
                showProgress(false);
                Toast.makeText(AlbumDetailActivity.this, R.string.album_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(AlbumDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Show or hide progress spinner
     * @param show True to show progress, false to hide
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddSongs.setEnabled(!show);
    }
    
    @Override
    public void onSongClick(Song song) {
        // Navigate to song detail screen
        Intent intent = new Intent(this, SongDetailActivity.class);
        intent.putExtra("SONG_ID", song.getSongId());
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
    
    @Override
    public void onSongOptionsClick(Song song, View view) {
        // Show options menu for song
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_album_song);
        
        // Only show remove option if the album belongs to the current user
        if (userId == null || !userId.equals(currentAlbum.getUserId())) {
            popupMenu.getMenu().findItem(R.id.action_remove_from_album).setVisible(false);
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.action_remove_from_album) {
                // Remove song from album
                removeSongFromAlbum(song.getSongId());
                return true;
            }
            
            return false;
        });
        
        popupMenu.show();
    }
    
    /**
     * Remove a song from the album
     * @param songId Song ID to remove
     */
    private void removeSongFromAlbum(String songId) {
        showProgress(true);
        
        albumRepository.removeSongFromAlbum(albumId, songId, new AlbumRepository.AlbumCallback() {
            @Override
            public void onSuccess(Album album) {
                // Refresh album data
                currentAlbum = album;
                updateUI();
                loadAlbumSongs();
                
                Toast.makeText(AlbumDetailActivity.this, R.string.song_removed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(AlbumDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Popup menu for song options
     */
    private static class PopupMenu extends android.widget.PopupMenu {
        public PopupMenu(android.content.Context context, android.view.View anchor) {
            super(context, anchor);
        }
    }
}
