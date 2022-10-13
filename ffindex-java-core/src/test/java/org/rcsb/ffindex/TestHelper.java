package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestHelper {
    public static InputStream getInputStream(String localPath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localPath);
        InputStream out = inputStream == null ? TestHelper.class.getResourceAsStream(localPath) : inputStream;
        Objects.requireNonNull(out, "Could not load test resource " + localPath);
        return out;
    }

    public static byte[] getBytes(String localPath) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead;
        byte[] buffer = new byte[1024];
        InputStream inputStream = getInputStream(localPath);
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        byteArrayOutputStream.flush();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        inputStream.close();

        return byteArray;
    }

    public static Path createTempFile(String localPath) throws IOException {
        Path path = Files.createTempFile("file-bundle-tests", localPath);
        Files.write(path, getBytes(localPath));
        return path;
    }

    @Test
    void shouldAcquireInputStream() {
        assertNotNull(getInputStream("test.data"), "Could not acquire InputStream of local resource");
    }
}
