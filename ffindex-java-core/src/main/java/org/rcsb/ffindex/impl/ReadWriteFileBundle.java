package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.AppendableFileBundle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;

/**
 * A bundle that supports reading and writing. Can be opened on existing files. Files added will be written to the
 * underlying files and can be read-back immediately.
 */
public class ReadWriteFileBundle implements AppendableFileBundle {
    private final Object writeLock = new Object();
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final FileChannel indexFileChannel;
    private final MutableEntries entries;
    private long offset;

    public ReadWriteFileBundle(RandomAccessFile dataFile, FileChannel dataFileChannel, FileChannel indexFileChannel, MutableEntries entries) {
        this.dataFile = dataFile;
        this.dataFileChannel = dataFileChannel;
        this.indexFileChannel = indexFileChannel;
        this.entries = entries;
        this.offset = 0;
        if (entries.size() > 0) {
            long largestOffset = -1;
            for (int i = 0; i < entries.size(); i++) {
                long o = entries.getOffset(i);
                if (o > largestOffset) {
                    largestOffset = o;
                }
            }
            this.offset = largestOffset;
        }
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
    public long writeFile(String filename, ByteBuffer byteBuffer) throws IOException {
        if (containsFile(filename)) {
            throw new IllegalStateException("File '" + filename + "' already exists - write to a new file if you want to update existing content");
        }

        int length = byteBuffer.limit() + FILE_END_LENGTH; // separated by NUL
        synchronized (writeLock) {
            writeIndexEntry(filename, length);
            writeData(byteBuffer);
            return offset;
        }
    }

    private void writeIndexEntry(String filename, int length) throws IOException {
        entries.addFile(filename, offset, length);
        String line = filename + INDEX_ENTRY_DELIMITER +
                offset + INDEX_ENTRY_DELIMITER +
                length + System.lineSeparator();
        ByteBuffer out = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
        indexFileChannel.write(out);
    }

    private void writeData(ByteBuffer byteBuffer) throws IOException {
        offset += dataFileChannel.write(byteBuffer);
        FILE_END_BUFFER.rewind();
        offset += dataFileChannel.write(FILE_END_BUFFER);
    }

    @Override
    public void close() throws IOException {
        indexFileChannel.close();
        dataFileChannel.close();
        dataFile.close();
    }
}
