package com.teinvdlugt.android.luckytv;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements LuckyAdapter.LoadNextYearListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String ENTRY_LIST = "entry_list";

    // TODO: 24-4-2016 Show number of search results

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private LuckyAdapter adapter;
    private EntryList entryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigationView);
        assert navView != null;
        navView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LuckyAdapter(this, this);
        adapter.setNoProgressBarText(R.string.that_was_it);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            entryList = (EntryList) savedInstanceState.getSerializable(ENTRY_LIST);
        }
        if (entryList != null) {
            if (entryList.entries != null && !entryList.entries.isEmpty()) {
                adapter.setData(entryList.entries);
            }
            if (entryList.searchQuery != null) {
                getSupportActionBar().setHomeAsUpIndicator(0);
            }
        } else {
            entryList = new EntryList();
        }
    }

    @Override
    public void loadNextYear() {
        String url;
        if (entryList.searchQuery == null) {
            url = EntryLoadTask.HOME_URL + "/";
            if (entryList.pageToLoad != 1)
                url += "page/" + entryList.pageToLoad + "/";
        } else {
            url = EntryLoadTask.HOME_URL;
            if (entryList.pageToLoad != 1)
                url += "page/" + entryList.pageToLoad + "/";
            url += "?q=" + entryList.searchQuery;
        }

        new EntryLoadTask(entryList, url) {
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ENTRY_LIST, entryList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                entryList = new EntryList();
                entryList.searchQuery = query;
                adapter.clearData();
                adapter.setShowProgressBar(true);
                adapter.setNoProgressBarText(R.string.that_was_it);
                searchView.clearFocus();
                getSupportActionBar().setHomeAsUpIndicator(0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (entryList.searchQuery == null)
                    drawerLayout.openDrawer(GravityCompat.START);
                else {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
                    entryList = new EntryList();
                    adapter.clearData();
                    adapter.setShowProgressBar(true);
                    adapter.setNoProgressBarText(R.string.that_was_it);
                    invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_tag:
                startActivity(new Intent(this, TagActivity.class)
                        .putExtra(TagActivity.OPEN_SEARCH_VIEW, true));
                drawerLayout.closeDrawers();
                return true;
            default:
                return false;
        }
    }
}
