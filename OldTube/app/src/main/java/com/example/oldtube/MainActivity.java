package com.example.oldtube;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Home screen: logo bar, three tabs (Featured, Popular, Search) and the video list. */
public class MainActivity extends Activity {
    private static final int TAB_FEATURED = 0;
    private static final int TAB_POPULAR = 1;
    private static final int TAB_SEARCH = 2;

    private VideoAdapter adapter;
    private View searchBar;
    private View loading;
    private EditText searchText;
    private TextView empty;
    private TextView[] tabs;
    private List<Video> featured;
    private List<Video> popular;
    private List<Video> results;
    private int currentTab = -1;
    private int token = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = findViewById(R.id.loading);
        empty = (TextView) findViewById(R.id.empty);
        searchBar = findViewById(R.id.search_bar);
        searchText = (EditText) findViewById(R.id.search_text);

        ListView list = (ListView) findViewById(R.id.list);
        adapter = new VideoAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Video video = (Video) parent.getItemAtPosition(position);
                DetailActivity.open(MainActivity.this, video);
            }
        });

        tabs = new TextView[] {
                (TextView) findViewById(R.id.tab_featured),
                (TextView) findViewById(R.id.tab_popular),
                (TextView) findViewById(R.id.tab_search)
        };
        for (int i = 0; i < tabs.length; i++) {
            final int tab = i;
            tabs[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectTab(tab, tab == currentTab);
                }
            });
        }

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectTab(TAB_SEARCH, false);
                searchText.requestFocus();
            }
        });
        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doSearch();
            }
        });
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doSearch();
                return true;
            }
        });

        selectTab(TAB_FEATURED, false);
    }

    private List<Video> cached(int tab) {
        if (tab == TAB_FEATURED) return featured;
        if (tab == TAB_POPULAR) return popular;
        return results;
    }

    private void store(int tab, List<Video> list) {
        if (tab == TAB_FEATURED) featured = list;
        else if (tab == TAB_POPULAR) popular = list;
        else results = list;
    }

    private void selectTab(int tab, boolean forceReload) {
        currentTab = tab;
        token++;
        loading.setVisibility(View.GONE);
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setSelected(i == tab);
        }
        searchBar.setVisibility(tab == TAB_SEARCH ? View.VISIBLE : View.GONE);

        List<Video> data = cached(tab);
        if (data != null && !forceReload) {
            show(data);
        } else if (tab == TAB_SEARCH) {
            showMessage(getString(R.string.type_search));
        } else {
            load(tab, null);
        }
    }

    private void doSearch() {
        String query = searchText.getText().toString().trim();
        if (query.length() == 0) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        results = null;
        load(TAB_SEARCH, query);
    }

    private void load(final int tab, final String query) {
        token++;
        final int myToken = token;
        empty.setVisibility(View.GONE);
        adapter.setVideos(new ArrayList<Video>());
        loading.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, Object>() {
            protected Object doInBackground(Void... params) {
                try {
                    if (tab == TAB_FEATURED) return Api.trending();
                    if (tab == TAB_POPULAR) return Api.popular();
                    return Api.search(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @SuppressWarnings("unchecked")
            protected void onPostExecute(Object result) {
                if (result instanceof Exception) {
                    if (myToken == token) {
                        showMessage(getString(R.string.error_loading) + "\n" + ((Exception) result).getMessage());
                    }
                    return;
                }
                List<Video> list = (List<Video>) result;
                store(tab, list);
                if (myToken == token) {
                    show(list);
                }
            }
        }.execute();
    }

    private void show(List<Video> list) {
        loading.setVisibility(View.GONE);
        if (list.size() == 0) {
            showMessage(getString(R.string.no_videos));
            return;
        }
        empty.setVisibility(View.GONE);
        adapter.setVideos(list);
    }

    private void showMessage(String message) {
        loading.setVisibility(View.GONE);
        adapter.setVideos(new ArrayList<Video>());
        empty.setText(message);
        empty.setVisibility(View.VISIBLE);
    }
}
