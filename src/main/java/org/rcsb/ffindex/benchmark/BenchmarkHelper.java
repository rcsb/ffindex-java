package org.rcsb.ffindex.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility functions for benchmarking.
 */
class BenchmarkHelper {
    /**
     * Get the contents of a file.
     * @param path what to read
     * @return what was read
     * @throws IOException no read
     */
    static byte[] getBytes(Path path) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead;
        byte[] buffer = new byte[1024];
        InputStream inputStream = Files.newInputStream(path);
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        byteArrayOutputStream.flush();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        inputStream.close();

        return byteArray;
    }
}
