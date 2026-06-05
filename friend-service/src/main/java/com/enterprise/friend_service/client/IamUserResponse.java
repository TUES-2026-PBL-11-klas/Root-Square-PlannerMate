package com.enterprise.friend_service.client;

import java.util.UUID;

public record IamUserResponse(
        UUID id,
        String name,
        String email,
        String status
) {}
