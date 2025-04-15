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
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.beomusic.R;
import com.example.beomusic.models.Song;
import com.example.beomusic.views.HomeActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SongDetailActivity extends AppCompatActivity {

    private static final String TAG = "SongDetailActivity";

    // UI Elements
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnBack, btnComment, btnFavourite, btnMenu;
    private ImageButton btnNext, btnPrevious, btnShuffle, btnRepeat;
    private ImageView ivAlbumArt;

    // Media Components
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;

    // State
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    // Data
    private ArrayList<Song> songList;
    private int currentPosition;

    //Intent
    private Intent newIntent;

    // === Activity Lifecycle ===
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);
        setupInsets();
        getIntentData();
        bindViews();
        setControlListeners();
        loadSongFromList(currentPosition);
    }

    // === UI Padding for System Bars ===
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

    // === Get Data from Intent ===
    private void getIntentData() {
        Intent intent = getIntent();
        songList = (ArrayList<Song>) intent.getSerializableExtra("song_list");
        currentPosition = intent.getIntExtra("current_position", 0);
    }

    // === Bind UI Views ===
    private void bindViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        btnComment = findViewById(R.id.btnComment);
        btnFavourite = findViewById(R.id.btnFavourite);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnMenu = findViewById(R.id.btnMenu);

        tvCurrentTime.setText("00:00");
        tvTotalTime.setText("00:00");
        btnPlayPause.setEnabled(false);
        btnBack.setOnClickListener(v -> finish());
    }

    // === Control Buttons Listeners ===
    private void setControlListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) pausePlayback();
            else startPlayback();
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition < songList.size() - 1) {
                currentPosition++;
                loadSongFromList(currentPosition);
            } else {
                Toast.makeText(this, "Đây là bài cuối cùng", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                loadSongFromList(currentPosition);
            } else {
                Toast.makeText(this, "Đây là bài đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setImageResource(isRepeat ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setImageResource(isShuffle ? R.drawable.ic_shuffle_on: R.drawable.ic_shuffle);
        });


        btnMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(SongDetailActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_song_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_add_to_playlist) {
                    Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_share) {
                    newIntent = new Intent(this, HomeActivity.class);
                    startActivity(newIntent);
                    return true;
                } else if (id == R.id.action_details) {

                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
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

    // === Load Song Data and Start Player ===
    private void loadSongFromList(int position) {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
        }

        Song song = songList.get(position);
        bindSongData(song);
        initializeMediaPlayer(song.getFilePath());
    }

    private void bindSongData(Song song) {
        tvSongTitle.setText(song.getTitle() != null ? song.getTitle() : "Unknown Title");
        tvArtistName.setText(song.getArtist() != null ? song.getArtist() : "Unknown Artist");
        tvCurrentTime.setText("00:00");
        tvTotalTime.setText("00:00");
        seekBar.setProgress(0);
        btnPlayPause.setImageResource(R.drawable.ic_play);
        isPlaying = false;

        if (song.getThumbnailUrl() != null && !song.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(song.getThumbnailUrl()).into(ivAlbumArt);
        } else {
            ivAlbumArt.setImageResource(R.drawable.ic_song_placeholder);
        }
    }

    private void initializeMediaPlayer(String url) {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mp.getDuration());
                tvTotalTime.setText(formatDuration(mp.getDuration() / 1000));
                btnPlayPause.setEnabled(true);

                startPlayback();
            });

            mediaPlayer.setOnCompletionListener(mp -> handleSongCompletion());

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Playback Error: " + what + ", " + extra);
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                return true;
            });

            initSeekBarUpdater();

        } catch (IOException e) {
            Log.e(TAG, "DataSource Error: " + e.getMessage());
            Toast.makeText(this, "Cannot play song", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSeekBarUpdater() {
        updateSeekBar = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                tvCurrentTime.setText(formatDuration(pos / 1000));
                handler.postDelayed(updateSeekBar, 1000);
            }
        };
    }

    private void handleSongCompletion() {
        if (isRepeat) {
            loadSongFromList(currentPosition);
        } else if (isShuffle) {
            currentPosition = new Random().nextInt(songList.size());
            loadSongFromList(currentPosition);
        } else {
            currentPosition = (currentPosition + 1) % songList.size();
            loadSongFromList(currentPosition);
        }
    }

    // === Playback ===
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

    // === Utility ===
    private String formatDuration(long seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds), seconds % 60);
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
