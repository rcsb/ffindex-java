package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.WritableFileBundle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * A bundle that supports write operations (which can only append the original file).
 */
public class WriteOnlyFileBundle implements WritableFileBundle {
    private final Object writeLock = new Object();
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final FileChannel indexFileChannel;
    private long offset;

    public WriteOnlyFileBundle(RandomAccessFile dataFile, FileChannel dataFileChannel, FileChannel indexFileChannel) {
        this.dataFile = dataFile;
        this.dataFileChannel = dataFileChannel;
        this.indexFileChannel = indexFileChannel;
        this.offset = 0;
    }


    public long writeFile(String filename, ByteBuffer byteBuffer) throws IOException {
        int length = byteBuffer.limit() + FILE_END_LENGTH; // separated by NUL
        synchronized (writeLock) {
            writeIndexEntry(filename, length);
            writeData(byteBuffer);
            return offset;
        }
    }

    private void writeData(ByteBuffer byteBuffer) throws IOException {
        offset += dataFileChannel.write(byteBuffer);
        FILE_END_BUFFER.rewind();
        offset += dataFileChannel.write(FILE_END_BUFFER);
    }

    private void writeIndexEntry(String filename, int length) throws IOException {
        String line = filename + INDEX_ENTRY_DELIMITER +
                offset + INDEX_ENTRY_DELIMITER +
                length + System.lineSeparator();
        ByteBuffer out = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
        indexFileChannel.write(out);
    }

    @Override
    public void close() throws IOException {
        indexFileChannel.close();
        dataFileChannel.close();
        dataFile.close();
    }
}
