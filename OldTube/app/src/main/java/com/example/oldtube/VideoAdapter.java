package com.example.oldtube;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/** Adapter that shows videos as rows: thumbnail on the left, text on the right. */
public class VideoAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private List<Video> videos = new ArrayList<Video>();

    public VideoAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setVideos(List<Video> newVideos) {
        videos = newVideos;
        notifyDataSetChanged();
    }

    public int getCount() {
        return videos.size();
    }

    public Object getItem(int position) {
        return videos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.item_video, parent, false);
        }
        bind(row, videos.get(position));
        return row;
    }

    /** Fills a row created from item_video.xml with the data of one video. */
    public static void bind(View row, Video v) {
        ImageLoader.load(Api.thumbUrl(v.id), (ImageView) row.findViewById(R.id.thumb), 2);
        TextView duration = (TextView) row.findViewById(R.id.duration);
        duration.setText(v.duration);
        duration.setVisibility(v.duration.length() > 0 ? View.VISIBLE : View.GONE);
        ((TextView) row.findViewById(R.id.title)).setText(v.title);
        ((TextView) row.findViewById(R.id.uploader)).setText("by " + v.uploader);
        String meta = v.views > 0 ? formatViews(v.views) : "";
        if (v.published.length() > 0) {
            meta = meta.length() > 0 ? meta + " \u00b7 " + v.published : v.published;
        }
        ((TextView) row.findViewById(R.id.views)).setText(meta);
    }

    public static String formatViews(long views) {
        return NumberFormat.getInstance().format(views) + " views";
    }
}
