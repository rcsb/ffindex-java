package org.rcsb.ffindex.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.rcsb.ffindex.FileBundle;
import org.rcsb.ffindex.FileBundleIO;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This is tailored benchmark for strucmotif-search. Expects 'production' data in the right locations.
 */
public class ReadBenchmark {
    @Benchmark
    public void readFileSystem(Blackhole blackhole, ReadState state) throws IOException {
        for (Path path : state.files) {
            blackhole.consume(BenchmarkHelper.getBytes(path));
        }
    }

    @Benchmark
    public void readFFindex(Blackhole blackhole, ReadState state) throws IOException {
        try (FileBundle fileBundle = FileBundleIO.open(state.dataIn, state.indexIn)) {
            for (String filename : state.filenames) {
                blackhole.consume(fileBundle.readFile(filename));
            }
        }
    }

    @Benchmark
    public void readFFindexInitialized(Blackhole blackhole, ReadState state) throws IOException {
        try (FileBundle fileBundle = state.fileBundle) { // TODO this is pretty bad
            for (String filename : state.filenames) {
                blackhole.consume(fileBundle.readFile(filename));
            }
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
