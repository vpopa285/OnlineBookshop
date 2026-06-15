package org.task;

import org.junit.jupiter.api.Test;
import org.task.util.BenchmarkUtil;
import org.task.util.JdbcUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BenchmarkUtilTest {

    @Test
    void allThreadsExecute() throws InterruptedException {
        JdbcUtil jdbcUtil = mock(JdbcUtil.class);
        int count = 8;

        new BenchmarkUtil(jdbcUtil, count).run();

        verify(jdbcUtil, times(count)).simulateQuery();
    }

    @Test
    void returnsElapsedTime() throws InterruptedException {
        JdbcUtil jdbcUtil = mock(JdbcUtil.class);
        doAnswer(invocation -> {
            Thread.sleep(100);
            return null;
        }).when(jdbcUtil).simulateQuery();

        long elapsed = new BenchmarkUtil(jdbcUtil, 4).run();

        assertTrue(elapsed >= 100, "Should take at least 100ms");
        assertTrue(elapsed < 400, "Should run in parallel, not sequentially");
    }

    @Test
    void latchCountsDownEvenOnFailure() throws InterruptedException {
        JdbcUtil jdbcUtil = mock(JdbcUtil.class);
        doThrow(new RuntimeException("DB error")).when(jdbcUtil).simulateQuery();

        long elapsed = new BenchmarkUtil(jdbcUtil, 4).run();

        assertTrue(elapsed >= 0);
    }

    @Test
    void countIsRespected() throws InterruptedException {
        JdbcUtil jdbcUtil = mock(JdbcUtil.class);
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            callCount.incrementAndGet();
            return null;
        }).when(jdbcUtil).simulateQuery();

        new BenchmarkUtil(jdbcUtil, 5).run();

        assertEquals(5, callCount.get());
    }
}
