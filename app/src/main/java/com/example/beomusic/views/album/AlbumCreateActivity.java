package com.example.beomusic.views.album;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beomusic.R;
import com.example.beomusic.models.Album;
import com.example.beomusic.repositories.AlbumRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity for creating a new album
 */
public class AlbumCreateActivity extends AppCompatActivity {

    private EditText etAlbumTitle, etAlbumDescription;
    private ImageView ivAlbumCover;
    private Button btnSelectCover, btnCreateAlbum, btnCancel;
    private ProgressBar progressBar;
    
    private AlbumRepository albumRepository;
    private String userId;
    private byte[] coverImageBytes = null;
    
    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            // Load the selected image
                            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            
                            // Display the selected image
                            ivAlbumCover.setImageBitmap(bitmap);
                            ivAlbumCover.setVisibility(View.VISIBLE);
                            
                            // Convert bitmap to byte array for upload
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            coverImageBytes = baos.toByteArray();
                            
                        } catch (IOException e) {
                            Toast.makeText(this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_create);
        
        // Initialize repository
        albumRepository = new AlbumRepository();
        
        // Get user ID from intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        etAlbumTitle = findViewById(R.id.etAlbumTitle);
        etAlbumDescription = findViewById(R.id.etAlbumDescription);
        ivAlbumCover = findViewById(R.id.ivAlbumCover);
        btnSelectCover = findViewById(R.id.btnSelectCover);
        btnCreateAlbum = findViewById(R.id.btnCreateAlbum);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
        
        // Set up click listeners
        btnSelectCover.setOnClickListener(v -> selectCoverImage());
        btnCreateAlbum.setOnClickListener(v -> createAlbum());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    /**
     * Open image picker to select album cover
     */
    private void selectCoverImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }
    
    /**
     * Create a new album with the provided information
     */
    private void createAlbum() {
        // Reset errors
        etAlbumTitle.setError(null);
        etAlbumDescription.setError(null);
        
        // Get values
        String title = etAlbumTitle.getText().toString().trim();
        String description = etAlbumDescription.getText().toString().trim();
        
        // Validate inputs
        boolean cancel = false;
        View focusView = null;
        
        if (TextUtils.isEmpty(title)) {
            etAlbumTitle.setError(getString(R.string.error_field_required));
            focusView = etAlbumTitle;
            cancel = true;
        }
        
        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the album creation
            showProgress(true);
            
            albumRepository.createAlbum(title, description, userId, coverImageBytes, new AlbumRepository.AlbumCallback() {
                @Override
                public void onSuccess(Album album) {
                    showProgress(false);
                    Toast.makeText(AlbumCreateActivity.this, R.string.album_created, Toast.LENGTH_SHORT).show();
                    
                    // Return to previous screen
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    showProgress(false);
                    Toast.makeText(AlbumCreateActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    /**
     * Show or hide progress spinner
     * @param show True to show progress, false to hide
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSelectCover.setEnabled(!show);
        btnCreateAlbum.setEnabled(!show);
        btnCancel.setEnabled(!show);
        etAlbumTitle.setEnabled(!show);
        etAlbumDescription.setEnabled(!show);
    }
}
