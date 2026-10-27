package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.consumer.KafkaAuditConsumer;
import com.enterprise.ai.audit.consumer.KafkaAuditDlqConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KafkaHealthController.class)
@DisplayName("Kafka Health Controller WebMvc Tests in Audit Service")
class KafkaHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaAuditConsumer kafkaAuditConsumer;

    @MockBean
    private KafkaAuditDlqConsumer kafkaAuditDlqConsumer;

    @Test
    @DisplayName("GET /api/v1/audit/kafka/health returns consumer and DLQ status")
    void testKafkaHealthEndpoint() throws Exception {
        when(kafkaAuditConsumer.getMessagesReceived()).thenReturn(100L);
        when(kafkaAuditConsumer.getMessagesProcessed()).thenReturn(98L);
        when(kafkaAuditConsumer.getProcessingErrors()).thenReturn(2L);
        when(kafkaAuditConsumer.getLastProcessedTime()).thenReturn(Instant.now());
        when(kafkaAuditDlqConsumer.getDlqMessagesReceived()).thenReturn(2L);

        mockMvc.perform(get("/api/v1/audit/kafka/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditTopic").value("ai-guardian-audit-events"))
                .andExpect(jsonPath("$.dlqTopic").value("ai-guardian-audit-events.DLT"))
                .andExpect(jsonPath("$.messagesReceived").value(100))
                .andExpect(jsonPath("$.messagesProcessed").value(98))
                .andExpect(jsonPath("$.dlqMessagesReceived").value(2));
    }
}
