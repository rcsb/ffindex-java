package org.rcsb.ffindex.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class FormatConstants {
    public static final String LINE_END = "\n\u0000";
    public static final ByteBuffer LINE_END_BUFFER = ByteBuffer.wrap(LINE_END.getBytes(StandardCharsets.UTF_8));
    public static final String INDEX_FILE_DELIMITER = "\t";
}
