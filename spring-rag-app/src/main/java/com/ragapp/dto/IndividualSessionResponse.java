package com.ragapp.dto;

/**
 * Response returned when a visitor starts an anonymous individual session.
 * The {@code workspaceId} scopes every document and query to that private session.
 */
public record IndividualSessionResponse(String token, String workspaceId, String mode) {
}
