package com.example.oldtube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Loads thumbnails in the background and keeps a small in-memory cache. */
public class ImageLoader {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final Map<String, Bitmap> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
                    return size() > 30;
                }
            });

    /**
     * @param sample 1 = full size, 2 = half size (uses less memory)
     */
    public static void load(final String url, final ImageView view, final int sample) {
        final String key = url + "#" + sample;
        view.setTag(url);
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageBitmap(null);
        POOL.execute(new Runnable() {
            public void run() {
                try {
                    byte[] data = Api.getBytes(url);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = sample;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    if (bitmap == null) return;
                    CACHE.put(key, bitmap);
                    HANDLER.post(new Runnable() {
                        public void run() {
                            if (url.equals(view.getTag())) {
                                view.setImageBitmap(bitmap);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
