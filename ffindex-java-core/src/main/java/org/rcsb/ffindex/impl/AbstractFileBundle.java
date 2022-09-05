package org.rcsb.ffindex.impl;

import org.rcsb.ffindex.FileBundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Shared functionality of a {@link FileBundle}.
 */
public abstract class AbstractFileBundle implements FileBundle {
    protected final Path dataPath;
    protected final Path indexPath;
    protected final RandomAccessFile dataFile;
    protected final FileChannel dataFileChannel;

    AbstractFileBundle(Path dataPath, Path indexPath, String mode) throws FileNotFoundException {
        this.dataPath = dataPath;
        this.indexPath = indexPath;
        this.dataFile = new RandomAccessFile(dataPath.toFile(), mode);
        this.dataFileChannel = dataFile.getChannel();
    }

    @Override
    public Path getDataPath() {
        return dataPath;
    }

    @Override
    public Path getIndexPath() {
        return indexPath;
    }

    protected void sortIndexFile(Path indexPath) throws IOException {
        byte[] bytes = Files.lines(indexPath)
                .sorted(Comparator.comparing(l -> l.split(INDEX_ENTRY_DELIMITER)[0]))
                .collect(Collectors.joining(LINE_END, "", LINE_END))
                .getBytes(StandardCharsets.UTF_8);
        Files.write(indexPath, bytes);
    }
}
