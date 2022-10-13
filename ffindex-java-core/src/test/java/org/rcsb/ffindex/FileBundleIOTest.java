package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBundleIOTest {
    @Test
    void whenSortingIndexFile_thenBinarySearchIsSupported() throws IOException {
        Path resourcePath = Paths.get("src/test/resources/").resolve("not-sorted.ffindex");
        Path testPath = Files.createTempFile("file-bundle-test", "not-sorted.ffindex");

        Files.copy(resourcePath, testPath, StandardCopyOption.REPLACE_EXISTING);

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
        Path resourceData = Paths.get("src/test/resources/").resolve("test.data");
        Path testData = Files.createTempFile("file-bundle-test", "test.data");
        Files.copy(resourceData, testData, StandardCopyOption.REPLACE_EXISTING);

        Path resourceIndex = Paths.get("src/test/resources/").resolve("test.ffindex");
        Path testIndex = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.copy(resourceIndex, testIndex, StandardCopyOption.REPLACE_EXISTING);

        FileBundleIO.unlinkFiles(testIndex, "a", "c", "not");

        assertArrayEquals(Files.readAllBytes(resourceData), Files.readAllBytes(testData), "Data file content should not change");

        ReadableFileBundle fileBundle = FileBundleIO.openBundle(testData, testIndex).inReadOnlyMode();
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("a"));
        assertEquals("bb", Conversions.toString(fileBundle.readFile("b")));
        assertThrows(NoSuchFileException.class, () -> fileBundle.readFile("c"));
        assertEquals("fooo\nfooo", Conversions.toString(fileBundle.readFile("foo")));
    }

    @Test
    void whenCompactingDataFile_thenIndexAndDataFileUpdated() throws IOException {
        Path resourceData = Paths.get("src/test/resources/").resolve("test.data");
        Path testData = Files.createTempFile("file-bundle-test", "test.data");
        Files.copy(resourceData, testData, StandardCopyOption.REPLACE_EXISTING);

        Path resourceIndex = Paths.get("src/test/resources/").resolve("test.ffindex");
        Path testIndex = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.copy(resourceIndex, testIndex, StandardCopyOption.REPLACE_EXISTING);

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
        Path resourceData = Paths.get("src/test/resources/").resolve("test.data");
        Path testData = Files.createTempFile("file-bundle-test", "test.data");
        Files.copy(resourceData, testData, StandardCopyOption.REPLACE_EXISTING);

        Path resourceIndex = Paths.get("src/test/resources/").resolve("test.ffindex");
        Path testIndex = Files.createTempFile("file-bundle-test", "test.ffindex");
        Files.copy(resourceIndex, testIndex, StandardCopyOption.REPLACE_EXISTING);

        assertThrows(IllegalStateException.class, () -> FileBundleIO.mergeBundles(resourceData, resourceIndex, resourceData, resourceIndex));
    }

    @Test
    void whenMergingBundles_thenFirstIsAppended() throws IOException {
        Path resourceData1 = Paths.get("src/test/resources/").resolve("part1.data");
        Path testData1 = Files.createTempFile("file-bundle-test", "part1.data");
        Files.copy(resourceData1, testData1, StandardCopyOption.REPLACE_EXISTING);

        Path resourceIndex1 = Paths.get("src/test/resources/").resolve("part1.ffindex");
        Path testIndex1 = Files.createTempFile("file-bundle-test", "part1.ffindex");
        Files.copy(resourceIndex1, testIndex1, StandardCopyOption.REPLACE_EXISTING);

        Path resourceData2 = Paths.get("src/test/resources/").resolve("part2.data");
        Path testData2 = Files.createTempFile("file-bundle-test", "part2.data");
        Files.copy(resourceData2, testData2, StandardCopyOption.REPLACE_EXISTING);

        Path resourceIndex2 = Paths.get("src/test/resources/").resolve("part2.ffindex");
        Path testIndex2 = Files.createTempFile("file-bundle-test", "part2.ffindex");
        Files.copy(resourceIndex2, testIndex2, StandardCopyOption.REPLACE_EXISTING);

        FileBundleIO.mergeBundles(testData1, testIndex1, testData2, testIndex2);

        assertArrayEquals(Files.readAllBytes(testData1), Files.readAllBytes(Paths.get("src/test/resources/").resolve("test.data")));
        assertArrayEquals(Files.readAllBytes(testIndex1), Files.readAllBytes(Paths.get("src/test/resources/").resolve("test.ffindex")));
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