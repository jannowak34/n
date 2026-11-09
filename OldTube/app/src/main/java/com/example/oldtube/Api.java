package com.example.oldtube;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to an Invidious server. Change BASE to use another instance.
 * Plain http is used on purpose: old Android versions cannot do modern TLS.
 */
public class Api {
    public static final String BASE = "http://s60tube.io.vn";

    /** Extra data that only the video details request returns. */
    public static class Details {
        public String description = "";
        public long views;
        public long likes;
        public float rating;
        public List<Video> related = new ArrayList<Video>();
    }

    public static List<Video> trending() throws Exception {
        return parseList(getString("/api/v1/trending"));
    }

    public static List<Video> popular() throws Exception {
        return parseList(getString("/api/v1/popular"));
    }

    public static List<Video> search(String query) throws Exception {
        return parseList(getString("/api/v1/search?type=video&q=" + URLEncoder.encode(query, "UTF-8")));
    }

    public static Details details(String videoId) throws Exception {
        JSONObject o = new JSONObject(getString("/api/v1/videos/" + videoId));
        Details d = new Details();
        d.description = o.optString("description", "");
        d.views = o.optLong("viewCount", 0);
        d.likes = o.optLong("likeCount", 0);
        d.rating = (float) o.optDouble("rating", 0);
        JSONArray rec = o.optJSONArray("recommendedVideos");
        if (rec != null) {
            for (int i = 0; i < rec.length(); i++) {
                JSONObject r = rec.optJSONObject(i);
                if (r == null) continue;
                String id = r.optString("videoId", "");
                if (id.length() == 0) continue;
                d.related.add(new Video(id, r.optString("title", ""), r.optString("author", ""),
                        formatDuration(r.optInt("lengthSeconds", 0)), r.optLong("viewCount", 0), ""));
            }
        }
        return d;
    }

    public static String thumbUrl(String videoId) {
        return BASE + "/vi/" + videoId + "/mqdefault.jpg";
    }

    /** Last-resort stream address: 360p MP4 (itag 18) proxied through the server. */
    public static String fallbackStream(String videoId) {
        return BASE + "/latest_version?id=" + videoId + "&itag=18&local=true";
    }

    /**
     * Asks the server for the video's streams and picks one that old phones can play
     * (360p MP4, itag 18). Falls back to a fixed address if anything goes wrong.
     */
    public static String resolveStream(String videoId) {
        try {
            JSONObject o = new JSONObject(getString("/api/v1/videos/" + videoId + "?local=true"));
            JSONArray streams = o.optJSONArray("formatStreams");
            if (streams == null) return fallbackStream(videoId);
            String best = null;
            for (int i = 0; i < streams.length(); i++) {
                JSONObject st = streams.optJSONObject(i);
                if (st == null) continue;
                String url = st.optString("url", "");
                if (url.length() == 0) continue;
                if ("18".equals(st.optString("itag", ""))) {
                    best = url;
                    break;
                }
                if (best == null && st.optString("type", "").indexOf("mp4") >= 0) {
                    best = url;
                }
            }
            if (best == null) return fallbackStream(videoId);
            if (best.startsWith("/")) best = BASE + best;
            if (best.startsWith("https://")) best = "http://" + best.substring(8);
            return best;
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackStream(videoId);
        }
    }

    private static List<Video> parseList(String json) throws Exception {
        JSONArray arr = new JSONArray(json);
        List<Video> list = new ArrayList<Video>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            if (!"video".equals(o.optString("type", "video"))) continue;
            String id = o.optString("videoId", "");
            if (id.length() == 0) continue;
            list.add(new Video(id, o.optString("title", ""), o.optString("author", ""),
                    formatDuration(o.optInt("lengthSeconds", 0)), o.optLong("viewCount", 0),
                    o.optString("publishedText", "")));
        }
        return list;
    }

    public static String formatDuration(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        String ss = (s < 10 ? "0" : "") + s;
        if (h > 0) {
            return h + ":" + (m < 10 ? "0" : "") + m + ":" + ss;
        }
        return m + ":" + ss;
    }

    private static String getString(String path) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BASE + path).openConnection();
        try {
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(25000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) OldTube");
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new IOException("HTTP " + code);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"), 8192);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
            reader.close();
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    /** Downloads a whole file into memory (used for thumbnails). */
    public static byte[] getBytes(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(25000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) OldTube");
            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP " + conn.getResponseCode());
            }
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            in.close();
            return out.toByteArray();
        } finally {
            conn.disconnect();
        }
    }
}
