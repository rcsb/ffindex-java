package org.rcsb.ffindex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rather "expensive" operations, if possible use the exposed {@link java.nio.ByteBuffer} directly to facilitate read
 * operations.
 */
class Conversions {
    /**
     * Convert to a byte array.
     * @return array of bytes
     */
    static byte[] toByteArray(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        byte[] out = new byte[byteBuffer.remaining()];
        byteBuffer.get(out);
        return out;
    }

    /**
     * Convert to an {@link InputStream}.
     * @return an input stream
     */
    static InputStream toInputStream(ByteBuffer byteBuffer) {
        return new ByteArrayInputStream(toByteArray(byteBuffer));
    }

    /**
     * Convert to a String by applying the specified charset to the underlying byte array.
     * @param charset the charset to apply
     * @return a String
     */
    static String toString(ByteBuffer byteBuffer, Charset charset) {
        return new String(toByteArray(byteBuffer), charset);
    }

    /**
     * Convert to a String by applying the default UTF-8 charset.
     * @return a String
     */
    static String toString(ByteBuffer byteBuffer) {
        return toString(byteBuffer, StandardCharsets.UTF_8);
    }
}
