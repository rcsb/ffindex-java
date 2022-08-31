package org.rcsb.ffindex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Writable file bundles are new files
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

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as String
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    default long writeFile(String filename, String content) throws IOException {
        return writeFile(filename, content, StandardCharsets.UTF_8);
    }

    /**
     * Add a new file to this bundle.
     * @param filename the name of this file
     * @param content the content of this file, as String
     * @param charset the charset to use
     * @return the new offset (i.e., the position in the data file where the next file will be written)
     * @throws IOException file already exists or writing failed
     * @see #writeFile(String, ByteBuffer)
     */
    default long writeFile(String filename, String content, Charset charset) throws IOException {
        return writeFile(filename, content.getBytes(charset));
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
