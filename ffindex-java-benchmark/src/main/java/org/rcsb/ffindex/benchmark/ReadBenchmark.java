package org.rcsb.ffindex.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.rcsb.ffindex.FileBundleIO;
import org.rcsb.ffindex.ReadableFileBundle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is tailored benchmark for ffindex-java. Expects 'production' data in the right locations.
 */
public class ReadBenchmark {
    private static final ReadableFileBundle fileBundle;

    static {
        // this is pretty dirty...
        try {
            fileBundle = FileBundleIO.openBundle(Paths.get("/opt/data/renumbered.data"), Paths.get("/opt/data/renumbered.ffindex")).inReadOnlyMode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Benchmark
    public void readFileSystem(Blackhole blackhole, ReadState state) throws IOException {
        for (Path path : state.files) {
            blackhole.consume(BenchmarkHelper.hashContents(path));
        }
    }

    @Benchmark
    public void readFFindex(Blackhole blackhole, ReadState state) throws IOException {
        try (ReadableFileBundle fileBundle = FileBundleIO.openBundle(state.dataIn, state.indexIn).inReadOnlyMode()) {
            for (String filename : state.filenames) {
                blackhole.consume(BenchmarkHelper.hashContents(fileBundle.readFile(filename)));
            }
        }
    }

    @Benchmark
    public void readFFindexInitialized(Blackhole blackhole, ReadState state) throws IOException {
        for (String filename : state.filenames) {
            blackhole.consume(BenchmarkHelper.hashContents(fileBundle.readFile(filename)));
        }
    }

    /**
     * Entry point.
     * @param args nothing
     * @throws RunnerException benchmark failed
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ReadBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(10)
                .timeout(TimeValue.days(1))
                .mode(Mode.AverageTime)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
