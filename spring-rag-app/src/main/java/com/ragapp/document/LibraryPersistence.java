package com.ragapp.document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragapp.dto.DocumentInfo;

/**
 * File-based persistence for the document library so uploads survive restarts.
 *
 * <p>Two files are written under {@code app.rag.persistence.dir}:
 * <ul>
 *   <li>{@code vector-store.json} — the embeddings + chunk text ({@link SimpleVectorStore#save})</li>
 *   <li>{@code registry.json} — the per-scope document list (filename, chunk count)</li>
 * </ul>
 *
 * <p>All operations are best-effort and synchronized; a failure is logged and
 * never propagated so it cannot break a request or startup.
 */
@Service
public class LibraryPersistence {

    private static final Logger log = LoggerFactory.getLogger(LibraryPersistence.class);
    private static final String VECTOR_FILE = "vector-store.json";
    private static final String REGISTRY_FILE = "registry.json";

    private final SimpleVectorStore vectorStore;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Path dir;
    private final Object lock = new Object();

    public LibraryPersistence(SimpleVectorStore vectorStore,
                              DocumentService documentService,
                              ObjectMapper objectMapper,
                              @Value("${app.rag.persistence.enabled:true}") boolean enabled,
                              @Value("${app.rag.persistence.dir:./data}") String dir) {
        this.vectorStore = vectorStore;
        this.documentService = documentService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.dir = Paths.get(dir);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Restores the vector store and registry from disk. Returns true if a library was loaded. */
    public boolean restore() {
        if (!enabled) {
            return false;
        }
        synchronized (lock) {
            File vectorFile = dir.resolve(VECTOR_FILE).toFile();
            File registryFile = dir.resolve(REGISTRY_FILE).toFile();
            if (!vectorFile.exists() || !registryFile.exists()) {
                return false;
            }
            try {
                vectorStore.load(vectorFile);
                Map<String, Map<String, DocumentInfo>> registry = objectMapper.readValue(
                        registryFile, new TypeReference<Map<String, Map<String, DocumentInfo>>>() {});
                documentService.restoreRegistry(registry);
                int docs = registry.values().stream().mapToInt(Map::size).sum();
                log.info("Restored persisted library from {} ({} document(s)).", dir.toAbsolutePath(), docs);
                return true;
            } catch (Exception e) {
                log.warn("Could not restore persisted library from {}: {}", dir.toAbsolutePath(), e.getMessage());
                return false;
            }
        }
    }

    /** Persists the current vector store and registry to disk. Best-effort. */
    public void persist() {
        if (!enabled) {
            return;
        }
        synchronized (lock) {
            try {
                Files.createDirectories(dir);
                vectorStore.save(dir.resolve(VECTOR_FILE).toFile());
                objectMapper.writeValue(dir.resolve(REGISTRY_FILE).toFile(), documentService.snapshotRegistry());
            } catch (Exception e) {
                log.warn("Could not persist library to {}: {}", dir.toAbsolutePath(), e.getMessage());
            }
        }
    }
}
