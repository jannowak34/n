package com.example.oldtube;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/** Full screen video player. */
public class PlayerActivity extends Activity {
    public static final String EXTRA_URL = "url";

    private VideoView video;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final View loading = findViewById(R.id.player_loading);
        video = (VideoView) findViewById(R.id.video);
        video.setMediaController(new MediaController(this));
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                loading.setVisibility(View.GONE);
            }
        });
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PlayerActivity.this, R.string.error_playing, Toast.LENGTH_LONG).show();
                finish();
                return true;
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
        video.setVideoURI(Uri.parse(getIntent().getStringExtra(EXTRA_URL)));
        video.requestFocus();
        video.start();
    }

    protected void onPause() {
        super.onPause();
        if (video != null) {
            video.stopPlayback();
        }
        finish();
    }
}
