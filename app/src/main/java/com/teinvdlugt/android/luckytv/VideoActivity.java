package com.teinvdlugt.android.luckytv;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.devbrackets.android.exomedia.EMVideoView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    public static final String ENTRY_EXTRA = "entry";

    /*private SurfaceView surfaceView;
    private AspectRatioFrameLayout videoFrame;*/

    private EMVideoView videoView;
    private Entry entry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        entry = (Entry) getIntent().getSerializableExtra(ENTRY_EXTRA);
        if (entry.getVideoUrl() != null) {
            videoUrlFound();
        } else {
            startAsyncTask();
        }

        videoView = (EMVideoView) findViewById(R.id.video_view);
        assert videoView != null;
        videoView.setOnPreparedListener(this);
        videoView.setDefaultControlsEnabled(true);
    }

    private void startAsyncTask() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String url = entry.getUrl();

                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements videoPlayers = doc.getElementsByClass("videoplayer");
                    for (Element videoPlayer : videoPlayers) {
                        Element a = videoPlayer.getElementsByTag("a").first();
                        if (a != null) {
                            return a.attr("href");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    entry.setVideoUrl(result);
                    videoUrlFound();
                }
            }
        }.execute();
    }

    private void videoUrlFound() {
        videoView.setVideoPath(entry.getVideoUrl());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
    }
}
