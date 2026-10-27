package com.enterprise.ai.gateway.performance;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.dto.ChatResponse;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.client.QualityClient;
import com.enterprise.ai.gateway.client.RiskClient;
import com.enterprise.ai.gateway.client.ScannerClient;
import com.enterprise.ai.gateway.engine.DecisionEngine;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.pipeline.PipelineTracer;
import com.enterprise.ai.gateway.publisher.AuditPublisher;
import com.enterprise.ai.gateway.security.IdentityValidator;
import com.enterprise.ai.gateway.security.JwtTokenService;
import com.enterprise.ai.gateway.security.RateLimitingService;
import com.enterprise.ai.gateway.security.RbacSecurityManager;
import com.enterprise.ai.gateway.service.GatewayPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gateway Reactive Performance & Stress Tests")
class GatewayPerformanceTest {

    @Mock
    private ScannerClient scannerClient;

    @Mock
    private RiskClient riskClient;

    @Mock
    private QualityClient qualityClient;

    @Mock
    private LlmClient llmClient;

    @Mock
    private AuditPublisher auditPublisher;

    private GatewayPipelineService pipelineService;
    private RateLimitingService rateLimitingService;
    private LlmCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        IdentityValidator identityValidator = new IdentityValidator(new JwtTokenService(), new RbacSecurityManager());
        DecisionEngine decisionEngine = new DecisionEngine();
        PipelineTracer pipelineTracer = new PipelineTracer();

        pipelineService = new GatewayPipelineService(
                identityValidator,
                scannerClient,
                riskClient,
                qualityClient,
                llmClient,
                decisionEngine,
                pipelineTracer,
                auditPublisher
        );

        rateLimitingService = new RateLimitingService();
        circuitBreaker = new LlmCircuitBreaker(3, 500, 1000);
    }

    @Test
    @DisplayName("Performance: 100 concurrent requests processed non-blocking on parallel scheduler")
    void testConcurrentChatPipelineThroughput() throws InterruptedException {
        int concurrency = 100;
        CountDownLatch latch = new CountDownLatch(concurrency);
        AtomicInteger successCounter = new AtomicInteger(0);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        // Mock downstream dependencies
        when(scannerClient.scan(anyString(), anyString())).thenReturn(Mono.just(
                ScanResult.builder().blocked(false).sensitivityScore(5).violations(Collections.emptyList()).build()
        ));
        when(riskClient.assess(any())).thenReturn(Mono.just(
                com.enterprise.ai.common.dto.RiskScore.builder().compositeScore(15).riskLevel(com.enterprise.ai.common.enums.RiskLevel.LOW).recommendedAction(com.enterprise.ai.common.enums.EnforcementAction.ALLOW).build()
        ));
        when(llmClient.complete(anyString(), anyString(), anyString())).thenReturn(Mono.just(
                new LlmClient.LlmCompletion("Quick performance response", 10, 15, 0.0005)
        ));
        when(qualityClient.checkQuality(any())).thenReturn(Mono.just(
                com.enterprise.ai.common.dto.QualityResult.builder().verdict(com.enterprise.ai.common.enums.QualityVerdict.PASS).build()
        ));
        when(auditPublisher.publish(any())).thenReturn(Mono.empty());

        long startTime = System.currentTimeMillis();

        Flux.range(1, concurrency)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(i -> {
                    long reqStart = System.currentTimeMillis();
                    ChatRequest req = ChatRequest.builder()
                            .requestId("perf-req-" + i)
                            .userId("perf.tester@corp.com")
                            .userRole("ENGINEER")
                            .prompt("Performance prompt iteration " + i)
                            .model("gpt-4o")
                            .build();

                    return pipelineService.processChat(req)
                            .doOnNext(res -> {
                                latencies.add(System.currentTimeMillis() - reqStart);
                                if (res.isAllowed()) successCounter.incrementAndGet();
                                latch.countDown();
                            });
                })
                .sequential()
                .subscribe();

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        long totalElapsed = System.currentTimeMillis() - startTime;

        assertTrue(completed, "All 100 concurrent pipeline runs should finish within 10 seconds");
        assertEquals(concurrency, successCounter.get(), "All concurrent requests must complete successfully");

        // Verify sub-50ms average pipeline latency for non-blocking reactive processing
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertTrue(avgLatency < 100, "Average pipeline latency should be under 100ms, actual: " + avgLatency + "ms");
    }

    @Test
    @DisplayName("Performance: Circuit Breaker trips to OPEN under rapid burst of downstream failures")
    void testCircuitBreakerStateTransitionUnderStress() {
        assertEquals(LlmCircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // Simulate 3 consecutive downstream connection timeouts / 500 errors
        circuitBreaker.recordFailure();
        assertEquals(LlmCircuitBreaker.State.CLOSED, circuitBreaker.getState());

        circuitBreaker.recordFailure();
        assertEquals(LlmCircuitBreaker.State.CLOSED, circuitBreaker.getState());

        circuitBreaker.recordFailure(); // Threshold = 3
        assertEquals(LlmCircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertFalse(circuitBreaker.allowRequest(), "Circuit should reject requests when in OPEN state");
    }

    @Test
    @DisplayName("Performance: Rate Limiter high-concurrency atomic bucket decrementing")
    void testRateLimiterConcurrencySafety() throws InterruptedException {
        int threads = 50;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        String clientIp = "10.0.0.99";

        // Intern role quota is 20 per minute
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                RateLimitingService.RateLimitResult res = rateLimitingService.checkLimit(clientIp, UserRole.INTERN);
                if (res.isAllowed()) {
                    allowedCount.incrementAndGet();
                } else {
                    rejectedCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(20, allowedCount.get(), "Exactly 20 requests must be allowed under concurrent access");
        assertEquals(30, rejectedCount.get(), "Exactly 30 requests must be rejected under concurrent access");
    }
}
