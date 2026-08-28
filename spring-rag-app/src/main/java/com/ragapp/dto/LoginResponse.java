package com.ragapp.dto;

/**
 * Response returned after a successful organization login.
 *
 * @param mode always {@code ORGANIZATION}
 */
public record LoginResponse(
        String token,
        String username,
        String mode,
        String orgId,
        String orgName,
        String role
) {}
