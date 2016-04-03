package com.teinvdlugt.android.luckytv;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Entry>> {

    private RecyclerView recyclerView;
    private LuckyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LuckyAdapter(this);
        recyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {
        return new LuckyLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Entry>> loader, List<Entry> data) {
        if (data == null) {
            Snackbar.make(recyclerView, R.string.error_snackbar, Snackbar.LENGTH_LONG).show();
        } else {
            adapter.setData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Entry>> loader) { /* TODO ? */ }
}
