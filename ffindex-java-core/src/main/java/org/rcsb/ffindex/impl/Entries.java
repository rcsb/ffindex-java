package org.rcsb.ffindex.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * Holds information on the files in a read-only bundle.
 */
public class Entries {
    private final Map<String, Integer> indices;
    private final long[] offsets;
    private final int[] lengths;

    /**
     * Create a new Entries object.
     * @param indices map of filenames -> index position of array
     * @param offsets array of offset values
     * @param lengths array of length values
     */
    private Entries(Map<String, Integer> indices, long[] offsets, int[] lengths) {
        this.indices = indices;
        this.offsets = offsets;
        this.lengths = lengths;
    }

    /**
     * Parse a file and create an index of all content.
     * @param indexPath the file to read
     * @return an Entries object
     * @throws IOException reading failed
     */
    public static Entries of(Path indexPath) throws IOException {
        List<String> lines = Files.readAllLines(indexPath);
        int lineCount = lines.size();

        Map<String, Integer> indices = new HashMap<>();
        long[] offsets = new long[lineCount];
        int[] lengths = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            String[] split = lines.get(i).split(INDEX_ENTRY_DELIMITER);
            indices.put(split[0], i);
            offsets[i] = Long.parseLong(split[1]);
            lengths[i] = Integer.parseInt(split[2]);
        }
        return new Entries(indices, offsets, lengths);
    }

    /**
     * Retrieve the index that corresponds to the given filename.
     * @param filename the filename to resolve
     * @return the index in the other arrays, -1 if not found
     */
    public int getIndex(String filename) {
        return indices.getOrDefault(filename, -1);
    }

    /**
     * The offset of a file.
     * @param index the index of this file
     * @return the start position as long
     */
    public long getOffset(int index) {
        return offsets[index];
    }

    /**
     * The length of a file.
     * @param index the index of this file
     * @return the length as int
     */
    public int getLength(int index) {
        return lengths[index];
    }

    /**
     * The collection of all registered filenames.
     * @return a set of Strings
     */
    public Set<String> getFilenames() {
        return indices.keySet();
    }

    /**
     * The number of files present in this bundle.
     * @return an int
     */
    public int size() {
        return indices.size();
    }
}
