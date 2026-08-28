package com.ragapp.document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ragapp.auth.RagUserPrincipal;
import com.ragapp.dto.DocumentInfo;
import com.ragapp.dto.DocumentUploadResponse;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final LibraryPersistence libraryPersistence;

    public DocumentController(DocumentService documentService, LibraryPersistence libraryPersistence) {
        this.documentService = documentService;
        this.libraryPersistence = libraryPersistence;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal RagUserPrincipal principal) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        DocumentUploadResponse response = documentService.ingestDocument(file, principal.scopeKey());
        libraryPersistence.persist(); // survive restarts
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentInfo>> listDocuments(
            @AuthenticationPrincipal RagUserPrincipal principal) {
        return ResponseEntity.ok(documentService.listDocuments(principal.scopeKey()));
    }

    /**
     * Removes a document from the caller's scope. Restricted to admins / individuals
     * (see SecurityConfig — org members receive 403).
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String documentId,
            @AuthenticationPrincipal RagUserPrincipal principal) {
        return documentService.deleteDocument(principal.scopeKey(), documentId)
                .<ResponseEntity<Map<String, Object>>>map(info -> {
                    libraryPersistence.persist();
                    return ResponseEntity.ok(Map.of(
                            "documentId", documentId,
                            "filename", info.filename(),
                            "message", "Document removed"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
