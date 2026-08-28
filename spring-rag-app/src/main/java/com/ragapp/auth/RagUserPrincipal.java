package com.ragapp.auth;

/**
 * Authenticated principal that carries the data-isolation scope for a request.
 *
 * <p>{@code scopeKey} is the tenant boundary used to filter the vector store and
 * document registry:
 * <ul>
 *   <li>{@code org:<orgId>} — a shared organization knowledge base</li>
 *   <li>{@code ind:<workspaceId>} — a private individual workspace</li>
 * </ul>
 */
public record RagUserPrincipal(String username, String scopeKey, String role, String displayName) {
}
