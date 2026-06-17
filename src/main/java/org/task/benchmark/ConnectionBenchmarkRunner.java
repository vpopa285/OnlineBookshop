package org.task.benchmark;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.task.util.BenchmarkUtil;
import org.task.util.JdbcUtil;

@Component
@Profile("benchmark")
public class ConnectionBenchmarkRunner {
    private final JdbcUtil singleConnectionJdbcUtil;
    private final JdbcUtil pooledJdbcUtil;

    public ConnectionBenchmarkRunner(
            @Qualifier("singleConnectionJdbcUtil") JdbcUtil singleConnectionJdbcUtil,
            @Qualifier("pooledBenchmarkJdbcUtil") JdbcUtil pooledJdbcUtil) {
        this.singleConnectionJdbcUtil = singleConnectionJdbcUtil;
        this.pooledJdbcUtil = pooledJdbcUtil;
    }

    public void run(int threads) throws InterruptedException {
        long singleConnectionTime = new BenchmarkUtil(singleConnectionJdbcUtil, threads).run();
        long pooledConnectionTime = new BenchmarkUtil(pooledJdbcUtil, threads).run();

        System.out.println("Single connection: " + singleConnectionTime + "ms");
        System.out.println("Pooling connection: " + pooledConnectionTime + "ms\n");
    }
}
