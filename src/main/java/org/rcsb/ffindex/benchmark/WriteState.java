package org.rcsb.ffindex.benchmark;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@State(Scope.Benchmark)
public class WriteState {
    final Path workDirectory;
    final Path sourceDirectory;
    final Path dataOut;
    final Path indexOut;
    final Path tarOut;
    final Path tarGzOut;

    public WriteState() {
        this.workDirectory = Paths.get("/opt/data/");
        this.sourceDirectory = workDirectory.resolve("renumbered/");
        this.dataOut = Paths.get("/opt/data/benchmark-renumbered.data");
        this.indexOut = Paths.get("/opt/data/benchmark-renumbered.ffindex");
        this.tarOut = Paths.get("/opt/data/benchmark-renumbered.tar");
        this.tarGzOut = Paths.get("/opt/data/benchmark-renumbered.tar.gz");
    }

    @Setup(Level.Invocation)
    public void doSetup() throws IOException {
        cleanup();
    }

    @Setup(Level.Invocation)
    public void doTeardown() throws IOException {
        cleanup();
    }

    private void cleanup() throws IOException {
        Files.deleteIfExists(dataOut);
        Files.deleteIfExists(indexOut);
        Files.deleteIfExists(tarOut);
        Files.deleteIfExists(tarGzOut);
    }
}
