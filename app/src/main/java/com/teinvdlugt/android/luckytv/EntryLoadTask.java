package com.teinvdlugt.android.luckytv;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryLoadTask extends AsyncTask<Void, Void, List<Entry>> {
    public static final String HOME_URL = "http://www.luckymedia.nl/luckytv/";

    private LuckyAdapter adapter;
    private EntryList entryList;

    public EntryLoadTask(LuckyAdapter adapter, EntryList entryList) {
        this.adapter = adapter;
        this.entryList = entryList;
    }

    private boolean morePagesComing;

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

}
