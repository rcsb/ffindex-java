package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.DataFile;

import java.nio.ByteBuffer;

class ByteBufferDataFile implements DataFile {
    private final ByteBuffer byteBuffer;

    public ByteBufferDataFile(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public Conversions to() {
        return new ConversionsImpl(this);
    }
}
