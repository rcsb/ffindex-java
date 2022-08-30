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
import org.rcsb.ffindex.impl.AppendableFileBundle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This is tailored benchmark for ffindex-java. Expects 'production' data in the right locations. pigz must be
 * installed.
 */
public class WriteBenchmark {
    @Benchmark
    public void writeTar(Blackhole blackhole, WriteState state) throws IOException, InterruptedException {
        String[] arguments = new String[] { "tar", "-c", "-f", state.tarOut.toString(), "-C", state.workDirectory.toString(), state.sourceDirectory.toString() };
        blackhole.consume(runCommand(arguments));
    }

    @Benchmark
    public void writeTarGzSingleThreaded(Blackhole blackhole, WriteState state) throws IOException, InterruptedException {
        String[] arguments = new String[] { "tar", "-c", "-z", "-f",state.tarGzOut.toString(), "-C", state.workDirectory.toString(), state.sourceDirectory.toString() };
        blackhole.consume(runCommand(arguments));
    }

    @Benchmark
    public void writeTarGzMultiThreaded(Blackhole blackhole, WriteState state) throws IOException, InterruptedException {
        String[] arguments = new String[] { "tar", "-c", "--use-compress-program=pigz", "-f", state.tarGzOut.toString(), "-C", state.workDirectory.toString(), state.sourceDirectory.toString() };
        blackhole.consume(runCommand(arguments));
    }

    @Benchmark
    public void writeFFindexSingleThreaded(Blackhole blackhole, WriteState state) throws IOException {
        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(state.dataOut, state.indexOut).inAppendableMode()) {
            blackhole.consume(addDirectory(fileBundle, state.sourceDirectory, false));
        }
    }

    @Benchmark
    public void writeFFindexMultiThreaded(Blackhole blackhole, WriteState state) throws IOException {
        try (AppendableFileBundle fileBundle = FileBundleIO.openBundle(state.dataOut, state.indexOut).inAppendableMode()) {
            blackhole.consume(addDirectory(fileBundle, state.sourceDirectory, true));
        }
    }

    private int addDirectory(AppendableFileBundle fileBundle, Path sourceDirectory, boolean parallel) throws IOException {
        AtomicInteger counter = new AtomicInteger(0);
        Stream<Path> files = Files.walk(sourceDirectory);
        if (parallel) files = files.parallel();

        files.filter(Files::isRegularFile).forEach(p -> {
            try {
                Path relative = sourceDirectory.relativize(p);
                counter.incrementAndGet();
                fileBundle.writeFile(relative.toString(), BenchmarkHelper.getBytes(p));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return counter.get();
    }

    private int runCommand(String[] args) throws IOException, InterruptedException {
        return new ProcessBuilder(args)
//                .inheritIO() // useful for debugging
                .start()
                .waitFor();
    }

    /**
     * Entry point.
     * @param args nothing
     * @throws RunnerException benchmark failed
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(WriteBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(10)
                .timeout(TimeValue.days(1))
                .mode(Mode.AverageTime)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
