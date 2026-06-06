package com.aiapi.controller;

import com.aiapi.model.PromptRequest;
import com.aiapi.model.PromptResponse;
import com.aiapi.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final AiService aiService;

    public PromptController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<PromptResponse> generate(@Valid @RequestBody PromptRequest request) {
        return ResponseEntity.ok(aiService.generateResponse(request));
    }
}
