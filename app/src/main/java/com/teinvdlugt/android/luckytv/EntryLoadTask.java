package com.teinvdlugt.android.luckytv;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class EntryLoadTask extends AsyncTask<Void, Void, List<Entry>> {
    public static final String HOME_URL = "http://www.luckytv.nl/afleveringen/";

    private EntryList entryList;
    private String url;

    public EntryLoadTask(EntryList entryList, String url) {
        this.entryList = entryList;
        this.url = url;
    }

    private boolean morePagesComing;

    @Override
    protected List<Entry> doInBackground(Void... params) {
        if (entryList.everythingLoaded) return null;
        List<Entry> entries = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Element itemsDiv = doc.getElementsByClass("items").first();
            Elements videos = itemsDiv.getElementsByClass("video");
            for (Element video : videos) {
                Entry entry = new Entry();
                entry.setImageUrl(video.getElementsByClass(
                        "video__thumb").attr("src")); // Use attr srcset for higher resolution
                Element metaDiv = video.getElementsByClass("video__meta").first();
                Element title = metaDiv.getElementsByClass("video__title").first();
                entry.setUrl(title.attr("href"));
                entry.setTitle(title.text());
                Element date = metaDiv.getElementsByClass("video__date").first();
                entry.setDate(date.text());

                entries.add(entry);
            }
            Element paginationDiv = doc.getElementsByClass("pagination").first();
            Elements nextButton = paginationDiv.getElementsByClass("next");
            morePagesComing = !nextButton.isEmpty();
            return entries;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            // No search/tag results
            morePagesComing = false;
            return new ArrayList<>();
        }
    }

    @Override
    protected void onPostExecute(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            noResults();
            return;
        }
        if (entryList.entries == null) {
            entryList.entries = entries;
        } else {
            entryList.entries.addAll(entries);
        }

        if (morePagesComing) {
            entryList.pageToLoad++;
            newEntries(entries.size());
        } else {
            newEntries(entries.size());
            lastPageLoaded();
        }
    }

    public abstract void newEntries(int amount);

    public abstract void noResults();

    public abstract void lastPageLoaded();
}
