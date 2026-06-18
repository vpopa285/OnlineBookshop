package org.task.benchmark;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.task.util.BenchmarkUtil;
import org.task.jdbc.JdbcExecutor;

@Component
@Profile("benchmark")
@RequiredArgsConstructor
public class ConnectionBenchmarkRunner {
    private final JdbcExecutor singleConnectionJdbcExecutor;
    private final JdbcExecutor pooledJdbcExecutor;

    public void run(int threads) throws InterruptedException {
        long singleConnectionTime = new BenchmarkUtil(singleConnectionJdbcExecutor, threads).run();
        long pooledConnectionTime = new BenchmarkUtil(pooledJdbcExecutor, threads).run();

        System.out.println("Single connection: " + singleConnectionTime + "ms");
        System.out.println("Pooling connection: " + pooledConnectionTime + "ms\n");
    }
}
