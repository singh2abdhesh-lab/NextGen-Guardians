package com.enterprise.ai.gateway.llm;

import com.enterprise.ai.gateway.config.LlmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Resilient, thread-safe Circuit Breaker protecting the AI Gateway from
 * cascading upstream LLM provider outages, rate limits, and network partitions.
 */
@Component
public class LlmCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(LlmCircuitBreaker.class);

    public enum State {
        CLOSED,     // Normal operation: all traffic routed to upstream
        OPEN,       // Tripped: traffic short-circuited to mock fallback
        HALF_OPEN   // Probe: trial request allowed to verify upstream recovery
    }

    private final LlmProperties properties;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>(Instant.MIN);
    private final AtomicReference<Instant> lastStateChange = new AtomicReference<>(Instant.now());

    @Autowired
    public LlmCircuitBreaker(LlmProperties properties) {
        this.properties = properties != null ? properties : new LlmProperties();
    }

    public LlmCircuitBreaker(int failureThreshold, int resetTimeoutSeconds) {
        this.properties = new LlmProperties();
        this.properties.getCircuitBreaker().setFailureThreshold(failureThreshold);
        this.properties.getCircuitBreaker().setResetTimeoutSeconds(resetTimeoutSeconds);
    }

    public LlmCircuitBreaker(int failureThreshold, long timeoutMs, long resetTimeoutMs) {
        this(failureThreshold, (int) Math.max(1, resetTimeoutMs / 1000));
    }

    /**
     * Checks if execution through the circuit is permitted.
     *
     * @return true if circuit is CLOSED or ready for a trial probe in HALF_OPEN
     */
    public boolean allowExecution() {
        State current = state.get();
        if (current == State.CLOSED) {
            return true;
        }

        if (current == State.OPEN) {
            long openDurationSeconds = Instant.now().getEpochSecond() - lastFailureTime.get().getEpochSecond();
            int resetTimeout = properties.getCircuitBreaker().getResetTimeoutSeconds();

            if (openDurationSeconds >= resetTimeout) {
                if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    lastStateChange.set(Instant.now());
                    log.info("Circuit Breaker transitioned from OPEN to HALF_OPEN (probe permitted)");
                    return true;
                }
            }
            return false;
        }

        // In HALF_OPEN, allow probe
        return true;
    }

    public boolean allowRequest() {
        return allowExecution();
    }

    /**
     * Records a successful upstream invocation, resetting failure counters and closing the circuit.
     */
    public void recordSuccess() {
        State prev = state.getAndSet(State.CLOSED);
        consecutiveFailures.set(0);
        if (prev != State.CLOSED) {
            lastStateChange.set(Instant.now());
            log.info("Upstream LLM recovered! Circuit Breaker reset to CLOSED");
        }
    }

    /**
     * Records an upstream invocation failure, incrementing counters and tripping the circuit if threshold exceeded.
     */
    public void recordFailure() {
        lastFailureTime.set(Instant.now());
        int failures = consecutiveFailures.incrementAndGet();
        int threshold = properties.getCircuitBreaker().getFailureThreshold();

        if (state.get() == State.HALF_OPEN) {
            state.set(State.OPEN);
            lastStateChange.set(Instant.now());
            log.warn("Probe request failed in HALF_OPEN state; returning to OPEN");
        } else if (failures >= threshold && state.compareAndSet(State.CLOSED, State.OPEN)) {
            lastStateChange.set(Instant.now());
            log.error("Circuit Breaker TRIPPED to OPEN after {} consecutive upstream failures (threshold={})", failures, threshold);
        }
    }

    public State getState() {
        // Evaluate dynamic transition from OPEN to HALF_OPEN on inspection
        if (state.get() == State.OPEN) {
            long openDuration = Instant.now().getEpochSecond() - lastFailureTime.get().getEpochSecond();
            if (openDuration >= properties.getCircuitBreaker().getResetTimeoutSeconds()) {
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
            }
        }
        return state.get();
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public Instant getLastStateChange() {
        return lastStateChange.get();
    }
}

