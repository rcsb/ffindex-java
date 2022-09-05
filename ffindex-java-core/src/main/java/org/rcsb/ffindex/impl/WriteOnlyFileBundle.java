package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.WritableFileBundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * A bundle that supports write operations. Note that write-only bundles don't track written files and don't check that
 * written files have unique names. The behavior is undefined if multiple files with the same name were registered.
 */
public class WriteOnlyFileBundle extends AbstractFileBundle implements WritableFileBundle {
    private final Object writeLock = new Object();
    private final FileChannel indexFileChannel;
    private long offset;

    public WriteOnlyFileBundle(Path dataPath, Path indexPath) throws FileNotFoundException {
        super(dataPath, indexPath, "rw");
        this.indexFileChannel = new FileOutputStream(indexPath.toFile(), true).getChannel();
        this.offset = 0;
    }

    public void writeFile(String filename, ByteBuffer byteBuffer) throws IOException {
        int length = byteBuffer.limit() + FILE_END_LENGTH; // separated by NUL
        synchronized (writeLock) {
            writeIndexEntry(filename, length);
            writeData(byteBuffer);
        }
    }

    private void writeIndexEntry(String filename, int length) throws IOException {
        String line = filename + INDEX_ENTRY_DELIMITER +
                offset + INDEX_ENTRY_DELIMITER +
                length + "\n";
        ByteBuffer out = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
        indexFileChannel.write(out);
    }

    private void writeData(ByteBuffer byteBuffer) throws IOException {
        offset += dataFileChannel.write(byteBuffer);
        FILE_END_BUFFER.rewind();
        offset += dataFileChannel.write(FILE_END_BUFFER);
    }

    @Override
    public void sortIndexFile() throws IOException {
        synchronized (writeLock) {
            sortIndexFile(indexPath);
        }
    }

    @Override
    public void close() throws IOException {
        indexFileChannel.close();
        dataFileChannel.close();
        dataFile.close();
    }
}
