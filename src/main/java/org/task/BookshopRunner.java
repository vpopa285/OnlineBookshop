package org.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.task.benchmark.ConnectionBenchmarkRunner;

@Component
@Profile("!test")
public class BookshopRunner implements CommandLineRunner {
    private final App app;
    private final ObjectProvider<ConnectionBenchmarkRunner> benchmarkRunner;

    public BookshopRunner(App app, ObjectProvider<ConnectionBenchmarkRunner> benchmarkRunner) {
        this.app = app;
        this.benchmarkRunner = benchmarkRunner;
    }

    @Override
    public void run(String... args) {
        benchmarkRunner.ifAvailable(runner -> {
            try {
                runner.run(8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Benchmark was interrupted", e);
            }
        });

        app.run();
    }
}
