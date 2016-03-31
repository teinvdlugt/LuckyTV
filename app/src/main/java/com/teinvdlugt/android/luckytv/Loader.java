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

public class Loader extends AsyncTaskLoader<List<Entry>> {
    public static final String HOME_URL = "http://www.luckymedia.nl/luckytv/";

    public Loader(Context context) {
        super(context);
    }

    @Override
    public List<Entry> loadInBackground() {
        List<Entry> entries = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(HOME_URL + "2016/").get();
            Element div = doc.getElementById("content");
            Elements posts = div.getElementsByClass("post");
            Entry entry = new Entry();
            for (Element post : posts) {
                Element img = post.getElementsByTag("img").first();
                entry.setImageUrl(img.attr("src"));
                Element postPreview = post.getElementsByClass("post-preview").first();
                Element a = postPreview.getElementsByTag("a").first();
                entry.setUrl(a.attr("href"));
                entry.setTitle(a.text());
                Element postDate =  postPreview.getElementsByClass("post-date").first();
                entry.setDate(postDate.text());
            }
            entries.add(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
