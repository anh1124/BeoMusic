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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beomusic.R;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SongDetailActivity extends AppCompatActivity {

    private static final String TAG = "SongDetailActivity";

    // UI elements
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause;

    // Media
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeekBar;
    private boolean isPlaying = false;

    // Song data from Intent
    private String title, artist, previewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        // Setup
        setupInsets();
        getIntentData();
        initializeViews();
        initializeMediaPlayer();
        setListeners();
    }

    private void setupInsets() {
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
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
    }

    private void initializeViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);

        handler = new Handler(Looper.getMainLooper());

        // Hiển thị thông tin bài hát
        tvSongTitle.setText(title != null ? title : "Unknown Title");
        tvArtistName.setText(artist != null ? artist : "Unknown Artist");
        tvTotalTime.setText("00:00");
        tvCurrentTime.setText("00:00");
        btnPlayPause.setEnabled(false); // Chờ media sẵn sàng
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            if (previewUrl == null || previewUrl.isEmpty()) {
                throw new IOException("Không có URL bài hát.");
            }

            mediaPlayer.setDataSource(previewUrl);
            Toast.makeText(this, "Đang tải bài hát...", Toast.LENGTH_SHORT).show();
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                seekBar.setMax(duration);
                tvTotalTime.setText(formatDuration(duration / 1000));
                btnPlayPause.setEnabled(true);
                Toast.makeText(this, "Sẵn sàng phát nhạc", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", extra: " + extra);
                Toast.makeText(this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi thiết lập nguồn dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Không thể phát bài hát", Toast.LENGTH_SHORT).show();
        }

        updateSeekBar = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                tvCurrentTime.setText(formatDuration(pos / 1000));
                handler.postDelayed(updateSeekBar, 1000);
            }
        };
    }

    private void setListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) pausePlayback();
            else startPlayback();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatDuration(progress / 1000));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar sb) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.postDelayed(updateSeekBar, 1000);
                }
            }
        });
    }

    private void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.postDelayed(updateSeekBar, 1000);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateSeekBar);
        }
    }

    private void resetPlayer() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
        seekBar.setProgress(0);
        tvCurrentTime.setText("00:00");
        handler.removeCallbacks(updateSeekBar);
    }

    private String formatDuration(long seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds),
                seconds % 60);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
