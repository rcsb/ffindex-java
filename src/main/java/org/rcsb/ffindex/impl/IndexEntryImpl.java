package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.IndexEntry;

public class IndexEntryImpl implements IndexEntry {
    // the filename reported for this entry
    private final String name;
    // the start position
    private final long offset;
    // the length of this file
    private final int length;

    public IndexEntryImpl(String name, long offset, int length) {
        this.name = name;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public int getLength() {
        return length;
    }
}
