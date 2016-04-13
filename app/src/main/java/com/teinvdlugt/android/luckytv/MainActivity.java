package com.teinvdlugt.android.luckytv;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LuckyAdapter.LoadNextYearListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String ENTRY_LIST = "entry_list";

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
            public void noResults() {

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
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
