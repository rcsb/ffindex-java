package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.Entries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * Holds information on the files in a read-only bundle.
 */
public class ImmutableEntries implements Entries {
    private final String[] filenames;
    private final long[] offsets;
    private final int[] lengths;

    /**
     * Create a new Entries object.
     * @param filenames array of filenames, must be sorted
     * @param offsets array of offset values
     * @param lengths array of length values
     */
    private ImmutableEntries(String[] filenames, long[] offsets, int[] lengths) {
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

        String[] filenames = new String[lineCount];
        long[] offsets = new long[lineCount];
        int[] lengths = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            String[] split = lines.get(i).split(INDEX_ENTRY_DELIMITER);
            filenames[i] = split[0];
            offsets[i] = Long.parseLong(split[1]);
            lengths[i] = Integer.parseInt(split[2]);
        }
        return new ImmutableEntries(filenames, offsets, lengths);
    }

    @Override
    public int getIndex(String filename) {
        return Arrays.binarySearch(filenames, filename);
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
        return Stream.of(filenames);
    }

    @Override
    public int size() {
        return filenames.length;
    }
}
