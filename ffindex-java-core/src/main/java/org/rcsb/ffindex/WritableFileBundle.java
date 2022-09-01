package org.rcsb.ffindex;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Write operations defined for a {@link FileBundle}.
 */
public interface WritableFileBundle extends FileBundle {
    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param byteBuffer the content of this file
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     */
    long writeFile(String filename, ByteBuffer byteBuffer) throws IOException;
}
