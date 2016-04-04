package com.teinvdlugt.android.luckytv;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LuckyAdapter.LoadNextYearListener {
    private static final String ENTRY_LIST = "entry_list";
    public static final String HOME_URL = "http://www.luckymedia.nl/luckytv/";

    private RecyclerView recyclerView;
    private LuckyAdapter adapter;
    private EntryList entryList;

    private static class EntryList implements Serializable {
        public List<Entry> entries;
        public int lastLoadedYear;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LuckyAdapter(this, this);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null)
            entryList = (EntryList) savedInstanceState.getSerializable(ENTRY_LIST);
        if (entryList != null && entryList.entries != null && !entryList.entries.isEmpty()) {
            adapter.setData(entryList.entries);
        } else {
            entryList = new EntryList();
            entryList.lastLoadedYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
            loadNextYear();
        }
    }

    @Override
    public void loadNextYear() {
        new AsyncTask<Void, Void, List<Entry>>() {
            @Override
            protected List<Entry> doInBackground(Void... params) {
                List<Entry> entries = new ArrayList<>();
                try {
                    String url = HOME_URL + (entryList.lastLoadedYear - 1) + "/";
                    Document doc = Jsoup.connect(url).get();
                    Element div = doc.getElementById("content");
                    Elements posts = div.getElementsByClass("post");
                    for (Element post : posts) {
                        Entry entry = new Entry();
                        Element img = post.getElementsByTag("img").first();
                        entry.setImageUrl(img.attr("src"));
                        Element postPreview = post.getElementsByClass("post-preview").first();
                        Element a = postPreview.getElementsByTag("a").first();
                        entry.setUrl(a.attr("href"));
                        entry.setTitle(a.text());
                        Element postDate = postPreview.getElementsByClass("post-date").first();
                        entry.setDate(postDate.text());
                        entries.add(entry);
                    }
                    return entries;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Entry> entries) {
                if (entries == null) return;
                if (entryList.entries == null) {
                    entryList.entries = entries;
                    adapter.setData(entries);
                } else {
                    adapter.addItems(entries);
                }

                entryList.lastLoadedYear--;
            }
        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ENTRY_LIST, entryList);
    }
}
