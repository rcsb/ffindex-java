package org.rcsb.ffindex.impl;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.AppendableFileBundle;
import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.TestHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReadWriteFileBundleTest {
    @Test
    void whenWritingDuplicates_thenIllegalStateExceptionThrown() throws IOException {
        Path dataPath = TestHelper.createTempFile("test.data");
        Path indexPath = TestHelper.createTempFile("test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            ByteBuffer byteBuffer = Conversions.toByteBuffer("reject");
            assertThrows(IllegalStateException.class, () -> fileBundle.writeFile("a", byteBuffer));
        }
    }

    @Test
    void whenWritingDuplicatesDynamically_thenIllegalStateExceptionThrown() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            fileBundle.writeFile("a", Conversions.toByteBuffer("OK"));
            ByteBuffer byteBuffer = Conversions.toByteBuffer("reject");
            assertThrows(IllegalStateException.class, () -> fileBundle.writeFile("a", byteBuffer));
        }
    }

    @Test
    void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
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

    @Test
    void whenOpeningExistingBundle_thenOffsetDeterminedCorrectly() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");

        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            fileBundle.writeFile("a", Conversions.toByteBuffer("a"));
            fileBundle.writeFile("b", Conversions.toByteBuffer("bb"));

            assertArrayEquals(TestHelper.getBytes("data/a"), Conversions.toByteArray(fileBundle.readFile("a")));
            assertArrayEquals(TestHelper.getBytes("data/b"), Conversions.toByteArray(fileBundle.readFile("b")));
        }

        // the offset of the bundle isn't exposed -- but written data will be corrupted if offset logic is wrong
        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadWriteMode()) {
            fileBundle.writeFile("c", Conversions.toByteBuffer("cc"));
            fileBundle.writeFile("foo", Conversions.toByteBuffer("fooo\nfooo"));

            assertArrayEquals(TestHelper.getBytes("data/a"), Conversions.toByteArray(fileBundle.readFile("a")));
            assertArrayEquals(TestHelper.getBytes("data/b"), Conversions.toByteArray(fileBundle.readFile("b")));
            assertArrayEquals(TestHelper.getBytes("data/a"), Conversions.toByteArray(fileBundle.readFile("a")));
            assertArrayEquals(TestHelper.getBytes("data/b"), Conversions.toByteArray(fileBundle.readFile("b")));
        }
    }
}