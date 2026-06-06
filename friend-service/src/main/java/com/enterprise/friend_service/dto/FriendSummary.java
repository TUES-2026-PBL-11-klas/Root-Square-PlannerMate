package com.enterprise.friend_service.dto;

import java.util.UUID;

public record FriendSummary(
        Long id,
        UUID userId,
        String email
) {}
