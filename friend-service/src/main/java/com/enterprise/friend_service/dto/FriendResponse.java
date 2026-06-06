package com.enterprise.friend_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendResponse(
        Long id,
        UUID requesterId,
        String requesterEmail,
        UUID receiverId,
        String receiverEmail,
        String status,
        LocalDateTime createdAt
) {}
