package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.ReadWriteFileBundle;
import org.rcsb.ffindex.impl.WriteOnlyFileBundle;
import org.rcsb.ffindex.impl.ReadOnlyFileBundle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Sorts all entries of an index file. Not relevant for this library but this allows for interoperability with other
     * FFindex implementations, which perform a binary search to navigate the contents of the index file. Modifies the
     * file in place.
     * @param indexPath the location of the corresponding index file
     * @throws IOException reading or writing failed
     */
    public static void sortIndexFile(Path indexPath) throws IOException {
        byte[] bytes;
        try (Stream<String> lines = Files.lines(indexPath)) {
            bytes = lines.sorted(Comparator.comparing(l -> l.split(FileBundle.INDEX_ENTRY_DELIMITER)[0]))
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
            bytes = lines.filter(l -> !set.contains(l.split(FileBundle.INDEX_ENTRY_DELIMITER)[0]))
                    .collect(Collectors.joining(FileBundle.LINE_END, "", FileBundle.LINE_END))
                    .getBytes(StandardCharsets.UTF_8);
        }
        Files.write(indexPath, bytes);
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
}
