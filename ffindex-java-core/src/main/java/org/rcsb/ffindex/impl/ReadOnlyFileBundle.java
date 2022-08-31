package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.ReadableFileBundle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;

/**
 * A bundle that supports only read operations.
 */
public class ReadOnlyFileBundle implements ReadableFileBundle {
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final Entries entries;

    public ReadOnlyFileBundle(RandomAccessFile dataFile, FileChannel dataFileChannel, Entries entries) {
        this.dataFile = dataFile;
        this.dataFileChannel = dataFileChannel;
        this.entries = entries;
    }

    @Override
    public ByteBuffer readFile(String filename) throws IOException {
        int index = entries.getIndex(filename);
        if (index == -1) {
            throw new NoSuchFileException("No file with name '" + filename + "'");
        }
        return dataFileChannel.map(FileChannel.MapMode.READ_ONLY, entries.getOffset(index), entries.getLength(index) - FILE_END_LENGTH);
    }

    @Override
    public boolean containsFile(String filename) {
        return entries.getIndex(filename) != -1;
    }

    @Override
    public int fileCount() {
        return entries.size();
    }

    @Override
    public Stream<String> filenames() {
        return entries.getFilenames().stream();
    }

    @Override
    public void close() throws IOException {
        dataFileChannel.close();
        dataFile.close();
    }
}
