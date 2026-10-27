package com.enterprise.ai.gateway.pipeline;

import com.enterprise.ai.common.dto.ChatResponse.PipelineStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages chronological execution tracing across all gateway pipeline lifecycle stages.
 */
@Component
public class PipelineTracer {

    private final ObjectMapper objectMapper;

    public PipelineTracer() {
        this(new ObjectMapper());
    }

    public PipelineTracer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Initializes a new thread-safe trace list.
     *
     * @return synchronized List of PipelineStage
     */
    public List<PipelineStage> createTrace() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Records an execution stage into the trace list.
     *
     * @param trace        active pipeline trace list
     * @param stageName    name of stage (e.g. INTERCEPT, SCAN, RISK)
     * @param serviceName  name of target service executing stage
     * @param status       status code (OK, BLOCKED, REVIEW, SKIPPED, FAILED)
     * @param summary      human readable summary
     * @param dataSent     input payload sent to stage
     * @param dataReceived output payload received from stage
     */
    public void recordStage(List<PipelineStage> trace,
                            String stageName,
                            String serviceName,
                            String status,
                            String summary,
                            Object dataSent,
                            Object dataReceived) {
        PipelineStage stage = PipelineStage.builder()
                .stageName(stageName)
                .serviceName(serviceName)
                .status(status)
                .summary(summary)
                .dataSent(toMap(dataSent))
                .dataReceived(toMap(dataReceived))
                .build();
        trace.add(stage);
    }

    /**
     * Appends skipped markers for downstream stages when a pipeline halts prematurely.
     *
     * @param trace      active pipeline trace
     * @param skippedStages list of stage names to mark as skipped
     * @param reason     reason for skip
     */
    public void recordSkipped(List<PipelineStage> trace, List<String> skippedStages, String reason) {
        for (String stage : skippedStages) {
            recordStage(trace, stage, getServiceForStage(stage), "SKIPPED", reason, null, null);
        }
    }

    public String getServiceForStage(String stage) {
        return switch (stage) {
            case "SCAN", "REDACT" -> "prompt-security-scanner";
            case "RISK" -> "risk-scoring-engine";
            case "QUALITY" -> "response-quality-checker";
            case "AUDIT" -> "audit-service";
            default -> "ai-gateway";
        };
    }

    @SuppressWarnings("unchecked")
    private Object toMap(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Map || obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return obj;
        }
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return Map.of("value", obj.toString());
        }
    }
}

