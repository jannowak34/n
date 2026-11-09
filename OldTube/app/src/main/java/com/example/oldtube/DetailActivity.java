package com.example.oldtube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

/** Video page: player area, title, rating, buttons, description and related videos. */
public class DetailActivity extends Activity {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_UPLOADER = "uploader";
    public static final String EXTRA_VIEWS = "views";

    private boolean favorite = false;

    public static void open(Context context, Video video) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, video.id);
        intent.putExtra(EXTRA_TITLE, video.title);
        intent.putExtra(EXTRA_UPLOADER, video.uploader);
        intent.putExtra(EXTRA_VIEWS, video.views);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final String videoId = getIntent().getStringExtra(EXTRA_ID);
        if (videoId == null) {
            finish();
            return;
        }
        final String title = getIntent().getStringExtra(EXTRA_TITLE);

        findViewById(R.id.btn_search).setVisibility(View.GONE);
        ImageLoader.load(Api.thumbUrl(videoId), (ImageView) findViewById(R.id.player_thumb), 1);
        findViewById(R.id.player).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_URL, Api.streamUrl(videoId));
                startActivity(intent);
            }
        });

        ((TextView) findViewById(R.id.detail_title)).setText(title);
        ((TextView) findViewById(R.id.detail_uploader)).setText("by " + getIntent().getStringExtra(EXTRA_UPLOADER));
        long views = getIntent().getLongExtra(EXTRA_VIEWS, 0);
        ((TextView) findViewById(R.id.detail_views)).setText(views > 0 ? VideoAdapter.formatViews(views) : "");

        final TextView favoriteButton = (TextView) findViewById(R.id.btn_favorite);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                favorite = !favorite;
                favoriteButton.setText(favorite ? R.string.unfavorite : R.string.favorite);
                Toast.makeText(DetailActivity.this,
                        favorite ? R.string.added_favorite : R.string.removed_favorite,
                        Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, title + " http://www.youtube.com/watch?v=" + videoId);
                startActivity(Intent.createChooser(send, getString(R.string.share)));
            }
        });

        loadDetails(videoId);
    }

    private void loadDetails(final String videoId) {
        new AsyncTask<Void, Void, Object>() {
            protected Object doInBackground(Void... params) {
                try {
                    return Api.details(videoId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
            }

            protected void onPostExecute(Object result) {
                TextView description = (TextView) findViewById(R.id.detail_description);
                if (result instanceof Exception) {
                    description.setText(getString(R.string.error_details) + "\n" + ((Exception) result).getMessage());
                    return;
                }
                showDetails((Api.Details) result);
            }
        }.execute();
    }

    private void showDetails(Api.Details d) {
        ((TextView) findViewById(R.id.detail_description)).setText(d.description);
        if (d.views > 0) {
            ((TextView) findViewById(R.id.detail_views)).setText(VideoAdapter.formatViews(d.views));
        }
        if (d.rating > 0) {
            RatingBar bar = (RatingBar) findViewById(R.id.detail_rating);
            bar.setRating(d.rating);
            bar.setVisibility(View.VISIBLE);
        }

        LinearLayout related = (LinearLayout) findViewById(R.id.related);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < d.related.size() && i < 8; i++) {
            final Video other = d.related.get(i);
            View row = inflater.inflate(R.layout.item_video, related, false);
            VideoAdapter.bind(row, other);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    open(DetailActivity.this, other);
                }
            });
            related.addView(row);
        }
    }
}
