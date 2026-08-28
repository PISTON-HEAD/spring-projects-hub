package com.ragapp.document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.ragapp.dto.DocumentInfo;
import com.ragapp.dto.DocumentUploadResponse;

/**
 * Unit tests for DocumentService.
 *
 * The SimpleVectorStore and Spring AI internals are mocked so these tests run
 * offline (no OpenAI calls needed).
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final String SCOPE = "ind:test-workspace";

    @Mock
    private SimpleVectorStore vectorStore;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(vectorStore);
        ReflectionTestUtils.setField(documentService, "chunkSize", 800);
        ReflectionTestUtils.setField(documentService, "chunkOverlap", 100);
    }

    // ── ingestDocument ────────────────────────────────────────────────────────

    @Test
    @DisplayName("ingestDocument: returns a response with a valid documentId and filename")
    void ingestDocument_returnsValidResponse() throws IOException {
        MockMultipartFile file = createTextFile("hello.txt",
                "Spring AI is a framework for building AI-powered applications. " +
                "It provides integration with various AI providers including OpenAI.");

        DocumentUploadResponse response = documentService.ingestDocument(file, SCOPE);

        assertThat(response.documentId()).isNotNull().isNotEmpty();
        assertThat(response.filename()).isEqualTo("hello.txt");
        assertThat(response.totalChunks()).isGreaterThan(0);
        assertThat(response.message()).contains("success");
    }

    @Test
    @DisplayName("ingestDocument: calls vectorStore.add with chunks tagged with documentId + scopeKey")
    void ingestDocument_tagsChunksWithDocumentId() throws IOException {
        MockMultipartFile file = createTextFile("doc.txt",
                "This is a test document about Retrieval Augmented Generation, " +
                "also known as RAG. RAG combines information retrieval with LLMs.");

        DocumentUploadResponse response = documentService.ingestDocument(file, SCOPE);

        // Capture what was passed to vectorStore.add
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore, times(1)).add(captor.capture());

        List<Document> storedDocs = captor.getValue();
        assertThat(storedDocs).isNotEmpty();
        storedDocs.forEach(doc -> {
            assertThat(doc.getMetadata().get("documentId")).isEqualTo(response.documentId());
            assertThat(doc.getMetadata().get("scopeKey")).isEqualTo(SCOPE);
        });
    }

    @Test
    @DisplayName("ingestDocument: registers document in the internal store for its scope")
    void ingestDocument_registersDocumentInStore() throws IOException {
        MockMultipartFile file = createTextFile("report.txt", "Annual financial report 2025.");

        DocumentUploadResponse response = documentService.ingestDocument(file, SCOPE);

        assertThat(documentService.documentExists(SCOPE, response.documentId())).isTrue();
    }

    @Test
    @DisplayName("ingestDocument: keeps scopes isolated — a doc is not visible from another scope")
    void ingestDocument_isolatesScopes() throws IOException {
        DocumentUploadResponse response = documentService.ingestDocument(
                createTextFile("secret.txt", "Confidential organization data."), "org:acme");

        assertThat(documentService.documentExists("org:acme", response.documentId())).isTrue();
        assertThat(documentService.documentExists("ind:someone-else", response.documentId())).isFalse();
        assertThat(documentService.listDocuments("ind:someone-else")).isEmpty();
    }

    // ── listDocuments ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("listDocuments: starts empty and grows with each upload in the scope")
    void listDocuments_growsWithEachUpload() throws IOException {
        assertThat(documentService.listDocuments(SCOPE)).isEmpty();

        documentService.ingestDocument(createTextFile("a.txt", "first document content here"), SCOPE);
        assertThat(documentService.listDocuments(SCOPE)).hasSize(1);

        documentService.ingestDocument(createTextFile("b.txt", "second document content here"), SCOPE);
        assertThat(documentService.listDocuments(SCOPE)).hasSize(2);
    }

    @Test
    @DisplayName("listDocuments: returns DocumentInfo with correct filename and chunkCount")
    void listDocuments_returnsCorrectFilename() throws IOException {
        documentService.ingestDocument(createTextFile("my-report.txt",
                "This is a detailed report about machine learning techniques."), SCOPE);

        List<DocumentInfo> docs = documentService.listDocuments(SCOPE);

        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).filename()).isEqualTo("my-report.txt");
        assertThat(docs.get(0).chunks()).isGreaterThan(0);
    }

    // ── documentExists ────────────────────────────────────────────────────────

    @Test
    @DisplayName("documentExists: returns false for unknown documentId")
    void documentExists_returnsFalseForUnknownId() {
        assertThat(documentService.documentExists(SCOPE, "non-existent-id")).isFalse();
    }

    @Test
    @DisplayName("documentExists: returns true after document is ingested")
    void documentExists_returnsTrueAfterIngestion() throws IOException {
        DocumentUploadResponse response = documentService.ingestDocument(
                createTextFile("test.txt", "Some content for document existence test."), SCOPE
        );

        assertThat(documentService.documentExists(SCOPE, response.documentId())).isTrue();
    }

    // ── deleteDocument ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteDocument: removes the document from its scope and returns its info")
    void deleteDocument_removesExistingDocument() throws IOException {
        DocumentUploadResponse response = documentService.ingestDocument(
                createTextFile("to-remove.txt", "Content that will be deleted."), SCOPE);

        var removed = documentService.deleteDocument(SCOPE, response.documentId());

        assertThat(removed).isPresent();
        assertThat(removed.get().filename()).isEqualTo("to-remove.txt");
        assertThat(documentService.documentExists(SCOPE, response.documentId())).isFalse();
        assertThat(documentService.listDocuments(SCOPE)).isEmpty();
        verify(vectorStore, times(1)).delete(any(org.springframework.ai.vectorstore.filter.Filter.Expression.class));
    }

    @Test
    @DisplayName("deleteDocument: returns empty and does not touch the vector store for an unknown id")
    void deleteDocument_unknownId_returnsEmpty() {
        var removed = documentService.deleteDocument(SCOPE, "does-not-exist");

        assertThat(removed).isEmpty();
        verify(vectorStore, never()).delete(any(org.springframework.ai.vectorstore.filter.Filter.Expression.class));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private MockMultipartFile createTextFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
