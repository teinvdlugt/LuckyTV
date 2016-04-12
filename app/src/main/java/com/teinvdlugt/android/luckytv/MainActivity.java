package com.teinvdlugt.android.luckytv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LuckyAdapter.LoadNextYearListener {
    private static final String ENTRY_LIST = "entry_list";

    private RecyclerView recyclerView;
    private LuckyAdapter adapter;
    private EntryList entryList;

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
        new EntryLoadTask(entryList, EntryLoadTask.HOME_URL + entryList.yearToLoad + "/") {
            @Override
            public void newEntries(int amount) {
                if (adapter.getData() == null || !adapter.getData().equals(entryList.entries)) {
                    adapter.setData(entryList.entries);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyItemRangeInserted(adapter.getData().size() - amount, amount);
                }
            }

            @Override
            public void lastPageLoaded() {
                entryList.yearToLoad--;
                entryList.pageToLoad = 1;
                if (entryList.yearToLoad == 2000) {
                    // Videos start at 2001
                    adapter.setShowProgressBar(false);
                    entryList.everythingLoaded = true;
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
