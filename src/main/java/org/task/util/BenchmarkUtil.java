package org.task.util;

import org.task.jdbc.JdbcExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkUtil {
    private final JdbcExecutor jdbcUtil;
    private final int count;

    public BenchmarkUtil(JdbcExecutor jdbcUtil, int count) {
        this.jdbcUtil = jdbcUtil;
        this.count = count;
    }

    public long run() throws InterruptedException{
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch latch = new CountDownLatch(count);

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                try {
                    jdbcUtil.simulateQuery();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        return System.currentTimeMillis() - start;
    }
}
