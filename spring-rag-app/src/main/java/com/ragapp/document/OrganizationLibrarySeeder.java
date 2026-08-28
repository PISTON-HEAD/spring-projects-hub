package com.ragapp.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.ragapp.auth.OrgAccountService;
import com.ragapp.auth.OrgAccountService.Organization;
import com.ragapp.dto.DocumentUploadResponse;

/**
 * Loads each organization's shared library from bundled seed documents, so
 * members find content already present the first time they sign in.
 *
 * <p>Invoked once by {@link LibraryBootstrap} only when there is no persisted
 * library to restore. Never throws — a failed seed is logged and skipped.
 */
@Component
public class OrganizationLibrarySeeder {

    private static final Logger log = LoggerFactory.getLogger(OrganizationLibrarySeeder.class);

    private final DocumentService documentService;
    private final OrgAccountService orgAccountService;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Value("${app.rag.seed.enabled:true}")
    private boolean seedEnabled;

    public OrganizationLibrarySeeder(DocumentService documentService, OrgAccountService orgAccountService) {
        this.documentService = documentService;
        this.orgAccountService = orgAccountService;
    }

    /** Seeds every organization's library from bundled documents. No-op when disabled. */
    public void seedAll() {
        if (!seedEnabled) {
            return;
        }
        for (Organization org : orgAccountService.organizations()) {
            String scopeKey = "org:" + org.orgId();
            Resource[] resources;
            try {
                resources = resolver.getResources("classpath*:seed/" + org.orgId() + "/*");
            } catch (Exception e) {
                log.warn("No seed documents found for {}: {}", scopeKey, e.getMessage());
                continue;
            }

            int seeded = 0;
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null || filename.isBlank() || !resource.isReadable()) {
                    continue;
                }
                try {
                    DocumentUploadResponse resp = documentService.ingestResource(filename, resource, scopeKey);
                    seeded++;
                    log.info("Seeded '{}' into {} ({} chunks)", filename, scopeKey, resp.totalChunks());
                } catch (Exception e) {
                    log.warn("Failed to seed '{}' into {}: {}", filename, scopeKey, e.getMessage());
                }
            }
            log.info("Organization '{}' library ready with {} seed document(s).", org.orgName(), seeded);
        }
    }
}
