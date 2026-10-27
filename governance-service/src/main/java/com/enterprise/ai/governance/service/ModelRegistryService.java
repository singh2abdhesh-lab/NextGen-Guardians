package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.ModelRegistrationDto;
import com.enterprise.ai.governance.entity.ModelRegistrationEntity;
import com.enterprise.ai.governance.repository.ModelRegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ModelRegistryService {

    private static final Logger log = LoggerFactory.getLogger(ModelRegistryService.class);

    private final ModelRegistrationRepository modelRepository;
    private final NotificationService notificationService;

    public ModelRegistryService(
            ModelRegistrationRepository modelRepository,
            NotificationService notificationService) {
        this.modelRepository = modelRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ModelRegistrationDto> getAllModels() {
        return modelRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModelRegistrationDto> getActiveModels() {
        return modelRepository.findByEnabledTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModelRegistrationDto getModelById(Long id) {
        return modelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Model not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ModelRegistrationDto getModelByName(String modelName) {
        return modelRepository.findByModelName(modelName)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Model not found with name: " + modelName));
    }

    @Transactional
    public ModelRegistrationDto registerModel(ModelRegistrationDto dto) {
        if (modelRepository.existsByModelName(dto.getModelName())) {
            throw new IllegalArgumentException("Model with name '" + dto.getModelName() + "' is already registered");
        }

        ModelRegistrationEntity entity = toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setLastHealthCheck(LocalDateTime.now());

        ModelRegistrationEntity saved = modelRepository.save(entity);

        notificationService.sendAlert("New Model Registered",
                "Model '" + saved.getModelName() + "' (" + saved.getVendor() + ") added to Enterprise Model Registry",
                "INFO", "SECURITY_LEAD");

        return toDto(saved);
    }

    @Transactional
    public ModelRegistrationDto updateModel(Long id, ModelRegistrationDto dto) {
        ModelRegistrationEntity existing = modelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Model not found with id: " + id));

        existing.setVendor(dto.getVendor());
        existing.setVersion(dto.getVersion());
        existing.setEndpoint(dto.getEndpoint());
        existing.setApiType(dto.getApiType());
        existing.setSupportedFeatures(dto.getSupportedFeatures());
        existing.setCostPer1kInput(dto.getCostPer1kInput());
        existing.setCostPer1kOutput(dto.getCostPer1kOutput());
        existing.setContextWindow(dto.getContextWindow());
        existing.setStreamingSupport(dto.getStreamingSupport());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setOwner(dto.getOwner());
        existing.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : existing.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        return toDto(modelRepository.save(existing));
    }

    @Transactional
    public ModelRegistrationDto toggleModelStatus(Long id, boolean enabled) {
        ModelRegistrationEntity existing = modelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Model not found with id: " + id));

        existing.setEnabled(enabled);
        existing.setStatus(enabled ? "APPROVED" : "RESTRICTED");
        existing.setUpdatedAt(LocalDateTime.now());

        ModelRegistrationEntity updated = modelRepository.save(existing);

        notificationService.sendAlert("Model Status Changed",
                "Model '" + updated.getModelName() + "' was toggled to " + (enabled ? "ENABLED" : "DISABLED"),
                "WARN", "SECURITY_LEAD");

        return toDto(updated);
    }

    @Transactional
    public ModelRegistrationDto pingHealthCheck(Long id) {
        ModelRegistrationEntity existing = modelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Model not found with id: " + id));

        existing.setLastHealthCheck(LocalDateTime.now());
        existing.setHealthStatus(existing.getEnabled() ? "HEALTHY" : "DEGRADED");
        return toDto(modelRepository.save(existing));
    }

    @Transactional
    public void deleteModel(Long id) {
        modelRepository.deleteById(id);
    }

    public ModelRegistrationDto toDto(ModelRegistrationEntity e) {
        return ModelRegistrationDto.builder()
                .id(e.getId())
                .modelName(e.getModelName())
                .vendor(e.getVendor())
                .version(e.getVersion())
                .endpoint(e.getEndpoint())
                .apiType(e.getApiType())
                .supportedFeatures(e.getSupportedFeatures())
                .costPer1kInput(e.getCostPer1kInput())
                .costPer1kOutput(e.getCostPer1kOutput())
                .contextWindow(e.getContextWindow())
                .streamingSupport(e.getStreamingSupport())
                .status(e.getStatus())
                .owner(e.getOwner())
                .healthStatus(e.getHealthStatus())
                .enabled(e.getEnabled())
                .lastHealthCheck(e.getLastHealthCheck())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public ModelRegistrationEntity toEntity(ModelRegistrationDto d) {
        return ModelRegistrationEntity.builder()
                .id(d.getId())
                .modelName(d.getModelName())
                .vendor(d.getVendor())
                .version(d.getVersion())
                .endpoint(d.getEndpoint())
                .apiType(d.getApiType())
                .supportedFeatures(d.getSupportedFeatures())
                .costPer1kInput(d.getCostPer1kInput() != null ? d.getCostPer1kInput() : 0.002)
                .costPer1kOutput(d.getCostPer1kOutput() != null ? d.getCostPer1kOutput() : 0.008)
                .contextWindow(d.getContextWindow() != null ? d.getContextWindow() : 128000)
                .streamingSupport(d.getStreamingSupport() != null ? d.getStreamingSupport() : true)
                .status(d.getStatus() != null ? d.getStatus() : "APPROVED")
                .owner(d.getOwner() != null ? d.getOwner() : "Platform AI Team")
                .healthStatus(d.getHealthStatus() != null ? d.getHealthStatus() : "HEALTHY")
                .enabled(d.getEnabled() != null ? d.getEnabled() : true)
                .build();
    }
}
