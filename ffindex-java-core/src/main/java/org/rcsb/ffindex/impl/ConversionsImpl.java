package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.DataFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class ConversionsImpl implements Conversions {
    private final DataFile dataFile;

    public ConversionsImpl(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public byte[] byteArray() {
        ByteBuffer byteBuffer = dataFile.getByteBuffer();
        byteBuffer.rewind();
        byte[] out = new byte[byteBuffer.remaining()];
        byteBuffer.get(out);
        return out;
    }

    public InputStream inputStream() {
        return new ByteArrayInputStream(byteArray());
    }

    public String string(Charset charset) {
        return new String(byteArray(), charset);
    }
}