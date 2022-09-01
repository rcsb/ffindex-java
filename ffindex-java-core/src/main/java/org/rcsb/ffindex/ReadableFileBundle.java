package org.rcsb.ffindex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

/**
 * Read operations defined for a {@link FileBundle}.
 */
public interface ReadableFileBundle extends FileBundle {
    /**
     * Read a specific file, identified by its filename.
     * @param filename the name of this file
     * @return the requested file as {@link java.nio.ByteBuffer}
     * @throws IOException file doesn't exist or reading failed
     */
    ByteBuffer readFile(String filename) throws IOException;

    /**
     * Check if a file is part of this bundle.
     * @param filename the name of this file
     * @return true if this file is registered
     */
    boolean containsFile(String filename);

    /**
     * The number of files in this bundle.
     * @return an int
     */
    int fileCount();

    /**
     * A {@link Stream} over all filenames that are registered in this bundle.
     * @return all filenames
     */
    Stream<String> filenames();
}
