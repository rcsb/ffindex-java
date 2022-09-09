package org.rcsb.ffindex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
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