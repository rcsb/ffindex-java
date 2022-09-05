package org.rcsb.ffindex;

import java.util.stream.Stream;

/**
 * Holds all known files of a particular bundle.
 */
public interface Entries {
    /**
     * Retrieve the index that corresponds to the given filename.
     * @param filename the filename to resolve
     * @return the index in the other arrays, -1 if not found
     */
    int getIndex(String filename);

    /**
     * The offset of a file.
     * @param index the index of this file
     * @return the start position as long
     */
    long getOffset(int index);

    /**
     * The length of a file.
     * @param index the index of this file
     * @return the length as int
     */
    int getLength(int index);

    /**
     * The collection of all registered filenames.
     * @return a set of Strings
     */
    Stream<String> filenames();

    /**
     * The number of files present in this bundle.
     * @return an int
     */
    int size();
}
