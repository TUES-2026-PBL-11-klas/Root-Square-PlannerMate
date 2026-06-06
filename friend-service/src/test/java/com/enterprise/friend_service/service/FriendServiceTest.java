package com.enterprise.friend_service.service;

import com.enterprise.friend_service.client.IamClient;
import com.enterprise.friend_service.client.IamUserResponse;
import com.enterprise.friend_service.dto.FriendResponse;
import com.enterprise.friend_service.dto.SendFriendRequest;
import com.enterprise.friend_service.model.Friend;
import com.enterprise.friend_service.repository.FriendRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private IamClient iamClient;

    @InjectMocks
    private FriendService friendService;

    @Test
    void sendFriendRequest_createsPendingRequest() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String requesterEmail = "requester@example.com";
        String bearer = "Bearer test-token";

        SendFriendRequest request = new SendFriendRequest(receiverId);
        IamUserResponse receiver = new IamUserResponse(receiverId, "Receiver", "receiver@example.com", "ACTIVE");

        Friend saved = Friend.builder()
                .id(10L)
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(iamClient.getUserById(receiverId, bearer)).thenReturn(receiver);
        when(friendRepository.existsBetweenUsers(requesterId, receiverId)).thenReturn(false);
        when(friendRepository.save(any(Friend.class))).thenReturn(saved);

        FriendResponse response = friendService.sendFriendRequest(
                requesterId,
                requesterEmail,
                request,
                bearer
        );

        assertEquals("PENDING", response.status());
        assertEquals(requesterId, response.requesterId());
        assertEquals(receiverId, response.receiverId());
        assertEquals("receiver@example.com", response.receiverEmail());
    }

    @Test
    void acceptFriendRequest_marksAccepted() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String bearer = "Bearer test-token";

        Friend friendship = Friend.builder()
                .id(5L)
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(friendRepository.findById(5L)).thenReturn(Optional.of(friendship));
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));
        when(iamClient.getUserById(eq(requesterId), eq(bearer)))
                .thenReturn(new IamUserResponse(requesterId, "Requester", "req@example.com", "ACTIVE"));
        when(iamClient.getUserById(eq(receiverId), eq(bearer)))
                .thenReturn(new IamUserResponse(receiverId, "Receiver", "rec@example.com", "ACTIVE"));

        FriendResponse response = friendService.acceptFriendRequest(receiverId, 5L, bearer);

        assertEquals("ACCEPTED", response.status());
        assertNotNull(friendship.getUpdatedAt());
    }

    @Test
    void rejectFriendRequest_marksRejected() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String bearer = "Bearer test-token";

        Friend friendship = Friend.builder()
                .id(7L)
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(friendRepository.findById(7L)).thenReturn(Optional.of(friendship));
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));
        when(iamClient.getUserById(eq(requesterId), eq(bearer)))
                .thenReturn(new IamUserResponse(requesterId, "Requester", "req@example.com", "ACTIVE"));
        when(iamClient.getUserById(eq(receiverId), eq(bearer)))
                .thenReturn(new IamUserResponse(receiverId, "Receiver", "rec@example.com", "ACTIVE"));

        FriendResponse response = friendService.rejectFriendRequest(receiverId, 7L, bearer);

        assertEquals("REJECTED", response.status());
        assertNotNull(friendship.getUpdatedAt());
    }
}
