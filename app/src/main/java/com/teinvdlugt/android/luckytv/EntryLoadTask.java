package com.teinvdlugt.android.luckytv;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class EntryLoadTask extends AsyncTask<Void, Void, List<Entry>> {
    public static final String HOME_URL = "http://www.luckymedia.nl/luckytv/";

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

                Set<String> classNames = post.classNames();
                for (Iterator<String> it = classNames.iterator(); it.hasNext(); )
                    if (!it.next().startsWith("tag-"))
                        it.remove();
                for (String tag : classNames) {
                    entry.addTag(tag.substring(4, tag.length()));
                }

                entries.add(entry);
            }
            morePagesComing = !doc.getElementsByClass("emm-next").isEmpty();
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
