package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.Entries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * Holds information on the files in a read-only bundle.
 */
public class ImmutableEntries implements Entries {
    private final Map<String, Integer> filenames;
    private final long[] offsets;
    private final int[] lengths;

    /**
     * Create a new Entries object.
     * @param filenames map of filenames to their index in other arrays
     * @param offsets array of offset values
     * @param lengths array of length values
     */
    private ImmutableEntries(Map<String, Integer> filenames, long[] offsets, int[] lengths) {
        this.filenames = filenames;
        this.offsets = offsets;
        this.lengths = lengths;
    }

    /**
     * Parse a file and create an index of all content.
     * @param indexPath the file to read
     * @return an Entries object
     * @throws IOException reading failed
     */
    public static ImmutableEntries of(Path indexPath) throws IOException {
        List<String> lines = Files.readAllLines(indexPath);
        int lineCount = lines.size();

        Map<String, Integer> filenames = new HashMap<>();
        long[] offsets = new long[lineCount];
        int[] lengths = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            String[] split = lines.get(i).split(INDEX_ENTRY_DELIMITER);
            filenames.put(split[0], i);
            offsets[i] = Long.parseLong(split[1]);
            lengths[i] = Integer.parseInt(split[2]);
        }
        return new ImmutableEntries(filenames, offsets, lengths);
    }

    @Override
    public int getIndex(String filename) {
        return filenames.getOrDefault(filename, -1);
    }

    @Override
    public long getOffset(int index) {
        return offsets[index];
    }

    @Override
    public int getLength(int index) {
        return lengths[index];
    }

    @Override
    public Stream<String> filenames() {
        return filenames.keySet().stream();
    }

    @Override
    public int size() {
        return filenames.size();
    }
}
