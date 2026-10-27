package com.enterprise.ai.common.exception;

/**
 * Thrown when an audit record, approval task, or configuration entity cannot be resolved.
 */
public class ResourceNotFoundException extends AiGuardianException {

    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("Resource '%s' with identifier '%s' was not found.", resourceName, resourceId),
                "RESOURCE_NOT_FOUND", 404);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
