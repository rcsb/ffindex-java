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
     * @throws IOException file already exists or writing failed
     */
    void writeFile(String filename, ByteBuffer byteBuffer) throws IOException;

    /**
     * Entries in the index file must appear in ascending order. Make sure to write file in the correct order or invoke
     * this method after all writing to ensure a proper index file, which can be read by other impls.
     * @throws IOException file manipulation failed
     */
    void sortIndexFile() throws IOException;
}
