package com.enterprise.ai.gateway.llm;

import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LLM Circuit Breaker Unit Tests")
class LlmCircuitBreakerTest {

    private LlmProperties properties;
    private LlmCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        properties = new LlmProperties();
        properties.getCircuitBreaker().setFailureThreshold(3);
        properties.getCircuitBreaker().setResetTimeoutSeconds(1); // 1 second for fast test
        circuitBreaker = new LlmCircuitBreaker(properties);
    }

    @Test
    @DisplayName("Initial state should be CLOSED allowing execution")
    void testInitialState() {
        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.allowExecution());
        assertEquals(0, circuitBreaker.getConsecutiveFailures());
    }

    @Test
    @DisplayName("Failures below threshold keep circuit CLOSED")
    void testFailuresBelowThreshold() {
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();

        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(2, circuitBreaker.getConsecutiveFailures());
        assertTrue(circuitBreaker.allowExecution());
    }

    @Test
    @DisplayName("Failures reaching threshold trip circuit to OPEN")
    void testTripToOpen() {
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();

        assertEquals(State.OPEN, circuitBreaker.getState());
        assertEquals(3, circuitBreaker.getConsecutiveFailures());
        assertFalse(circuitBreaker.allowExecution());
    }

    @Test
    @DisplayName("Success resets failure counter to 0")
    void testSuccessResetsCounter() {
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordSuccess();

        assertEquals(0, circuitBreaker.getConsecutiveFailures());
        assertEquals(State.CLOSED, circuitBreaker.getState());
    }

    @Test
    @DisplayName("Expired reset timeout transitions OPEN to HALF_OPEN")
    void testTransitionToHalfOpen() throws InterruptedException {
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();

        assertEquals(State.OPEN, circuitBreaker.getState());

        Thread.sleep(1100); // Wait for resetTimeoutSeconds (1s) to elapse

        assertTrue(circuitBreaker.allowExecution());
        assertEquals(State.HALF_OPEN, circuitBreaker.getState());
    }
}
