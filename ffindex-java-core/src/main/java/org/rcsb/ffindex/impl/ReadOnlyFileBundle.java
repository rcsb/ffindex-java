package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.ReadableFileBundle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A bundle that supports only read operations.
 */
public class ReadOnlyFileBundle extends AbstractFileBundle implements ReadableFileBundle {
    private final ImmutableEntries entries;

    public ReadOnlyFileBundle(Path dataPath, Path indexPath) throws IOException {
        super(dataPath, indexPath, "r");
        this.entries = ImmutableEntries.of(indexPath);
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
        return entries.filenames();
    }

    @Override
    public void close() throws IOException {
        dataFileChannel.close();
        dataFile.close();
    }
}
