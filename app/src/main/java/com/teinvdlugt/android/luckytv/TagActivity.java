package com.teinvdlugt.android.luckytv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TagActivity extends AppCompatActivity implements LuckyAdapter.LoadNextYearListener {
    private static final String ENTRY_LIST = "entry_list";
    public static final String OPEN_SEARCH_VIEW = "open_search_bar";
    public static final String TAG_QUERY = "tag";

    private RecyclerView recyclerView;
    private LuckyAdapter adapter;
    private EntryList entryList;
    private String url;
    private String tagExtra;
    private boolean openSearchView;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getBooleanExtra(OPEN_SEARCH_VIEW, false))
            openSearchView = true;

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LuckyAdapter(this, this);

        if (savedInstanceState != null)
            entryList = (EntryList) savedInstanceState.getSerializable(ENTRY_LIST);
        if (entryList != null && entryList.entries != null && !entryList.entries.isEmpty()) {
            adapter.setData(entryList.entries);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            entryList = new EntryList();
            recyclerView.setVisibility(View.INVISIBLE);
            tagExtra = getIntent().getStringExtra(TAG_QUERY);
            if (tagExtra != null) {
                searchNewTag(tagExtra);
            }
        }

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void loadNextYear() {
        if (url == null) return;
        new EntryLoadTask(entryList, url) {
            @Override
            public void newEntries(int amount) {
                if (adapter.getData() == null || adapter.getData().isEmpty()) {
                    adapter.setData(entryList.entries);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyItemRangeInserted(adapter.getData().size() - amount, amount);
                }
            }

            @Override
            public void noResults() {
                adapter.setShowProgressBar(false);
                adapter.setNoProgressBarText(R.string.no_results);
                adapter.notifyDataSetChanged();
                entryList.everythingLoaded = true;
            }

            @Override
            public void lastPageLoaded() {
                adapter.setShowProgressBar(false);
                entryList.everythingLoaded = true;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint(getString(R.string.tag_ellipsis));
        if (tagExtra == null) searchView.setIconified(false);
        else searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNewTag(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        if (openSearchView) searchView.requestFocus();
        return true;
    }

    private void searchNewTag(String tag) {
        url = EntryLoadTask.HOME_URL + "tag/" + tag + "/";
        entryList = new EntryList();
        adapter.clearData();
        adapter.setShowProgressBar(true);
        adapter.setNoProgressBarText(R.string.that_was_it);
        recyclerView.setVisibility(View.VISIBLE);
        setTitle(tag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ENTRY_LIST, entryList);
    }

    public static void openActivityUsingTag(Context context, String tag) {
        context.startActivity(new Intent(context, TagActivity.class)
                .putExtra(TAG_QUERY, tag));
    }
}
