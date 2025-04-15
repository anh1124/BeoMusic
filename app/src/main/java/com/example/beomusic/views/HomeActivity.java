package com.example.beomusic.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

        recyclerSongs = findViewById(R.id.recyclerSongs);
        progressBar = findViewById(R.id.progressBar);
        btnSearch = findViewById(R.id.btnSearch);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, this);
        recyclerSongs.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

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

        // Tab selection triggers different song categories
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        viewModel.searchSongs("popular");
                        break;
                    case 1:
                        viewModel.searchSongs("top");
                        break;
                    case 2:
                        viewModel.searchSongs("new release");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSearch.setOnClickListener(v -> {
            viewModel.searchSongs("Alan Walker");
        });

        viewModel.searchSongs("popular");
    }

    @Override
    public void onSongClick(Song song) {
        Toast.makeText(this, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();

        Intent newIntent = new Intent(this, SongDetailActivity.class);
        newIntent.putExtra("song_id", song.getSongId());
        newIntent.putExtra("title", song.getTitle());
        newIntent.putExtra("artist", song.getArtist());
        newIntent.putExtra("duration", song.getDuration());
        newIntent.putExtra("thumbnail_url", song.getThumbnailUrl());
        newIntent.putExtra("preview_url", song.getFilePath());
        newIntent.putExtra("genre", song.getGenre());

        startActivity(newIntent);

        // Increase play count
        song.setPlayCount(song.getPlayCount() + 1);
    }

    @Override
    public void onMoreClick(Song song, View view) {
        Toast.makeText(this, "Options for: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
