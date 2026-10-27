package com.enterprise.ai.common.constant;

/**
 * Enterprise AI Guardian - API Route Paths, Header Names, and Protocol Constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    // --- Base Prefixes ---
    public static final String API_V1_PREFIX = "/api/v1";

    // --- Route Endpoints ---
    public static final String ROUTE_AI_CHAT = API_V1_PREFIX + "/ai/chat";
    public static final String ROUTE_SCAN = API_V1_PREFIX + "/scan";
    public static final String ROUTE_SCAN_REDACT = API_V1_PREFIX + "/scan/redact";
    public static final String ROUTE_RISK_ASSESS = API_V1_PREFIX + "/risk/assess";
    public static final String ROUTE_AUDIT_EVENTS = API_V1_PREFIX + "/audit/events";
    public static final String ROUTE_AUDIT_ALERTS = API_V1_PREFIX + "/audit/events/alerts";
    public static final String ROUTE_AUDIT_PENDING = API_V1_PREFIX + "/audit/events/pending";
    public static final String ROUTE_DASHBOARD_OVERVIEW = API_V1_PREFIX + "/dashboard/overview";
    public static final String ROUTE_QUALITY_CHECK = API_V1_PREFIX + "/quality/check";

    // --- Custom Enterprise HTTP Headers ---
    public static final String HEADER_REQUEST_ID = "X-AI-Guardian-Request-ID";
    public static final String HEADER_USER_ID = "X-AI-Guardian-User-ID";
    public static final String HEADER_USER_ROLE = "X-AI-Guardian-User-Role";
    public static final String HEADER_RISK_LEVEL = "X-AI-Guardian-Risk-Level";
    public static final String HEADER_ACTION = "X-AI-Guardian-Action";
    public static final String HEADER_PROCESSING_TIME = "X-AI-Guardian-Processing-Time-Ms";
}
