package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RunState {
    private volatile Long runId;
    private final AtomicInteger testNo = new AtomicInteger(0);

    public synchronized void ensureRun(BenchRepo bench, String label, String variant) throws SQLException {
        if (runId == null) {
            this.runId = bench.startRun(label, variant);
            this.testNo.set(0);
        }
    }

    public int nextTestNo() {
        return testNo.incrementAndGet();
    }

    public Long getRunId() {
        return runId;
    }
}