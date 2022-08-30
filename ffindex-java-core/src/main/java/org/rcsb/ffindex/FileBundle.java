package org.rcsb.ffindex;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * A FFindex bundle, comprised of data and index file. Supports a number of read and write operations. Files are
 * identified by their filename. Directory structures can be emulated by naming the files accordingly (e.g. introduce
 * '/' into your filenames).
 *
 * <p>Bundles are append-only and don't support any modifying operations other than that. If you want to modify a
 * bundle, then create a new one.
 */
public interface FileBundle extends Closeable {
    /**
     * How files are terminated in the data file.
     */
    String FILE_END = "\n\u0000";
    /**
     * Buffer that can be used to terminate files.
     */
    ByteBuffer FILE_END_BUFFER = ByteBuffer.wrap(FILE_END.getBytes(StandardCharsets.UTF_8));
    /**
     * Delimiter in index entry lines.
     */
    String INDEX_ENTRY_DELIMITER = "\t";

    /**
     * Read a specific file, identified by its filename.
     * @param filename the name of this file
     * @return the requested file
     * @throws IOException file doesn't exist or reading failed
     */
    DataFile readFile(String filename) throws IOException;

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
