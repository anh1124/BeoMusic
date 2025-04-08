package com.example.beomusic.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beomusic.R;
import com.example.beomusic.adapters.SongAdapter;
import com.example.beomusic.models.Song;
import com.example.beomusic.ViewModel.HomeViewModel;
import com.example.beomusic.views.album.SongDetailActivity;
import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private HomeViewModel viewModel;
    private SongAdapter adapter;
    private RecyclerView recyclerSongs;
    private ProgressBar progressBar;
    private ImageButton btnSearch;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo Views
        recyclerSongs = findViewById(R.id.recyclerSongs);
        progressBar = findViewById(R.id.progressBar);  // Thêm ProgressBar vào layout
        btnSearch = findViewById(R.id.btnSearch);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Thiết lập RecyclerView
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, this);
        recyclerSongs.setAdapter(adapter);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Quan sát dữ liệu từ ViewModel
        viewModel.getSongs().observe(this, songs -> {
            adapter.setSongs(songs);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Thiết lập sự kiện cho TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lấy bài hát dựa trên tab được chọn
                switch (tab.getPosition()) {
                    case 0: // Recommendation
                        viewModel.searchSongs("popular");
                        break;
                    case 1: // Popular
                        viewModel.searchSongs("top");
                        break;
                    case 2: // New
                        viewModel.searchSongs("new release");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });

        // Thiết lập sự kiện cho nút Search
        btnSearch.setOnClickListener(v -> {
            // Trong ứng dụng thực tế, bạn sẽ hiển thị dialog hoặc chuyển sang SearchActivity
            // Tạm thời tìm kiếm mặc định "Alan Walker"
            viewModel.searchSongs("Alan Walker");
        });

        // Tải dữ liệu ban đầu
        viewModel.searchSongs("popular");
    }

    @Override
    public void onSongClick(Song song) {
        // Xử lý khi người dùng nhấn vào bài hát
        // Ví dụ: Phát nhạc, chuyển sang màn hình chi tiết...
        Toast.makeText(this, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        Intent newIntent = new Intent(this, SongDetailActivity.class);
        Log.d("Song Tittle: ", song.getTitle());
        newIntent.putExtra("song_id", song.getSongId());
        newIntent.putExtra("title", song.getTitle());
        newIntent.putExtra("artist", song.getArtist());
        newIntent.putExtra("duration", song.getDuration());
        newIntent.putExtra("thumbnail_url", song.getThumbnailUrl());
        newIntent.putExtra("preview_url", song.getFilePath());
        newIntent.putExtra("genre", song.getGenre());
        startActivity(newIntent);
        // Tăng số lượt phát
        song.setPlayCount(song.getPlayCount() + 1);
    }

    @Override
    public void onMoreClick(Song song, View view) {
        // Xử lý khi người dùng nhấn vào nút more
        // Ví dụ: Hiển thị popup menu với các tùy chọn như Thêm vào playlist, Chia sẻ...
        Toast.makeText(this, "Options for: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }
}