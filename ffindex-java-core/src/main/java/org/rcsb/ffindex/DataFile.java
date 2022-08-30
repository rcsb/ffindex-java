package org.rcsb.ffindex;

import java.nio.ByteBuffer;

/**
 * The content of a FFindex file.
 */
public interface DataFile {
    /**
     * The underlying data. Represented as {@link ByteBuffer}.
     * @return the data of this file
     */
    ByteBuffer getByteBuffer();

    /**
     * A collection of rather "expensive" conversions. If possible use the exposed {@link ByteBuffer} when working with
     * this data.
     * @return a collection of supported conversions
     */
    Conversions to();
}
