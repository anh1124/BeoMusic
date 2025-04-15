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
import com.example.beomusic.models.Song;
import com.example.beomusic.views.others.CommentActivity;
import com.example.beomusic.views.others.FavouriteSongActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SongDetailActivity extends AppCompatActivity {

    private static final String TAG = "SongDetailActivity";

    // =================== UI Elements ===================
    private TextView tvSongTitle, tvArtistName, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnBack, btnComment, btnFavourite;
    private ImageButton btnNext, btnPrevious, btnShuffle, btnRepeat;
    private ImageView ivAlbumArt;

    // =================== Media Components ===================
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeekBar;
    private boolean isPlaying = false;

    // =================== Data ===================
    private String title, artist, previewUrl, thumbnail;
    private ArrayList<Song> songList;
    private int currentPosition;

    // =================== Lifecycle ===================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        setupInsets();              // (1) Áp dụng padding cho status/nav bar
        getIntentData();           // (2) Lấy dữ liệu bài hát từ Intent
        initializeViews();         // (3) Liên kết UI
        setControlListeners();     // (4) Gán sự kiện nút điều hướng/comment/yêu thích
        initializeMediaPlayer();   // (5) Cấu hình MediaPlayer
        setPlayerListeners();      // (6) Gán các sự kiện điều khiển nhạc
    }

    // =================== (1) UI Insets ===================
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

    // =================== (2) Lấy dữ liệu từ Intent ===================
    private void getIntentData() {
        Intent intent = getIntent();
        songList = (ArrayList<Song>) intent.getSerializableExtra("song_list");
        currentPosition = intent.getIntExtra("current_position", 0);

        Song currentSong = songList.get(currentPosition);
        title = currentSong.getTitle();
        artist = currentSong.getArtist();
        previewUrl = currentSong.getFilePath();
        thumbnail = currentSong.getThumbnailUrl();
    }

    // =================== (3) Liên kết UI ===================
    private void initializeViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        btnComment = findViewById(R.id.btnComment);
        btnFavourite = findViewById(R.id.btnFavourite);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);

        handler = new Handler(Looper.getMainLooper());

        // Gán ảnh album bằng Glide
        if (thumbnail != null && !thumbnail.isEmpty()) {
            Glide.with(this).load(thumbnail).into(ivAlbumArt);
        } else {
            ivAlbumArt.setImageResource(R.drawable.ic_song_placeholder);
        }

        tvSongTitle.setText(title != null ? title : "Unknown Title");
        tvArtistName.setText(artist != null ? artist : "Unknown Artist");
        tvTotalTime.setText("00:00");
        tvCurrentTime.setText("00:00");
        btnPlayPause.setEnabled(false);

        btnBack.setOnClickListener(v -> finish());
    }

    // =================== (4) Gán các nút điều hướng/comment/favourite ===================
    private void setControlListeners() {
        btnComment.setOnClickListener(v ->
                startActivity(new Intent(this, CommentActivity.class))
        );

        btnFavourite.setOnClickListener(v ->
                startActivity(new Intent(this, FavouriteSongActivity.class))
        );

        btnNext.setOnClickListener(v -> {
            if (songList != null && currentPosition < songList.size() - 1) {
                currentPosition++;
                loadSongFromList(currentPosition);
            } else {
                Toast.makeText(this, "Đây là bài cuối cùng", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (songList != null && currentPosition > 0) {
                currentPosition--;
                loadSongFromList(currentPosition);
            } else {
                Toast.makeText(this, "Đây là bài đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =================== (5) Khởi tạo MediaPlayer ===================
    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );

        try {
            if (previewUrl == null || previewUrl.isEmpty())
                throw new IOException("Song URL is missing.");

            mediaPlayer.setDataSource(previewUrl);
            Toast.makeText(this, "Loading song...", Toast.LENGTH_SHORT).show();
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                seekBar.setMax(duration);
                tvTotalTime.setText(formatDuration(duration / 1000));
                btnPlayPause.setEnabled(true);
                Toast.makeText(this, "Ready to play", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", extra: " + extra);
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                return true;
            });

            // Cập nhật SeekBar mỗi giây
            updateSeekBar = () -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int pos = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(pos);
                    tvCurrentTime.setText(formatDuration(pos / 1000));
                    handler.postDelayed(updateSeekBar, 1000);
                }
            };

        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage());
            Toast.makeText(this, "Cannot play song", Toast.LENGTH_SHORT).show();
        }
    }

    // =================== (6) Gán các sự kiện điều khiển playback ===================
    private void setPlayerListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) pausePlayback();
            else startPlayback();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatDuration(progress / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.postDelayed(updateSeekBar, 1000);
                }
            }
        });
    }

    // =================== Chuyển sang bài hát khác ===================
    private void loadSongFromList(int position) {
        if (mediaPlayer != null) mediaPlayer.reset();

        Song song = songList.get(position);
        title = song.getTitle();
        artist = song.getArtist();
        previewUrl = song.getFilePath();
        thumbnail = song.getThumbnailUrl();

        tvSongTitle.setText(title);
        tvArtistName.setText(artist);
        tvCurrentTime.setText("00:00");
        tvTotalTime.setText("00:00");
        seekBar.setProgress(0);
        btnPlayPause.setImageResource(R.drawable.ic_play);
        isPlaying = false;

        Glide.with(this).load(thumbnail).into(ivAlbumArt);
        initializeMediaPlayer();
    }

    // =================== Playback Controls ===================
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

    // =================== Utility ===================
    private String formatDuration(long seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds), seconds % 60);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayback(); // Tạm dừng khi không ở foreground
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
