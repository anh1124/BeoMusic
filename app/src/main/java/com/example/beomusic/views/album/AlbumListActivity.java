package com.example.beomusic.views.album;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beomusic.R;
import com.example.beomusic.adapters.AlbumAdapter;
import com.example.beomusic.models.Album;
import com.example.beomusic.repositories.AlbumRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách album của người dùng.
 */
public class AlbumListActivity extends AppCompatActivity implements AlbumAdapter.AlbumClickListener {

    private RecyclerView rvAlbums;
    private TextView tvEmptyState;
    private Button btnCreateAlbum;
    private ProgressBar progressBar;

    private AlbumRepository albumRepository;
    private AlbumAdapter albumAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        // Khởi tạo repository
        albumRepository = new AlbumRepository();

        // Lấy userId từ Intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ UI
        rvAlbums = findViewById(R.id.rvAlbums);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnCreateAlbum = findViewById(R.id.btnCreateAlbum);
        progressBar = findViewById(R.id.progressBar);

        // Cấu hình RecyclerView
        rvAlbums.setLayoutManager(new GridLayoutManager(this, 2));
        albumAdapter = new AlbumAdapter(new ArrayList<>(), this);
        rvAlbums.setAdapter(albumAdapter);

        // Sự kiện nhấn tạo album mới
        btnCreateAlbum.setOnClickListener(v -> navigateToCreateAlbum());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải album khi activity được mở lại
        loadAlbums();
    }

    /**
     * Load danh sách album từ repository.
     */
    private void loadAlbums() {
        showProgress(true);

        albumRepository.getUserAlbums(userId, new AlbumRepository.AlbumsCallback() {
            @Override
            public void onSuccess(List<Album> albums) {
                showProgress(false);

                if (albums.isEmpty()) {
                    // Hiển thị trạng thái rỗng
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvAlbums.setVisibility(View.GONE);
                } else {
                    // Hiển thị danh sách album
                    tvEmptyState.setVisibility(View.GONE);
                    rvAlbums.setVisibility(View.VISIBLE);
                    albumAdapter.updateAlbums(albums);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(AlbumListActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                // Hiển thị trạng thái rỗng nếu có lỗi
                tvEmptyState.setVisibility(View.VISIBLE);
                rvAlbums.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Chuyển đến màn hình tạo album.
     */
    private void navigateToCreateAlbum() {
        Intent intent = new Intent(this, AlbumCreateActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    /**
     * Chuyển đến màn hình chi tiết album.
     * @param album Album cần xem.
     */
    private void navigateToAlbumDetail(Album album) {
        Intent intent = new Intent(this, AlbumDetailActivity.class);
        intent.putExtra("ALBUM_ID", album.getAlbumId());
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    /**
     * Hiển thị hoặc ẩn ProgressBar.
     * @param show True nếu hiển thị, False nếu ẩn.
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCreateAlbum.setEnabled(!show);
    }

    @Override
    public void onAlbumClick(Album album) {
        navigateToAlbumDetail(album);
    }
}
