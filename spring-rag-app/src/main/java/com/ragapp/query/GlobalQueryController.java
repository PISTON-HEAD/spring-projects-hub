package com.ragapp.query;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ragapp.auth.RagUserPrincipal;
import com.ragapp.dto.QueryRequest;
import com.ragapp.dto.QueryResponse;

import jakarta.validation.Valid;

/**
 * Handles cross-document queries — searches across ALL documents in the caller's scope.
 *
 * Use this when you don't know which document has the answer,
 * or when you want the system to find the best match from everything in the scope.
 *
 * POST /api/query  { "question": "What are the payment terms?" }
 * Pass X-Session-Id header to maintain conversation history across requests.
 */
@RestController
@RequestMapping("/api/query")
public class GlobalQueryController {

    private final QueryService queryService;

    public GlobalQueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<QueryResponse> queryAllDocuments(
            @Valid @RequestBody QueryRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @AuthenticationPrincipal RagUserPrincipal principal
    ) {
        QueryResponse response = queryService.queryAllDocuments(principal.scopeKey(), request, sessionId);
        return ResponseEntity.ok(response);
    }
}
