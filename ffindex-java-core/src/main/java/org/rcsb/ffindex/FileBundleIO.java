package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.ReadWriteFileBundle;
import org.rcsb.ffindex.impl.WriteOnlyFileBundle;
import org.rcsb.ffindex.impl.ReadOnlyFileBundle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.rcsb.ffindex.FileBundle.INDEX_ENTRY_DELIMITER;

/**
 * IO operations on a bunch of files. FFindex-style.
 */
public class FileBundleIO {
    private FileBundleIO() {
        // deny instantiation
    }

    /**
     * Opens a handle to a file bundle. Use a try-with-resource block for this like with other IO operations.
     * The referenced files are not required to exist beforehand. Use them also to create a new FFindex bundle.
     * @param dataPath the location of the data file
     * @param indexPath the location of the corresponding index file
     * @return the {@link ModeStep}, which determines read-only or write-only mode
     */
    public static ModeStep openBundle(Path dataPath, Path indexPath) {
        return new ModeStep(dataPath, indexPath);
    }

    /**
     * Controls supported operations for the bundle.
     */
    public static class ModeStep {
        private final Path dataPath;
        private final Path indexPath;

        private ModeStep(Path dataPath, Path indexPath) {
            this.dataPath = dataPath;
            this.indexPath = indexPath;
        }

        /**
         * Create a read-only bundle.
         * @return a bundle that is read-only
         * @throws IOException reading failed
         */
        public ReadableFileBundle inReadOnlyMode() throws IOException {
            return new ReadOnlyFileBundle(dataPath, indexPath);
        }

        /**
         * Create a new write-only bundle.
         * @return a new bundle that supports write operations
         * @throws IOException files already exists or reading failed
         */
        public WritableFileBundle inWriteOnlyMode() throws IOException {
            createFiles(false, dataPath, indexPath);
            return new WriteOnlyFileBundle(dataPath, indexPath);
        }

        /**
         * Create an appendable bundle.
         * @return a bundle that supports read and write operations
         * @throws IOException initial reading failed
         */
        public AppendableFileBundle inReadWriteMode() throws IOException {
            createFiles(true, dataPath, indexPath);
            return new ReadWriteFileBundle(dataPath, indexPath);
        }

        /**
         * It's OK to open a {@link WritableFileBundle} without the files existing yet. If that's the case: create data
         * and index file.
         * @param canExist OK if files exist already?
         * @param paths any number of paths
         * @throws IOException file creation failed
         */
        private static void createFiles(boolean canExist, Path... paths) throws IOException {
            for (Path p : paths) {
                if (Files.exists(p) && canExist) {
                    continue;
                }

                Files.createFile(p);
            }
        }
    }

    /**
     * Sorts all entries of an index file. Not relevant for this library but this allows for interoperability with other
     * FFindex implementations, which perform a binary search to navigate the contents of the index file. Modifies the
     * file in place.
     * @param indexPath the location of the corresponding index file
     * @throws IOException reading or writing failed
     */
    public static void sortIndexFile(Path indexPath) throws IOException {
        byte[] bytes;
        try (Stream<String> lines = Files.lines(indexPath)) {
            bytes = lines.sorted(Comparator.comparing(l -> l.split(INDEX_ENTRY_DELIMITER)[0]))
                    .collect(Collectors.joining(FileBundle.LINE_END, "", FileBundle.LINE_END))
                    .getBytes(StandardCharsets.UTF_8);
        }
        Files.write(indexPath, bytes);
    }

    /**
     * Removes a collection of filenames from the index, effectively shadowing/hiding these files. Does not update the
     * data file, all data remains intact. Use {@link #compactBundle(Path, Path)} to make actual changes to the data
     * file (and reduce its size). Modifies the file in place.
     * @param indexPath the location of the corresponding index file
     * @param filenamesToDrop the filenames to remove from the index file
     * @throws IOException reading or writing failed
     */
    public static void unlinkFiles(Path indexPath, String... filenamesToDrop) throws IOException {
        Set<String> set = Set.of(filenamesToDrop);
        byte[] bytes;
        try (Stream<String> lines = Files.lines(indexPath)) {
            bytes = lines.filter(l -> !set.contains(l.split(INDEX_ENTRY_DELIMITER)[0]))
                    .collect(Collectors.joining(FileBundle.LINE_END, "", FileBundle.LINE_END))
                    .getBytes(StandardCharsets.UTF_8);
        }
        Files.write(indexPath, bytes);
    }

    /**
     * Removes a collection of regions from the data file, reducing the size of the data file and freeing up disk space.
     * Useful after invoking {@link #unlinkFiles(Path, String...)}. Modifies the files.
     * @param dataPath the location of the corresponding data file
     * @param indexPath the location of the corresponding index file
     * @throws IOException reading or writing failed
     */
    public static void compactBundle(Path dataPath, Path indexPath) throws IOException {
        FileChannel originalData = new RandomAccessFile(dataPath.toFile(), "r").getChannel();
        Path updatedDataPath = dataPath.resolveSibling(dataPath.getFileName() + FileBundle.TMP_EXT);
        FileChannel updatedData = new RandomAccessFile(updatedDataPath.toFile(), "rw").getChannel();
        Path updatedIndexPath = indexPath.resolveSibling(indexPath.getFileName() + FileBundle.TMP_EXT);
        FileChannel updatedIndex = new RandomAccessFile(updatedIndexPath.toFile(), "rw").getChannel();

        // parse sparse entry list and sort by offset
        List<Entry> entries = parseEntries(indexPath);
        entries.sort(Comparator.comparingLong(Entry::getOffset));

        for (Entry entry : entries) {
            String filename = entry.getFilename();
            long originalOffset = entry.getOffset();
            int length = entry.getLength();

            // move data -- optimally, this would use DirectByteBuffers -- however, there's no API to dispose them again
            // and in tight loops the OS might exceed the limit of memory-mapped regions
            ByteBuffer content = ByteBuffer.allocate(length);
            originalData.read(content, originalOffset);
            updatedData.write(content);

            // track new offset to index file
            long updatedOffset = updatedData.position();
            String line = filename + INDEX_ENTRY_DELIMITER +
                    updatedOffset + INDEX_ENTRY_DELIMITER +
                    length + FileBundle.LINE_END;
            ByteBuffer indexLine = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
            updatedIndex.write(indexLine);
        }

        // replace originals by tmp files
        Files.move(updatedDataPath, dataPath, StandardCopyOption.REPLACE_EXISTING);
        Files.move(updatedIndexPath, indexPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static List<Entry> parseEntries(Path indexPath) throws IOException {
        try (Stream<String> lines = Files.lines(indexPath)) {
            return lines.map(line -> line.split(INDEX_ENTRY_DELIMITER))
                    .map(split -> new Entry(split[0], Long.parseLong(split[1]), Integer.parseInt(split[2])))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Convenience class that represents one line of the index file.
     */
    static class Entry {
        private final String filename;
        private final long offset;
        private final int length;

        Entry(String filename, long offset, int length) {
            this.filename = filename;
            this.offset = offset;
            this.length = length;
        }

        String getFilename() {
            return filename;
        }

        long getOffset() {
            return offset;
        }

        int getLength() {
            return length;
        }
    }
}
