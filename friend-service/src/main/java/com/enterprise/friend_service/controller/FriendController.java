package com.enterprise.friend_service.controller;

import com.enterprise.friend_service.client.IamClient;
import com.enterprise.friend_service.client.IamUserResponse;
import com.enterprise.friend_service.dto.FriendResponse;
import com.enterprise.friend_service.dto.FriendSummary;
import com.enterprise.friend_service.dto.SendFriendRequest;
import com.enterprise.friend_service.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final IamClient iamClient;

    // GET /api/friends — accepted friends list
    @GetMapping
    public ResponseEntity<List<FriendSummary>> getMyFriends(HttpServletRequest request) {
        String bearer = extractBearer(request);
        IamUserResponse caller = resolveCaller(bearer);
        return ResponseEntity.ok(friendService.getAcceptedFriends(caller.id(), bearer));
    }

    // GET /api/friends/requests/incoming
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendResponse>> getIncomingRequests(HttpServletRequest request) {
        String bearer = extractBearer(request);
        IamUserResponse caller = resolveCaller(bearer);
        return ResponseEntity.ok(friendService.getIncomingRequests(caller.id(), bearer));
    }

    // GET /api/friends/requests/outgoing
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendResponse>> getOutgoingRequests(HttpServletRequest request) {
        String bearer = extractBearer(request);
        IamUserResponse caller = resolveCaller(bearer);
        return ResponseEntity.ok(friendService.getOutgoingRequests(caller.id(), bearer));
    }

    // POST /api/friends/requests — send a friend request
    @PostMapping("/requests")
    public ResponseEntity<FriendResponse> sendFriendRequest(
            @RequestBody @Valid SendFriendRequest body,
            HttpServletRequest request) {

        String bearer = extractBearer(request);
        IamUserResponse caller = resolveCaller(bearer);
        return ResponseEntity.ok(
                friendService.sendFriendRequest(caller.id(), caller.email(), body, bearer));
    }

    // POST /api/friends/requests/{id}/accept — receiver accepts
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<FriendResponse> acceptRequest(
            @PathVariable Long id, HttpServletRequest request) {

        String bearer = extractBearer(request);
        UUID callerId = resolveCaller(bearer).id();
        return ResponseEntity.ok(friendService.acceptFriendRequest(callerId, id, bearer));
    }

    // POST /api/friends/requests/{id}/reject — receiver rejects
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<FriendResponse> rejectRequest(
            @PathVariable Long id, HttpServletRequest request) {

        String bearer = extractBearer(request);
        UUID callerId = resolveCaller(bearer).id();
        return ResponseEntity.ok(friendService.rejectFriendRequest(callerId, id, bearer));
    }

    // DELETE /api/friends/requests/{id} — requester cancels pending request
    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long id, HttpServletRequest request) {

        String bearer = extractBearer(request);
        UUID callerId = resolveCaller(bearer).id();
        friendService.cancelFriendRequest(callerId, id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/friends/{id} — either party removes accepted friendship
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long id, HttpServletRequest request) {

        String bearer = extractBearer(request);
        UUID callerId = resolveCaller(bearer).id();
        friendService.removeFriend(callerId, id);
        return ResponseEntity.noContent().build();
    }

    // ─── Private Helpers ────────────────────────────────────────────────────────

    // * Pulls the raw "Bearer <token>" string from the incoming request header.
    // ! The filter already validated the token — we just need to forward it to IAM.
    private String extractBearer(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    // * Resolves the caller's full user record (including UUID) from the IAM service.
    // ? The email in the SecurityContext came from the JWT subject;
    // ? we forward the token so IAM can verify and return the full profile.
    private IamUserResponse resolveCaller(String bearerToken) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return iamClient.getUserByEmail(email, bearerToken);
    }
}
