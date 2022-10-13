package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBundleIOTest {
    @Test
    void whenSortingIndexFile_thenBinarySearchIsSupported() throws IOException {
        Path testPath = TestHelper.createTempFile("not-sorted.ffindex");

        List<String> original = Files.lines(testPath).collect(Collectors.toList());
        assertFalse(isSorted(original));

        FileBundleIO.sortIndexFile(testPath);
        List<String> sorted = Files.lines(testPath).collect(Collectors.toList());
        assertTrue(isSorted(sorted));

        String[] filenames = sorted.stream()
                .map(l -> l.split(FileBundle.INDEX_ENTRY_DELIMITER)[0])
                .toArray(String[]::new);
        assertEquals(0, Arrays.binarySearch(filenames, "a"));
        assertEquals(1, Arrays.binarySearch(filenames, "b"));
        assertEquals(2, Arrays.binarySearch(filenames, "c"));
        assertEquals(3, Arrays.binarySearch(filenames, "foo"));
    }

    @Test
    void whenUnlinkingFiles_thenIndexUpdatedAndDataFileNot() throws IOException {
        Path testData = TestHelper.createTempFile("test.data");
        Path testIndex = TestHelper.createTempFile("test.ffindex");

        FileBundleIO.unlinkFiles(testIndex, "a", "c", "not");

        assertArrayEquals(TestHelper.getBytes("test.data"), Files.readAllBytes(testData), "Data file content should not change");

        ReadableFileBundle fileBundle = FileBundleIO.openBundle(testData, testIndex).inReadOnlyMode();
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        assertEquals("bb", Conversions.toString(fileBundle.readFile("b")));
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("c"));
        assertEquals("fooo\nfooo", Conversions.toString(fileBundle.readFile("foo")));
    }

    @Test
    void whenCompactingDataFile_thenIndexAndDataFileUpdated() throws IOException {
        Path testData = TestHelper.createTempFile("test.data");
        Path testIndex = TestHelper.createTempFile("test.ffindex");

        FileBundleIO.unlinkFiles(testIndex, "a", "c", "not");
        FileBundleIO.compactBundle(testData, testIndex);

        assertArrayEquals(TestHelper.getBytes("compact.data"), Files.readAllBytes(testData), "Data file content should not change");

        ReadableFileBundle fileBundle = FileBundleIO.openBundle(testData, testIndex).inReadOnlyMode();
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        assertEquals("bb", Conversions.toString(fileBundle.readFile("b")));
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("c"));
        assertEquals("fooo\nfooo", Conversions.toString(fileBundle.readFile("foo")));
    }

    @Test
    void whenCompactingEmptyDataFile_thenNop() throws IOException {
        Path dataPath = Files.createTempFile("file-bundle-test", "test.data");
        Path indexPath = Files.createTempFile("file-bundle-test", "test.ffindex");

        FileBundleIO.unlinkFiles(indexPath);
        FileBundleIO.compactBundle(dataPath, indexPath);

        ReadableFileBundle fileBundle = FileBundleIO.openBundle(dataPath, indexPath).inReadOnlyMode();
        assertEquals(0, fileBundle.fileCount());
    }

    @Test
    void whenMergingBundlesWithDuplicates_thenIllegalStateExceptionThrown() throws IOException {
        Path testData = TestHelper.createTempFile("test.data");
        Path testIndex = TestHelper.createTempFile("test.ffindex");

        assertThrows(IllegalStateException.class, () -> FileBundleIO.mergeBundles(testData, testIndex, testData, testIndex));
    }

    @Test
    void whenMergingBundles_thenFirstIsAppended() throws IOException {
        Path testData1 = TestHelper.createTempFile("part1.data");
        Path testIndex1 = TestHelper.createTempFile("part1.ffindex");
        Path testData2 = TestHelper.createTempFile("part2.data");
        Path testIndex2 = TestHelper.createTempFile("part2.ffindex");

        FileBundleIO.mergeBundles(testData1, testIndex1, testData2, testIndex2);

        assertArrayEquals(TestHelper.getBytes("test.data"), Files.readAllBytes(testData1), "data differs");
        assertEquals(new String(TestHelper.getBytes("test.ffindex")), new String(Files.readAllBytes(testIndex1)), "index differs");
    }

    boolean isSorted(List<String> collection) {
        if (collection.isEmpty() || collection.size() == 1) {
            return true;
        }

        Iterator<String> iter = collection.iterator();
        String current, previous = iter.next();
        while (iter.hasNext()) {
            current = iter.next();
            if (previous.compareTo(current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }
}