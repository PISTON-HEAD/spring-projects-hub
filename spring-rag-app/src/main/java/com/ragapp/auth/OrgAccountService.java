package com.ragapp.auth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Seed registry of organization accounts. Maps a login username to the
 * organization it belongs to and the role it holds inside that organization.
 *
 * <p>Passwords are verified by Spring Security (see {@code UserConfig}); this
 * service resolves the organization identity, display name and role that back
 * the {@code org:<orgId>} data-isolation scope.
 *
 * <p>Roles: {@code ORG_ADMIN} may upload documents to the shared library;
 * {@code ORG_MEMBER} may only read and ask questions.
 */
@Service
public class OrgAccountService {

    /** An organization login: which tenant it maps to and the role it holds. */
    public record OrgAccount(String username, String orgId, String orgName, String role) {
    }

    /** A distinct organization (used to seed and list tenants). */
    public record Organization(String orgId, String orgName) {
    }

    private final Map<String, OrgAccount> byUsername = new LinkedHashMap<>();

    public OrgAccountService() {
        register(new OrgAccount("acme-admin", "acme", "Acme Corporation", "ORG_ADMIN"));
        register(new OrgAccount("acme", "acme", "Acme Corporation", "ORG_MEMBER"));
        register(new OrgAccount("globex-admin", "globex", "Globex Industries", "ORG_ADMIN"));
        register(new OrgAccount("globex", "globex", "Globex Industries", "ORG_MEMBER"));
    }

    private void register(OrgAccount account) {
        byUsername.put(account.username(), account);
    }

    public Optional<OrgAccount> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    /** Distinct organizations, in registration order. */
    public List<Organization> organizations() {
        Map<String, Organization> distinct = new LinkedHashMap<>();
        for (OrgAccount a : byUsername.values()) {
            distinct.putIfAbsent(a.orgId(), new Organization(a.orgId(), a.orgName()));
        }
        return List.copyOf(distinct.values());
    }
}
