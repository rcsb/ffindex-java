package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.ReadWriteFileBundle;
import org.rcsb.ffindex.impl.WriteOnlyFileBundle;
import org.rcsb.ffindex.impl.ReadOnlyFileBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * IO operations on a bunch of files. FFindex-style.
 */
public class FileBundleIO {
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
         * Create a write-only bundle.
         * @return a bundle that supports write operations
         * @throws IOException reading failed
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
