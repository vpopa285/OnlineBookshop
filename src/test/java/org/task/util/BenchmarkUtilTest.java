package org.task.util;

import org.junit.jupiter.api.Test;
import org.task.jdbc.JdbcExecutor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BenchmarkUtilTest {

    @Test
    void allThreadsExecute() throws InterruptedException {
        JdbcExecutor jdbcExecutor = mock(JdbcExecutor.class);
        int count = 8;

        new BenchmarkUtil(jdbcExecutor, count).run();

        verify(jdbcExecutor, times(count)).simulateQuery();
    }

    @Test
    void returnsElapsedTime() throws InterruptedException {
        JdbcExecutor jdbcExecutor = mock(JdbcExecutor.class);
        doAnswer(invocation -> {
            Thread.sleep(100);
            return null;
        }).when(jdbcExecutor).simulateQuery();

        long elapsed = new BenchmarkUtil(jdbcExecutor, 4).run();

        assertTrue(elapsed >= 100, "Should take at least 100ms");
        assertTrue(elapsed < 400, "Should run in parallel, not sequentially");
    }

    @Test
    void latchCountsDownEvenOnFailure() throws InterruptedException {
        JdbcExecutor jdbcExecutor = mock(JdbcExecutor.class);
        doThrow(new RuntimeException("DB error")).when(jdbcExecutor).simulateQuery();

        long elapsed = new BenchmarkUtil(jdbcExecutor, 4).run();

        assertTrue(elapsed >= 0);
    }

    @Test
    void countIsRespected() throws InterruptedException {
        JdbcExecutor jdbcExecutor = mock(JdbcExecutor.class);
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            callCount.incrementAndGet();
            return null;
        }).when(jdbcExecutor).simulateQuery();

        new BenchmarkUtil(jdbcExecutor, 5).run();

        assertEquals(5, callCount.get());
    }
}
