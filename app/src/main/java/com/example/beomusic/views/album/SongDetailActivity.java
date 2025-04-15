package com.example.beomusic.views.album;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.beomusic.R;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SongDetailActivity extends AppCompatActivity {

    private static final String TAG = "SongDetailActivity";

    // UI elements
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnBack;
    private ImageView ivAlbumArt;

    // Media components
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeekBar;
    private boolean isPlaying = false;

    // Song data passed from intent
    private String title, artist, previewUrl, thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        setupInsets();             // Adjust padding for system bars (status bar, navigation bar)
        getIntentData();           // Retrieve data passed through intent
        initializeViews();         // Initialize UI components
        initializeMediaPlayer();   // Set up MediaPlayer with song info
        setListeners();            // Set event listeners
    }

    private void setupInsets() {
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            // Apply system bar insets to padding
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        artist = intent.getStringExtra("artist");
        previewUrl = intent.getStringExtra("preview_url");
        thumbnail = intent.getStringExtra("thumbnail_url");
    }

    private void initializeViews() {
        // Link views from layout
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);

        handler = new Handler(Looper.getMainLooper());

        // Load album artwork using Glide
        if (thumbnail != null && !thumbnail.isEmpty()) {
            Glide.with(this)
                    .load(thumbnail)
                    .into(ivAlbumArt);
        } else {
            ivAlbumArt.setImageResource(R.drawable.ic_song_placeholder); // Use placeholder if thumbnail is empty
        }

        // Display title and artist
        tvSongTitle.setText(title != null ? title : "Unknown Title");
        tvArtistName.setText(artist != null ? artist : "Unknown Artist");
        tvTotalTime.setText("00:00");
        tvCurrentTime.setText("00:00");
        btnPlayPause.setEnabled(false); // Disable play button until song is ready

        // Back button returns to previous screen
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        // Set audio attributes for proper playback classification
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            if (previewUrl == null || previewUrl.isEmpty()) {
                throw new IOException("Song URL is missing.");
            }

            mediaPlayer.setDataSource(previewUrl);         // Set the audio source URL
            Toast.makeText(this, "Loading song...", Toast.LENGTH_SHORT).show();
            mediaPlayer.prepareAsync();                    // Prepare the MediaPlayer asynchronously

            // When MediaPlayer is ready to play
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();           // Total song duration in ms
                seekBar.setMax(duration);                  // Set SeekBar max to song duration
                tvTotalTime.setText(formatDuration(duration / 1000));
                btnPlayPause.setEnabled(true);             // Enable play button
                Toast.makeText(this, "Ready to play", Toast.LENGTH_SHORT).show();
            });

            // Reset player when song ends
            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());

            // Handle MediaPlayer errors
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", extra: " + extra);
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage());
            Toast.makeText(this, "Cannot play song", Toast.LENGTH_SHORT).show();
        }

        // Runnable to update seek bar and current time every second
        updateSeekBar = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition(); // Get current playback position
                seekBar.setProgress(pos);
                tvCurrentTime.setText(formatDuration(pos / 1000));
                handler.postDelayed(updateSeekBar, 1000);   // Repeat every second
            }
        };
    }

    private void setListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) pausePlayback();
            else startPlayback();
        });

        // Handle seek bar interaction
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress); // Seek to user-selected position
                    tvCurrentTime.setText(formatDuration(progress / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                handler.removeCallbacks(updateSeekBar); // Pause seek bar updates while dragging
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.postDelayed(updateSeekBar, 1000); // Resume updates
                }
            }
        });
    }

    private void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.start();                        // Start playback
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.postDelayed(updateSeekBar, 1000);   // Start updating seek bar
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();                        // Pause playback
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateSeekBar);     // Stop updating seek bar
        }
    }

    private void resetPlayer() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
        seekBar.setProgress(0);
        tvCurrentTime.setText("00:00");
        handler.removeCallbacks(updateSeekBar);
    }

    // Convert seconds to MM:SS format
    private String formatDuration(long seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds),
                seconds % 60);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayback(); // Pause playback when activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();   // Release resources to avoid memory leaks
            mediaPlayer = null;
        }
    }
}
