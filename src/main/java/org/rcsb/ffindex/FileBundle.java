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
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param byteBuffer the content of this file
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     */
    long writeFile(String filename, ByteBuffer byteBuffer) throws IOException;

    /**
     * The number of files in this bundle.
     * @return an int
     */
    int size();

    /**
     * A {@link Stream} over all filenames that are registered in this bundle.
     * @return all filenames
     */
    Stream<String> filenames();

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as String
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    default long writeFile(String filename, String content) throws IOException {
        return writeFile(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as byte array
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    default long writeFile(String filename, byte[] content) throws IOException {
        return writeFile(filename, ByteBuffer.wrap(content));
    }
}
