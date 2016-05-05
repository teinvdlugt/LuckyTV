package com.teinvdlugt.android.luckytv;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.devbrackets.android.exomedia.EMVideoView;
import com.devbrackets.android.exomedia.listener.ExoPlayerListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    public static final String ENTRY_EXTRA = "entry";
    public static final String VIDEO_POSITION_MS = "video_position";

    private EMVideoView videoView;
    private Entry entry;
    private TextView titleTextView, dateTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        videoView = (EMVideoView) findViewById(R.id.video_view);
        assert videoView != null;
        videoView.setOnPreparedListener(this);
        videoView.setDefaultControlsEnabled(true);

        videoView.addExoPlayerListener(new ExoPlayerListener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {}

            @Override
            public void onError(Exception e) {}

            @Override
            public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {
                videoView.getLayoutParams().height = (int) (1. * videoView.getWidth() / width * height);
                videoView.invalidate();
                videoView.requestLayout();
                Log.d("hi", "onVideoSizeChanged: Lol");
            }
        });

        entry = (Entry) getIntent().getSerializableExtra(ENTRY_EXTRA);

        StringBuilder tags = new StringBuilder("Tags: ");
        for (String tag : entry.getTags())
            tags.append(tag).append(", ");
        Toast.makeText(this, tags, Toast.LENGTH_SHORT).show();

        if (entry.getVideoUrl() != null) {
            videoUrlFound();
        } else {
            startAsyncTask();
        }

        displayVideoInfo();
    }

    private void displayVideoInfo() {
        if (titleTextView != null && entry.getTitle() != null)
            titleTextView.setText(entry.getTitle());
        if (dateTextView != null && entry.getDate() != null)
            dateTextView.setText(entry.getDate());
    }

    private void startAsyncTask() {
        Log.d("VideoActivity.class", "startAsyncTask: fetching");
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
    protected void onPause() {
        videoView.pause();
        super.onPause();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(VIDEO_POSITION_MS, videoView.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (videoView != null) {
            long millis = savedInstanceState.getLong(VIDEO_POSITION_MS);
            videoView.seekTo((int) millis);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void onClickShare(View view) {}
}
