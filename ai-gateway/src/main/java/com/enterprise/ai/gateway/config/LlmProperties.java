package com.enterprise.ai.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Type-safe configuration properties for upstream LLM integration,
 * resilient circuit breakers, timeouts, retries, and token pricing schedules.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.upstream")
public class LlmProperties {

    /**
     * Whether to mock upstream LLM responses instead of calling live OpenAI endpoints.
     */
    private boolean mock = true;

    /**
     * Target OpenAI-compatible base URL.
     */
    private String baseUrl = "https://api.openai.com";

    /**
     * Secret API key used for upstream authorization.
     */
    private String apiKey = "sk-placeholder";

    /**
     * Default model identifier if unspecified in client request.
     */
    private String defaultModel = "gpt-4o";

    /**
     * Upstream request timeout in seconds.
     */
    private int timeoutSeconds = 30;

    /**
     * Maximum retry attempts on transient network or 5xx server errors.
     */
    private int maxRetries = 3;

    /**
     * Initial retry backoff period in milliseconds.
     */
    private long retryBackoffMs = 500;

    /**
     * Default per-1k input tokens cost in USD.
     */
    private double costPer1kInput = 0.0025;

    /**
     * Default per-1k output tokens cost in USD.
     */
    private double costPer1kOutput = 0.0100;

    /**
     * Circuit breaker settings.
     */
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();

    /**
     * Model-specific pricing schedules.
     */
    private Map<String, ModelPricing> models = new HashMap<>();

    // Getters and Setters
    public boolean isMock() { return mock; }
    public void setMock(boolean mock) { this.mock = mock; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryBackoffMs() { return retryBackoffMs; }
    public void setRetryBackoffMs(long retryBackoffMs) { this.retryBackoffMs = retryBackoffMs; }
    public double getCostPer1kInput() { return costPer1kInput; }
    public void setCostPer1kInput(double costPer1kInput) { this.costPer1kInput = costPer1kInput; }
    public double getCostPer1kOutput() { return costPer1kOutput; }
    public void setCostPer1kOutput(double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; }
    public CircuitBreakerProperties getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreakerProperties circuitBreaker) { this.circuitBreaker = circuitBreaker; }
    public Map<String, ModelPricing> getModels() { return models; }
    public void setModels(Map<String, ModelPricing> models) { this.models = models; }

    public static class CircuitBreakerProperties {
        /**
         * Number of consecutive failures before tripping circuit from CLOSED to OPEN.
         */
        private int failureThreshold = 3;

        /**
         * Number of seconds to keep circuit OPEN before transitioning to HALF_OPEN probe state.
         */
        private int resetTimeoutSeconds = 15;

        public int getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }
        public int getResetTimeoutSeconds() { return resetTimeoutSeconds; }
        public void setResetTimeoutSeconds(int resetTimeoutSeconds) { this.resetTimeoutSeconds = resetTimeoutSeconds; }
    }

    public static class ModelPricing {
        private double costPer1kInput;
        private double costPer1kOutput;

        public ModelPricing() {
        }

        public ModelPricing(double costPer1kInput, double costPer1kOutput) {
            this.costPer1kInput = costPer1kInput;
            this.costPer1kOutput = costPer1kOutput;
        }

        public double getCostPer1kInput() { return costPer1kInput; }
        public void setCostPer1kInput(double costPer1kInput) { this.costPer1kInput = costPer1kInput; }
        public double getCostPer1kOutput() { return costPer1kOutput; }
        public void setCostPer1kOutput(double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; }
    }
}
