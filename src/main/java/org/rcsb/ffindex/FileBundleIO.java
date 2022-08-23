package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.AppendableFileBundle;
import org.rcsb.ffindex.impl.IndexEntryImpl;
import org.rcsb.ffindex.impl.ReadOnlyFileBundle;

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
    /**
     * Opens a handle to a file bundle. Use a try-with-resource block for this like with other IO operations.
     * The referenced files are not required to exist beforehand. Use them also to create a new FFindex bundle.
     * @param dataPath the location of the data file
     * @param indexPath the location of the corresponding index file
     * @return the {@link ModeStep}, which determines read-only or appendable mode
     * @throws IOException e.g. upon missing read permissions
     */
    public static ModeStep openBundle(Path dataPath, Path indexPath) throws IOException {
        return new ModeStep(dataPath, indexPath, parseEntryIndex(indexPath));
    }

    /**
     * Controls supported operations for the bundle.
     */
    public static class ModeStep {
        private final Path dataPath;
        private final Path indexPath;
        private final Map<String, IndexEntry> entries;

        private ModeStep(Path dataPath, Path indexPath, Map<String, IndexEntry> entries) {
            this.dataPath = dataPath;
            this.indexPath = indexPath;
            this.entries = entries;
        }

        /**
         * Create a read-only bundle.
         * @return a bundle that is read-only
         * @throws IOException reading failed
         */
        public ReadOnlyFileBundle inReadOnlyMode() throws IOException {
            RandomAccessFile dataFile = new RandomAccessFile(dataPath.toFile(), "r");
            FileChannel dataFileChannel = dataFile.getChannel();
            return new ReadOnlyFileBundle(dataFile, dataFileChannel, entries);
        }

        /**
         * Create an appendable bundle.
         * @return a bundle that supports write operations
         * @throws IOException reading failed
         */
        public AppendableFileBundle inAppendableMode() throws IOException {
            RandomAccessFile dataFile = new RandomAccessFile(dataPath.toFile(), "rw");
            FileChannel dataFileChannel = dataFile.getChannel();
            FileChannel indexFileChannel = new FileOutputStream(indexPath.toFile(), true).getChannel();
            return new AppendableFileBundle(dataFile, dataFileChannel, indexFileChannel, entries);
        }
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
