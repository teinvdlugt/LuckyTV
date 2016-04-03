package com.teinvdlugt.android.luckytv;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuckyLoader extends AsyncTaskLoader<List<Entry>> {
    public static final String HOME_URL = "http://www.luckymedia.nl/luckytv/";

    private List<Entry> data;

    public LuckyLoader(Context context) {
        super(context);
    }

    @Override
    public List<Entry> loadInBackground() {
        List<Entry> entries = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(HOME_URL + "2016/").get();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Entry> data) {
        if (isReset()) return;
        this.data = data;
        if (isStarted())
            super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (data != null)
            deliverResult(data);
        else forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        data = null;
    }
}
