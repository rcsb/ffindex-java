package org.rcsb.ffindex.impl;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.AppendableFileBundle;
import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.FileBundle;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.TestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ReadWriteFileBundleTest {
    @Test
    public void whenWritingDuplicate_thenIllegalStateExceptionThrown() throws IOException {
        Path resourcePath = Paths.get("src/test/resources/");
        Path dataPath = resourcePath.resolve("test.data");
        Path indexPath = resourcePath.resolve("test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            assertThrows(IllegalStateException.class, () -> fileBundle.writeFile("a", Conversions.toByteBuffer("reject")));
        }
    }


    @Test
    public void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            fileBundle.writeFile("a", Conversions.toByteBuffer("a"));
            fileBundle.writeFile("b", Conversions.toByteBuffer("bb"));
            fileBundle.writeFile("c", Conversions.toByteBuffer("cc"));
            fileBundle.writeFile("foo", Conversions.toByteBuffer("fooo\nfooo"));

            assertArrayEquals(TestHelper.getBytes("data/a"), Conversions.toByteArray(fileBundle.readFile("a")));
            assertArrayEquals(TestHelper.getBytes("data/b"), Conversions.toByteArray(fileBundle.readFile("b")));
            assertArrayEquals(TestHelper.getBytes("data/c"), Conversions.toByteArray(fileBundle.readFile("c")));
            assertArrayEquals(TestHelper.getBytes("data2/foo"), Conversions.toByteArray(fileBundle.readFile("foo")));
        }
    }
}