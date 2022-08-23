package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.DataFile;
import org.rcsb.ffindex.FileBundle;
import org.rcsb.ffindex.IndexEntry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A bundle that supports only read operations.
 */
public class ReadOnlyFileBundle implements FileBundle {
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final Map<String, IndexEntry> entries;

    public ReadOnlyFileBundle(RandomAccessFile dataFile, FileChannel dataFileChannel, Map<String, IndexEntry> entries) {
        this.dataFile = dataFile;
        this.dataFileChannel = dataFileChannel;
        this.entries = entries;
    }

    @Override
    public DataFile readFile(String filename) throws IOException {
        if (!containsFile(filename)) {
            throw new NoSuchFileException("No file with name '" + filename + "'");
        }

        IndexEntry indexEntry = entries.get(filename);
        return new ByteBufferDataFile(dataFileChannel.map(FileChannel.MapMode.READ_ONLY, indexEntry.getOffset(), indexEntry.getLength() - 2));
    }

    @Override
    public boolean containsFile(String filename) {
        return entries.containsKey(filename);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Stream<String> filenames() {
        return entries.keySet().stream();
    }

    @Override
    public void close() throws IOException {
        dataFileChannel.close();
        dataFile.close();
    }
}
