package org.rcsb.ffindex.impl;

import org.junit.jupiter.api.Test;
import org.rcsb.ffindex.Conversions;
import org.rcsb.ffindex.FileBundle;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.ReadableFileBundle;
import org.rcsb.ffindex.TestHelper;
import org.rcsb.ffindex.WritableFileBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WriteOnlyFileBundleTest {
    @Test
    void whenWritingContent_thenIndexUpdatedAndContentMatches() throws IOException {
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

    @Test
    void whenWritingAndSorting_thenIndexSorted() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(indexPath);

        try (WritableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inWriteOnlyMode()) {
            fileBundle.writeFile("foo", Conversions.toByteBuffer("fooo\nfooo"));
            fileBundle.writeFile("c", Conversions.toByteBuffer("cc"));
            fileBundle.writeFile("b", Conversions.toByteBuffer("bb"));
            fileBundle.writeFile("a", Conversions.toByteBuffer("a"));

            fileBundle.sortIndexFile();
        }

        List<String> observed = Files.lines(indexPath).map(l -> l.split(FileBundle.INDEX_ENTRY_DELIMITER)[0]).collect(Collectors.toList());
        List<String> expected = List.of("a", "b", "c", "foo");
        assertEquals(expected, observed);
    }

    @Test
    void whenWritingAndSortingAndWriting_thenWritingSucceeds() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(indexPath);

        try (WritableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inWriteOnlyMode()) {
            fileBundle.writeFile("foo", Conversions.toByteBuffer("fooo\nfooo"));
            fileBundle.writeFile("c", Conversions.toByteBuffer("cc"));
            fileBundle.sortIndexFile();

            fileBundle.writeFile("b", Conversions.toByteBuffer("bb"));
            fileBundle.writeFile("a", Conversions.toByteBuffer("a"));
            fileBundle.sortIndexFile();

            fileBundle.writeFile("g", Conversions.toByteBuffer("ggg"));
            fileBundle.writeFile("h", Conversions.toByteBuffer("hhh"));
            fileBundle.sortIndexFile();
        }

        List<String> observed = Files.lines(indexPath).map(l -> l.split(FileBundle.INDEX_ENTRY_DELIMITER)[0]).collect(Collectors.toList());
        List<String> expected = List.of("a", "b", "c", "foo", "g", "h");
        assertEquals(expected, observed);
    }
}