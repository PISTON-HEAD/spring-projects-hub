package com.ragapp.document;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ragapp.dto.DocumentInfo;
import com.ragapp.dto.DocumentUploadResponse;

@Service
public class DocumentService {

    private final SimpleVectorStore vectorStore;

    @Value("${app.rag.chunk-size:800}")
    private int chunkSize;

    @Value("${app.rag.chunk-overlap:100}")
    private int chunkOverlap;

    // Tracks uploaded documents per isolation scope: scopeKey -> (documentId -> DocumentInfo)
    private final Map<String, Map<String, DocumentInfo>> docsByScope = new ConcurrentHashMap<>();

    public DocumentService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Ingests an uploaded file into the caller's isolation scope.
     */
    public DocumentUploadResponse ingestDocument(MultipartFile file, String scopeKey) throws IOException {
        return ingest(file.getOriginalFilename(), new InputStreamResource(file.getInputStream()), scopeKey);
    }

    /**
     * Ingests a document from any {@link Resource} (e.g. a bundled seed file) into a scope.
     */
    public DocumentUploadResponse ingestResource(String filename, Resource resource, String scopeKey) {
        return ingest(filename, resource, scopeKey);
    }

    /**
     * Parses, chunks, embeds and stores a document inside a scope.
     * Every chunk is tagged with both {@code documentId} and {@code scopeKey} so that
     * retrieval can never cross a tenant boundary.
     */
    private DocumentUploadResponse ingest(String filename, Resource resource, String scopeKey) {
        String documentId = UUID.randomUUID().toString();

        // 1. Parse document using Tika
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocuments = reader.get();

        // 2. Split into chunks
        TokenTextSplitter splitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true, List.of('.', '?', '!', ';'));
        List<Document> chunks = splitter.apply(rawDocuments);

        // 3. Tag each chunk with documentId + scopeKey for filtered retrieval
        chunks.forEach(chunk -> {
            chunk.getMetadata().put("documentId", documentId);
            chunk.getMetadata().put("scopeKey", scopeKey);
        });

        // 4. Embed and store in vector store
        vectorStore.add(chunks);

        // 5. Track document within its scope
        DocumentInfo info = new DocumentInfo(documentId, filename, chunks.size());
        docsByScope.computeIfAbsent(scopeKey, k -> new ConcurrentHashMap<>()).put(documentId, info);

        return new DocumentUploadResponse(
                documentId,
                filename,
                chunks.size(),
                "Document indexed successfully"
        );
    }

    public List<DocumentInfo> listDocuments(String scopeKey) {
        return List.copyOf(docsByScope.getOrDefault(scopeKey, Map.of()).values());
    }

    public boolean documentExists(String scopeKey, String documentId) {
        return docsByScope.getOrDefault(scopeKey, Map.of()).containsKey(documentId);
    }

    /**
     * Removes a document (all its chunks) from a scope's library.
     * Returns the removed document info, or empty if it did not exist in that scope.
     */
    public java.util.Optional<DocumentInfo> deleteDocument(String scopeKey, String documentId) {
        Map<String, DocumentInfo> scoped = docsByScope.get(scopeKey);
        if (scoped == null || !scoped.containsKey(documentId)) {
            return java.util.Optional.empty();
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        vectorStore.delete(b.and(b.eq("scopeKey", scopeKey), b.eq("documentId", documentId)).build());
        return java.util.Optional.ofNullable(scoped.remove(documentId));
    }

    /** Snapshot of the document registry (scopeKey -> documentId -> info) for persistence. */
    public Map<String, Map<String, DocumentInfo>> snapshotRegistry() {
        Map<String, Map<String, DocumentInfo>> copy = new LinkedHashMap<>();
        docsByScope.forEach((scope, docs) -> copy.put(scope, new LinkedHashMap<>(docs)));
        return copy;
    }

    /** Replaces the in-memory registry with a previously persisted snapshot. */
    public void restoreRegistry(Map<String, Map<String, DocumentInfo>> data) {
        docsByScope.clear();
        data.forEach((scope, docs) -> docsByScope.put(scope, new ConcurrentHashMap<>(docs)));
    }
}
