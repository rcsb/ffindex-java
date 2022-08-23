package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.DataFile;
import org.rcsb.ffindex.FileBundle;
import org.rcsb.ffindex.IndexEntry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A bundle that supports write operations (which can only append the original file).
 */
public class AppendableFileBundle implements FileBundle {
    private final Object writeLock = new Object();
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final FileChannel indexFileChannel;
    private final Map<String, IndexEntry> entries;
    private long offset;

    public AppendableFileBundle(RandomAccessFile dataFile, FileChannel dataFileChannel, FileChannel indexFileChannel, Map<String, IndexEntry> entries) throws IOException {
        this.dataFile = dataFile;
        this.dataFileChannel = dataFileChannel;
        this.indexFileChannel = indexFileChannel;
        this.entries = entries;

        int size = entries.size();
        if (size > 0) {
            IndexEntry lastIndex = entries.values().stream().skip(size - 1).findFirst().orElseThrow();
            this.offset = lastIndex.getOffset() + lastIndex.getLength();
            dataFileChannel.position(offset);
        }
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

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param byteBuffer the content of this file
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     */
    public long writeFile(String filename, ByteBuffer byteBuffer) throws IOException {
        if (containsFile(filename)) {
            throw new IllegalStateException("File '" + filename + "' already exists - write to a new file if you want to update existing content");
        }

        int length = byteBuffer.limit() + 2; // separated by NUL
        synchronized (writeLock) {
            IndexEntry indexEntry = new IndexEntryImpl(filename, offset, length);
            writeIndexEntry(indexEntry);
            writeData(byteBuffer);
            return offset;
        }
    }

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as String
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    public long writeFile(String filename, String content) throws IOException {
        return writeFile(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as byte array
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    public long writeFile(String filename, byte[] content) throws IOException {
        return writeFile(filename, ByteBuffer.wrap(content));
    }

    private void writeData(ByteBuffer byteBuffer) throws IOException {
        offset += dataFileChannel.write(byteBuffer);
        FILE_END_BUFFER.rewind();
        offset += dataFileChannel.write(FILE_END_BUFFER);
    }

    private void writeIndexEntry(IndexEntry indexEntry) throws IOException {
        entries.put(indexEntry.getName(), indexEntry);
        String line = indexEntry.getName() + INDEX_ENTRY_DELIMITER +
                indexEntry.getOffset() + INDEX_ENTRY_DELIMITER +
                indexEntry.getLength() + System.lineSeparator();
        ByteBuffer out = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
        indexFileChannel.write(out);
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
        indexFileChannel.close();
        dataFileChannel.close();
        dataFile.close();
    }
}
