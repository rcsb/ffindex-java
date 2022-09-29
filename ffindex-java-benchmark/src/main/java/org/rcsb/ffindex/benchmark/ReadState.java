package org.rcsb.ffindex.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class ReadState {
    private static final int TO_READ = 1000;
    final Path workDirectory;
    final Path sourceDirectory;
    final Path dataIn;
    final Path indexIn;
    final List<Path> files;
    final List<String> filenames;

    @SuppressWarnings("unchecked")
    public ReadState() {
        this.workDirectory = Paths.get("/opt/data/");
        this.sourceDirectory = workDirectory.resolve("renumbered/");
        this.dataIn = Paths.get("/opt/data/renumbered.data");
        this.indexIn = Paths.get("/opt/data/renumbered.ffindex");
        Object[] data = initLists();
        this.files = (List<Path>) data[0];
        this.filenames = (List<String>) data[1];
    }

    private Object[] initLists() {
        try {
            List<Path> allFiles = Files.list(sourceDirectory).collect(Collectors.toList());
            Collections.shuffle(allFiles);
            List<Path> filesToRead = allFiles.stream().limit(TO_READ).collect(Collectors.toList());
            List<String> filenamesToRead = filesToRead.stream()
                    .map(sourceDirectory::relativize)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return new Object[] { filesToRead, filenamesToRead };
        } catch (IOException e) {
            System.err.println("Couldn't initialize benchmark state");
            return new Object[] { Collections.emptyList(), Collections.emptyList() };
        }
    }
}
