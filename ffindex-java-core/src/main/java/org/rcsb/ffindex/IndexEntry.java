package org.rcsb.ffindex;

/**
 * One index entry.
 */
public interface IndexEntry {
    /**
     * The filename.
     * @return a String
     */
    String getName();

    /**
     * The offset (start position) of this file in the data file.
     * @return a long
     */
    long getOffset();

    /**
     * The length of this file.
     * @return an int
     */
    int getLength();
}
