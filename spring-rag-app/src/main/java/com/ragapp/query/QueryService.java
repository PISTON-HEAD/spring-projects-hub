package com.ragapp.query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ragapp.document.DocumentService;
import com.ragapp.dto.ChatTurn;
import com.ragapp.dto.QueryRequest;
import com.ragapp.dto.QueryResponse;

@Service
public class QueryService {

    private final SimpleVectorStore vectorStore;
    private final ChatClient chatClient;
    private final DocumentService documentService;
    private final ChatHistoryService chatHistoryService;

    @Value("${app.rag.top-k:4}")
    private int topK;

    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions based on the provided context.
            Use ONLY the information from the context below to answer the question.
            If the context doesn't contain enough information to answer, say so clearly.
            Do not make up information that is not in the context.
            
            Context:
            {context}
            """;

    public QueryService(SimpleVectorStore vectorStore, ChatModel chatModel,
                        DocumentService documentService, ChatHistoryService chatHistoryService) {
        this.vectorStore = vectorStore;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.documentService = documentService;
        this.chatHistoryService = chatHistoryService;
    }

    public QueryResponse query(String scopeKey, String documentId, QueryRequest request, String sessionId) {
        if (!documentService.documentExists(scopeKey, documentId)) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }

        String resolvedSession = resolveSessionId(sessionId);

        String filterExpression = "scopeKey == '" + scopeKey + "' && documentId == '" + documentId + "'";
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.question())
                        .topK(topK)
                        .filterExpression(filterExpression)
                        .build()
        );

        return buildResponse(scopeKey, documentId, resolvedSession, request.question(), relevantDocs);
    }

    public QueryResponse queryAllDocuments(String scopeKey, QueryRequest request, String sessionId) {
        String resolvedSession = resolveSessionId(sessionId);

        String filterExpression = "scopeKey == '" + scopeKey + "'";
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.question())
                        .topK(topK)
                        .filterExpression(filterExpression)
                        .build()
        );

        return buildResponse(scopeKey, "ALL_DOCUMENTS", resolvedSession, request.question(), relevantDocs);
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId != null && !sessionId.isBlank()) ? sessionId : UUID.randomUUID().toString();
    }

    private QueryResponse buildResponse(String scopeKey, String scope, String sessionId, String question, List<Document> relevantDocs) {
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Chat history is namespaced by scope so sessions never leak across tenants.
        String historyKey = scopeKey + "::" + sessionId;

        // If no Gemini key is configured, still return the retrieved passages with a
        // clear message instead of failing — retrieval works, only generation is off.
        if (!geminiEnabled()) {
            List<String> preview = previewChunks(relevantDocs);
            String answer = relevantDocs.isEmpty()
                    ? "No matching content was found in the selected document(s). "
                        + "Also note: AI answers are disabled because GEMINI_API_KEY is not set."
                    : "AI answer generation is disabled (set the GEMINI_API_KEY environment variable "
                        + "and restart to enable it). Here are the most relevant passages I retrieved "
                        + "from your document(s) for this question.";
            return new QueryResponse(sessionId, answer, question, scope, preview);
        }

        // Build history messages (alternating user/assistant for previous turns)
        List<ChatTurn> history = chatHistoryService.getHistory(historyKey);
        List<Message> historyMessages = new ArrayList<>();
        for (ChatTurn turn : history) {
            historyMessages.add(new UserMessage(turn.question()));
            historyMessages.add(new AssistantMessage(turn.answer()));
        }

        // Call Gemini: system prompt (with RAG context) + history + current question
        String answer = chatClient.prompt()
                .system(s -> s.text(SYSTEM_PROMPT).param("context", context))
                .messages(historyMessages)
                .user(question)
                .call()
                .content();

        // Save this turn to the session history
        chatHistoryService.addTurn(historyKey, question, answer);

        List<String> chunks = previewChunks(relevantDocs);

        return new QueryResponse(sessionId, answer, question, scope, chunks);
    }

    private boolean geminiEnabled() {
        return geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.equals("not-configured");
    }

    private List<String> previewChunks(List<Document> relevantDocs) {
        return relevantDocs.stream()
                .map(doc -> {
                    String content = doc.getText();
                    return content.substring(0, Math.min(content.length(), 200)) + "...";
                })
                .toList();
    }
}
