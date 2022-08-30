package org.rcsb.ffindex;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rather "expensive" operations, if possible use the exposed {@link java.nio.ByteBuffer} directly to facilitate read
 * operations.
 */
public interface Conversions {
    /**
     * Convert to a byte array.
     * @return array of bytes
     */
    byte[] byteArray();

    /**
     * Convert to an {@link InputStream}.
     * @return an input stream
     */
    InputStream inputStream();

    /**
     * Convert to a String by applying the specified charset to the underlying byte array.
     * @param charset the charset to apply
     * @return a String
     */
    String string(Charset charset);

    /**
     * Convert to a String by applying the default UTF-8 charset.
     * @return a String
     */
    default String string() {
        return string(StandardCharsets.UTF_8);
    }
}
