package org.rcsb.ffindex.impl;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.ReadableFileBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ReadOnlyFileBundleTest {
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
}