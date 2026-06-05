package com.enterprise.friend_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendFriendRequest(@NotNull UUID receiverId) {}
