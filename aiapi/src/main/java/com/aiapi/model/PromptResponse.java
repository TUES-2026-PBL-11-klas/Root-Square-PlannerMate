package com.aiapi.model;

public class PromptResponse {
    // * Raw text response returned by the AI model.
    private String response;

    public PromptResponse() {
    }

    public PromptResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}