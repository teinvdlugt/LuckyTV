package com.teinvdlugt.android.luckytv;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class EntryList implements Serializable {
    public List<Entry> entries;
    public int pageToLoad = 1;
    public boolean everythingLoaded;
    public String searchQuery;
}
