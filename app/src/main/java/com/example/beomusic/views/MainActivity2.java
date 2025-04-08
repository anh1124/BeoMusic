package com.example.beomusic.views;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beomusic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "BeoMusic";
    private static final String DEEZER_SEARCH_URL = "https://api.deezer.com/search?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Test Deezer API
        searchDeezerTrack("Alan Walker");
    }

    private void searchDeezerTrack(String query) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Bắt đầu tìm kiếm: " + query);
                URL url = new URL(DEEZER_SEARCH_URL + query.replace(" ", "%20"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseDeezerResponse(response.toString());
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi gọi API Deezer: " + e.getMessage());
            }
        }).start();
    }

    private void parseDeezerResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            if (dataArray.length() > 0) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject track = dataArray.getJSONObject(i);
                    String title = track.getString("title");
                    String previewUrl = track.getString("preview");
                    int duration = track.getInt("duration");

                    JSONObject artist = track.getJSONObject("artist");
                    String artistName = artist.getString("name");

                    JSONObject album = track.getJSONObject("album");
                    String thumbnailUrl = album.getString("cover_medium");  // Hoac "cover_big"

                    Log.d(TAG, "Bai hat: " + title);
                    Log.d(TAG, "Nghe si: " + artistName);
                    Log.d(TAG, "Thoi luong: " + duration + " giay");
                    Log.d(TAG, "Preview URL: " + previewUrl);
                    Log.d(TAG, "Thumbnail URL: " + thumbnailUrl);
                }
            } else {
                Log.d(TAG, "Khong tim thay bai hat nao.");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Loi khi parse JSON: " + e.getMessage());
        }
    }

}
