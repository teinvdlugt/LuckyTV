package com.teinvdlugt.android.luckytv;

import java.io.Serializable;
import java.util.List;

public class EntryList implements Serializable {
    public List<Entry> entries;
    public int yearToLoad;
    public int pageToLoad;
    public boolean everythingLoaded;
}
