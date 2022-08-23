package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.impl.AppendableFileBundle;

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

        try (FileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
            assertEquals("a", fileBundle.readFile("a").to().string());
            assertEquals("bb", fileBundle.readFile("b").to().string());
            assertEquals("cc", fileBundle.readFile("c").to().string());
            assertEquals("fooo\nfooo", fileBundle.readFile("foo").to().string());
        }
    }

    @Test
    public void whenReadingNothing_thenEmptyStatusReported() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "nope.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "nope.ffindex");

        try (FileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode()) {
            assertFalse(fileBundle.containsFile("a"));
            assertEquals(0, fileBundle.size());
            assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        }
    }

    @Test
    public void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inAppendableMode()) {
            fileBundle.writeFile("a", "a".getBytes(StandardCharsets.UTF_8));
            fileBundle.writeFile("b", "bb".getBytes(StandardCharsets.UTF_8));
            fileBundle.writeFile("c", "cc".getBytes(StandardCharsets.UTF_8));
            fileBundle.writeFile("foo", "fooo\nfooo".getBytes(StandardCharsets.UTF_8));

            assertArrayEquals(TestHelper.getBytes("data/a"), fileBundle.readFile("a").to().byteArray());
            assertArrayEquals(TestHelper.getBytes("data/b"), fileBundle.readFile("b").to().byteArray());
            assertArrayEquals(TestHelper.getBytes("data/c"), fileBundle.readFile("c").to().byteArray());
            assertArrayEquals(TestHelper.getBytes("data2/foo"), fileBundle.readFile("foo").to().byteArray());
        }
    }

    @Test
    public void whenWritingDuplicate_thenIllegalStateExceptionThrown() throws IOException {
        Path resourcePath = Paths.get("src/test/resources/");
        Path dataPath = resourcePath.resolve("test.data");
        Path indexPath = resourcePath.resolve("test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inAppendableMode()) {
            assertThrows(IllegalStateException.class, () -> fileBundle.writeFile("a", "reject"));
        }
    }
}
