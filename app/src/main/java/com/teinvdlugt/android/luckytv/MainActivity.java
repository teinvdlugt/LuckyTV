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
        public int yearToLoad;
        public int pageToLoad;
        public boolean everythingLoaded;
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
            entryList.yearToLoad = Calendar.getInstance().get(Calendar.YEAR);
            entryList.pageToLoad = 1;
        }
    }

    @Override
    public void loadNextYear() {
        new AsyncTask<Void, Void, List<Entry>>() {
            boolean morePagesComing;

            @Override
            protected List<Entry> doInBackground(Void... params) {
                if (entryList.everythingLoaded) return null;
                List<Entry> entries = new ArrayList<>();
                String url = HOME_URL + (entryList.yearToLoad) + "/";
                if (entryList.pageToLoad != 1)
                    url += "page/" + (entryList.pageToLoad) + "/";
                try {
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
                    morePagesComing = !doc.getElementsByClass("emm-next").isEmpty();
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
                    entryList.entries.addAll(entries);
                    adapter.notifyItemRangeInserted(adapter.getData().size() - entries.size(), entries.size());
                }

                if (morePagesComing) {
                    entryList.pageToLoad++;
                } else {
                    entryList.yearToLoad--;
                    entryList.pageToLoad = 1;
                    if (entryList.yearToLoad == 2000) {
                        // Videos start at 2001
                        adapter.setShowProgressBar(false);
                        entryList.everythingLoaded = true;
                    }
                }
            }
        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ENTRY_LIST, entryList);
    }
}
