package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.AppendableFileBundle;
import org.rcsb.ffindex.impl.IndexEntryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * IO operations on a bunch of files. FFindex-style.
 */
public class FileBundleIO {
    private static final Logger logger = LoggerFactory.getLogger(FileBundleIO.class);

    /**
     * Opens a handle to a file bundle. Use a try-with-resource block for this like with other IO operations.
     * The referenced files are not required to exist beforehand. Use them also to create a new FFindex bundle.
     * @param dataPath the location of the data file
     * @param indexPath the location of the corresponding index file
     * @return a {@link FileBundle}, which supports read and limited write operations
     * @throws IOException e.g. upon missing read permissions
     */
    public static FileBundle open(Path dataPath, Path indexPath) throws IOException {
        // TODO options, like read-only mode
        long start = System.nanoTime();
        RandomAccessFile dataFile = new RandomAccessFile(dataPath.toFile(), "rw");
        // need file channel because RandomAccessFile only supports offsets of type int
        FileChannel dataFileChannel = dataFile.getChannel();
        FileChannel indexFileChannel = new FileOutputStream(indexPath.toFile(), true).getChannel();
        Map<String, IndexEntry> entries = parseEntryIndex(indexPath);
        FileBundle out = new AppendableFileBundle(dataFile, dataFileChannel, indexFileChannel, entries);
        logger.debug("Initialized bundle ({}, {}) in {} ms", dataPath, indexPath, (int) ((System.nanoTime() - start) * 0.001 * 0.001));
        return out;
    }

    private static Map<String, IndexEntry> parseEntryIndex(Path indexPath) throws IOException {
        List<String> lines = Files.readAllLines(indexPath);
        Map<String, IndexEntry> out = new HashMap<>();
        for (String line : lines) {
            String[] split = line.split(INDEX_ENTRY_DELIMITER);
            IndexEntry entry = new IndexEntryImpl(split[0], Long.parseLong(split[1]), Integer.parseInt(split[2]));
            out.put(entry.getName(), entry);
        }
        return out;
    }
}
