package com.aiapi.controller;

import com.aiapi.model.PromptRequest;
import com.aiapi.model.PromptResponse;
import com.aiapi.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class PromptController {

    private final AiService aiService;

    public PromptController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<PromptResponse> complete(@RequestBody PromptRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(new PromptResponse(aiService.complete(request.getPrompt().trim())));
    }
}
