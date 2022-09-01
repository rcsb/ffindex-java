package org.rcsb.ffindex.impl;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.ReadableFileBundle;
import org.rcsb.ffindex.TestHelper;
import org.rcsb.ffindex.WritableFileBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WriteOnlyFileBundleTest {
    @Test
    public void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(indexPath);

        try (WritableFileBundle writableFileBundle = FileBundleIO.openBundle(dataPath, indexPath).inWriteOnlyMode()) {
            writableFileBundle.writeFile("a", Conversions.toByteBuffer("a"));
            writableFileBundle.writeFile("b", Conversions.toByteBuffer("bb"));
            writableFileBundle.writeFile("c", Conversions.toByteBuffer("cc"));
            writableFileBundle.writeFile("foo", Conversions.toByteBuffer("fooo\nfooo"));
            writableFileBundle.close();

            try (ReadableFileBundle readableFileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
                assertArrayEquals(TestHelper.getBytes("data/a"), Conversions.toByteArray(readableFileBundle.readFile("a")));
                assertArrayEquals(TestHelper.getBytes("data/b"), Conversions.toByteArray(readableFileBundle.readFile("b")));
                assertArrayEquals(TestHelper.getBytes("data/c"), Conversions.toByteArray(readableFileBundle.readFile("c")));
                assertArrayEquals(TestHelper.getBytes("data2/foo"), Conversions.toByteArray(readableFileBundle.readFile("foo")));
            }
        }

        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(indexPath);
    }
}