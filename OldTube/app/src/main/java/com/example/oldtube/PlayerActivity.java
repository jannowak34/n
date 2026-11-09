package com.example.oldtube;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/** Full screen video player. */
public class PlayerActivity extends Activity {
    public static final String EXTRA_ID = "id";

    private VideoView video;
    private TextView loading;
    private String url = "";
    private boolean stopped = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        loading = (TextView) findViewById(R.id.player_loading);
        video = (VideoView) findViewById(R.id.video);
        video.setMediaController(new MediaController(this));
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                loading.setVisibility(View.GONE);
            }
        });
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PlayerActivity.this,
                        getString(R.string.error_playing) + " (" + what + "/" + extra + ")\n" + url,
                        Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "video/mp4");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                return true;
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });

        final String videoId = getIntent().getStringExtra(EXTRA_ID);
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                return Api.resolveStream(videoId);
            }

            protected void onPostExecute(String result) {
                if (stopped) return;
                url = result;
                video.setVideoURI(Uri.parse(url));
                video.requestFocus();
                video.start();
            }
        }.execute();
    }

    protected void onPause() {
        super.onPause();
        stopped = true;
        video.stopPlayback();
        finish();
    }
}
