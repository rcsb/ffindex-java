package org.rcsb.ffindex;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A FFindex bundle, comprised of data and index file. Files are identified by their filename. Directory structures can
 * be emulated by naming the files accordingly (e.g. introduce '/' into your filenames).
 *
 * <p>Existing bundles support reading. If you want to modify a bundle then create a new one in write mode.
 */
public interface FileBundle extends Closeable {
    /**
     * How files are terminated in the data file.
     */
    String FILE_END = "\n\u0000";
    /**
     * The length of the file end sequence.
     */
    int FILE_END_LENGTH = FILE_END.length();
    /**
     * Buffer that can be used to terminate files.
     */
    ByteBuffer FILE_END_BUFFER = ByteBuffer.wrap(FILE_END.getBytes(StandardCharsets.UTF_8));
    /**
     * Delimiter in index entry lines.
     */
    String INDEX_ENTRY_DELIMITER = "\t";
}
