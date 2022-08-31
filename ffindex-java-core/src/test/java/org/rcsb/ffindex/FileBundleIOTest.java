package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileBundleIOTest {
    @Test
    public void whenReadingFromFileBundle_thenContentMatchesExpectation() throws IOException {
        Path resourcePath = Paths.get("src/test/resources/");
        Path dataPath = resourcePath.resolve("test.data");
        Path indexPath = resourcePath.resolve("test.ffindex");

        try (ReadableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
            assertEquals("a", Conversions.toString(fileBundle.readFile("a")));
            assertEquals("bb", Conversions.toString(fileBundle.readFile("b")));
            assertEquals("cc", Conversions.toString(fileBundle.readFile("c")));
            assertEquals("fooo\nfooo", Conversions.toString(fileBundle.readFile("foo")));
        }
    }

    @Test
    public void whenReadingEmptyFile_thenEmptyStatusReported() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "nope.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "nope.ffindex");

        try (ReadableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
            assertFalse(fileBundle.containsFile("a"));
            assertEquals(0, fileBundle.fileCount());
            assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        }
    }

    @Test
    public void whenReadingNonExistentFile_thenEmptyStatusReported() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "nope.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "nope.ffindex");

        try (ReadableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
            assertFalse(fileBundle.containsFile("a"));
            assertEquals(0, fileBundle.fileCount());
            assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        }
    }

    @Test
    public void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(indexPath);

        try (WritableFileBundle writableFileBundle = FileBundleIO.openBundle(dataPath, indexPath).inWriteOnlyMode()) {
            writableFileBundle.writeFile("a", "a".getBytes(StandardCharsets.UTF_8));
            writableFileBundle.writeFile("b", "bb".getBytes(StandardCharsets.UTF_8));
            writableFileBundle.writeFile("c", "cc".getBytes(StandardCharsets.UTF_8));
            writableFileBundle.writeFile("foo", "fooo\nfooo".getBytes(StandardCharsets.UTF_8));
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
