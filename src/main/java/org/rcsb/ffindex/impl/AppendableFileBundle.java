package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.FileBundle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.rcsb.ffindex.impl.FormatConstants.INDEX_FILE_DELIMITER;
import static org.rcsb.ffindex.impl.FormatConstants.LINE_END_BUFFER;

public class AppendableFileBundle implements FileBundle {
    private static final Object writeLock = new Object();
    private final RandomAccessFile dataFile;
    private final FileChannel dataFileChannel;
    private final FileChannel indexFileChannel;
    private final Map<String, IndexEntry> entries;
    private long offset;

    public AppendableFileBundle(Path dataPath, Path indexPath) throws IOException {
        this.dataFile = new RandomAccessFile(dataPath.toFile(), "rw");
        // need file channel because RandomAccessFile only supports offsets of type int
        FileChannel dataFileChannel = dataFile.getChannel();
        FileChannel indexFileChannel = new FileOutputStream(indexPath.toFile(), true).getChannel();
        this.dataFileChannel = dataFileChannel;
        this.indexFileChannel = indexFileChannel;
        this.entries = parseEntryIndex(indexPath);

        int size = entries.size();
        if (size > 0) {
            IndexEntry lastIndex = entries.values().stream().skip(size - 1).findFirst().orElseThrow();
            this.offset = lastIndex.getOffset() + lastIndex.getLength();
            dataFileChannel.position(offset);
        }
    }

    private Map<String, IndexEntry> parseEntryIndex(Path indexPath) throws IOException {
        List<String> lines = Files.readAllLines(indexPath);
        Map<String, IndexEntry> out = new HashMap<>();
        for (String line : lines) {
            String[] split = line.split(INDEX_FILE_DELIMITER);
            IndexEntry entry = new IndexEntry(split[0], Long.parseLong(split[1]), Integer.parseInt(split[2]));
            out.put(entry.getName(), entry);
        }
        return out;
    }

    @Override
    public ByteBufferDataFile readFile(String filename) throws IOException {
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
    public long writeFile(String filename, ByteBuffer byteBuffer) throws IOException {
        if (containsFile(filename)) {
            throw new IllegalStateException("File '" + filename + "' already exists - write to a new file if you want to update existing content");
        }

        int length = byteBuffer.limit() + 2; // separated by NUL
        synchronized (writeLock) {
            IndexEntry indexEntry = new IndexEntry(filename, offset, length);
            writeIndexEntry(indexEntry);
            writeData(byteBuffer);
            return offset;
        }
    }

    private void writeData(ByteBuffer byteBuffer) throws IOException {
        offset += dataFileChannel.write(byteBuffer);
        LINE_END_BUFFER.rewind();
        offset += dataFileChannel.write(LINE_END_BUFFER);
    }

    private void writeIndexEntry(IndexEntry indexEntry) throws IOException {
        entries.put(indexEntry.getName(), indexEntry);
        String line = indexEntry.getName() + INDEX_FILE_DELIMITER +
                indexEntry.getOffset() + INDEX_FILE_DELIMITER +
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
