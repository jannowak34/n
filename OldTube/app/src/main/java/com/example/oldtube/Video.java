package com.example.oldtube;

/** One video as shown in a list row. */
public class Video {
    public final String id;
    public final String title;
    public final String uploader;
    public final String duration;
    public final long views;
    public final String published;

    public Video(String id, String title, String uploader, String duration,
                 long views, String published) {
        this.id = id;
        this.title = title;
        this.uploader = uploader;
        this.duration = duration;
        this.views = views;
        this.published = published;
    }
}
