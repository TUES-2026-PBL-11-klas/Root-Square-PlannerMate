package com.enterprise.friend_service.service;

import com.enterprise.friend_service.client.IamClient;
import com.enterprise.friend_service.client.IamUserResponse;
import com.enterprise.friend_service.dto.FriendResponse;
import com.enterprise.friend_service.dto.FriendSummary;
import com.enterprise.friend_service.dto.SendFriendRequest;
import com.enterprise.friend_service.model.Friend;
import com.enterprise.friend_service.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final IamClient iamClient;

    // * Sends a friend request.
    // ? bearerToken is forwarded to the IAM service to verify the receiver exists.
    @Transactional
    public FriendResponse sendFriendRequest(UUID requesterId, String requesterEmail,
                                            SendFriendRequest request, String bearerToken) {
        UUID receiverId = request.receiverId();

        // ! Prevent self-friending
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        // ! Verify the receiver actually exists in the IAM service before writing anything
        IamUserResponse receiver = iamClient.getUserById(receiverId, bearerToken);

        // ! Prevent duplicate requests in either direction
        if (friendRepository.existsBetweenUsers(requesterId, receiverId)) {
            throw new IllegalStateException("A friend relationship or request already exists with this user.");
        }

        Friend friendship = Friend.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status("PENDING")
                .build();

        Friend saved = friendRepository.save(friendship);

        return new FriendResponse(
                saved.getId(),
                requesterId, requesterEmail,
                receiverId, receiver.email(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    // * Accepts a pending incoming request. Only the receiver may call this.
    @Transactional
    public FriendResponse acceptFriendRequest(UUID callerId, Long friendshipId, String bearerToken) {
        Friend friendship = findById(friendshipId);
        assertIsReceiver(friendship, callerId);
        assertIsPending(friendship);

        friendship.setStatus("ACCEPTED");
        friendship.setUpdatedAt(LocalDateTime.now());
        return toResponse(friendRepository.save(friendship), bearerToken);
    }

    // * Rejects a pending incoming request. Only the receiver may call this.
    @Transactional
    public FriendResponse rejectFriendRequest(UUID callerId, Long friendshipId, String bearerToken) {
        Friend friendship = findById(friendshipId);
        assertIsReceiver(friendship, callerId);
        assertIsPending(friendship);

        friendship.setStatus("REJECTED");
        friendship.setUpdatedAt(LocalDateTime.now());
        return toResponse(friendRepository.save(friendship), bearerToken);
    }

    // * Cancels a PENDING request the caller originally sent.
    @Transactional
    public void cancelFriendRequest(UUID callerId, Long friendshipId) {
        Friend friendship = findById(friendshipId);
        assertIsRequester(friendship, callerId);
        assertIsPending(friendship);
        friendRepository.delete(friendship);
    }

    // * Removes an accepted friendship. Either party may call this.
    @Transactional
    public void removeFriend(UUID callerId, Long friendshipId) {
        Friend friendship = findById(friendshipId);
        assertIsParticipant(friendship, callerId);
        if (!"ACCEPTED".equals(friendship.getStatus())) {
            throw new IllegalStateException("Cannot remove a friendship that is not accepted.");
        }
        friendRepository.delete(friendship);
    }

    // * Returns all accepted friends of the caller as a lightweight list.
    @Transactional(readOnly = true)
    public List<FriendSummary> getAcceptedFriends(UUID callerId, String bearerToken) {
        return friendRepository.findAcceptedFriends(callerId).stream()
                .map(f -> {
                    UUID otherId = f.getRequesterId().equals(callerId)
                            ? f.getReceiverId()
                            : f.getRequesterId();

                    // * Resolve the other user's email from the IAM service
                    IamUserResponse other = iamClient.getUserById(otherId, bearerToken);
                    return new FriendSummary(f.getId(), otherId, other.email());
                })
                .toList();
    }

    // * Incoming PENDING requests for the caller.
    @Transactional(readOnly = true)
    public List<FriendResponse> getIncomingRequests(UUID callerId, String bearerToken) {
        return friendRepository.findAllByReceiverIdAndStatus(callerId, "PENDING")
                .stream().map(f -> toResponse(f, bearerToken)).toList();
    }

    // * Outgoing PENDING requests sent by the caller.
    @Transactional(readOnly = true)
    public List<FriendResponse> getOutgoingRequests(UUID callerId, String bearerToken) {
        return friendRepository.findAllByRequesterIdAndStatus(callerId, "PENDING")
                .stream().map(f -> toResponse(f, bearerToken)).toList();
    }

    // ─── Private Helpers ────────────────────────────────────────────────────────

    private Friend findById(Long id) {
        return friendRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Friendship not found: " + id
            ));
    }

    private void assertIsReceiver(Friend f, UUID callerId) {
        if (!f.getReceiverId().equals(callerId)) {
            throw new SecurityException("You are not authorised to respond to this friend request.");
        }
    }

    private void assertIsRequester(Friend f, UUID callerId) {
        if (!f.getRequesterId().equals(callerId)) {
            throw new SecurityException("You are not authorised to cancel this friend request.");
        }
    }

    private void assertIsParticipant(Friend f, UUID callerId) {
        if (!f.getRequesterId().equals(callerId) && !f.getReceiverId().equals(callerId)) {
            throw new SecurityException("You are not part of this friendship.");
        }
    }

    private void assertIsPending(Friend f) {
        if (!"PENDING".equals(f.getStatus())) {
            throw new IllegalStateException(
                    "This request is already " + f.getStatus().toLowerCase() + ".");
        }
    }

    // * Resolves both user emails from IAM then builds the response DTO.
    private FriendResponse toResponse(Friend f, String bearerToken) {
        IamUserResponse requester = iamClient.getUserById(f.getRequesterId(), bearerToken);
        IamUserResponse receiver  = iamClient.getUserById(f.getReceiverId(),  bearerToken);
        return new FriendResponse(
                f.getId(),
                f.getRequesterId(), requester.email(),
                f.getReceiverId(),  receiver.email(),
                f.getStatus(),
                f.getCreatedAt()
        );
    }
}
