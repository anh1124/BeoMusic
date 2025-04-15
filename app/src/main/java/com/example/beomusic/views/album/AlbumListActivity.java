package com.example.beomusic.views.album;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beomusic.BottomNavigation.BaseActivity;
import com.example.beomusic.R;

public class AlbumListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Các thiết lập bổ sung nếu có (EdgeToEdge, insets, ...)
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_album_list;
    }

    @Override
    protected int getDefaultNavigationItemId() {
        return R.id.nav_album;
    }

}