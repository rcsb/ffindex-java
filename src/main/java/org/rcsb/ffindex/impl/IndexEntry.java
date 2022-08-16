package org.rcsb.ffindex.impl;

class IndexEntry {
    // the filename reported for this entry
    private final String name;
    // the start position
    private final long offset;
    // the length of this file
    private final int length;

    public IndexEntry(String name, long offset, int length) {
        this.name = name;
        this.offset = offset;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}
