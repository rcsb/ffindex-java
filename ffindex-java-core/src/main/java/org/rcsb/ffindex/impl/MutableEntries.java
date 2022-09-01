package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.Entries;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * Entries of an appendable bundle that supports read and write operations.
 */
public class MutableEntries implements Entries {
    private final Map<String, Integer> indices;
    private final List<Long> offsets;
    private final List<Integer> lengths;

    /**
     * Create a new Entries object.
     * @param indices map of filenames -> index position of array
     * @param offsets array of offset values
     * @param lengths array of length values
     */
    private MutableEntries(Map<String, Integer> indices, List<Long> offsets, List<Integer> lengths) {
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
    public static MutableEntries of(Path indexPath) throws IOException {
        List<String> lines = Files.readAllLines(indexPath);
        int lineCount = lines.size();

        Map<String, Integer> indices = new HashMap<>();
        List<Long> offsets = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();
        for (int i = 0; i < lineCount; i++) {
            String[] split = lines.get(i).split(INDEX_ENTRY_DELIMITER);
            indices.put(split[0], i);
            offsets.add(Long.parseLong(split[1]));
            lengths.add(Integer.parseInt(split[2]));
        }
        return new MutableEntries(indices, offsets, lengths);
    }

    @Override
    public int getIndex(String filename) {
        return indices.getOrDefault(filename, -1);
    }

    @Override
    public long getOffset(int index) {
        return offsets.get(index);
    }

    @Override
    public int getLength(int index) {
        return lengths.get(index);
    }

    @Override
    public Set<String> getFilenames() {
        return indices.keySet();
    }

    @Override
    public int size() {
        return indices.size();
    }

    /**
     * Add a new file to this collection. Will reject any filename that is already present.
     * @param filename the filename
     * @param offset the offset of the file
     * @param length the length of the file in bytes
     */
    public void addFile(String filename, long offset, int length) throws IOException {
        if (indices.containsKey(filename)) {
            throw new FileAlreadyExistsException("File " + filename + " is already registered in bundle");
        }

        indices.put(filename, indices.size());
        offsets.add(offset);
        lengths.add(length);
    }
}
