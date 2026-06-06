package com.enterprise.friend_service.controller;

import com.enterprise.friend_service.client.IamClient;
import com.enterprise.friend_service.client.IamUserResponse;
import com.enterprise.friend_service.dto.FriendResponse;
import com.enterprise.friend_service.dto.SendFriendRequest;
import com.enterprise.friend_service.service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
@AutoConfigureMockMvc(addFilters = false)
class FriendControllerTest {

    private static final String BEARER = "Bearer test-token";
    private static final String CALLER_EMAIL = "caller@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendService friendService;

    @MockBean
    private IamClient iamClient;

    @Test
    @WithMockUser(username = CALLER_EMAIL)
    void sendFriendRequest_returnsResponse() throws Exception {
        UUID callerId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        when(iamClient.getUserByEmail(CALLER_EMAIL, BEARER))
                .thenReturn(new IamUserResponse(callerId, "Caller", CALLER_EMAIL, "ACTIVE"));

        FriendResponse response = new FriendResponse(
                1L,
                callerId,
                CALLER_EMAIL,
                receiverId,
                "receiver@example.com",
                "PENDING",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(friendService.sendFriendRequest(eq(callerId), eq(CALLER_EMAIL), any(), eq(BEARER)))
                .thenReturn(response);

        String json = objectMapper.writeValueAsString(new SendFriendRequest(receiverId));

        mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.receiverId").value(receiverId.toString()));

        verify(friendService).sendFriendRequest(eq(callerId), eq(CALLER_EMAIL), any(), eq(BEARER));
    }

    @Test
    @WithMockUser(username = CALLER_EMAIL)
    void acceptFriendRequest_returnsResponse() throws Exception {
        UUID callerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        when(iamClient.getUserByEmail(CALLER_EMAIL, BEARER))
                .thenReturn(new IamUserResponse(callerId, "Caller", CALLER_EMAIL, "ACTIVE"));

        FriendResponse response = new FriendResponse(
                2L,
                otherId,
                "other@example.com",
                callerId,
                CALLER_EMAIL,
                "ACCEPTED",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(friendService.acceptFriendRequest(callerId, 2L, BEARER))
                .thenReturn(response);

        mockMvc.perform(post("/api/friends/requests/2/accept")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.id").value(2));

        verify(friendService).acceptFriendRequest(callerId, 2L, BEARER);
    }

    @Test
    @WithMockUser(username = CALLER_EMAIL)
    void rejectFriendRequest_returnsResponse() throws Exception {
        UUID callerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        when(iamClient.getUserByEmail(CALLER_EMAIL, BEARER))
                .thenReturn(new IamUserResponse(callerId, "Caller", CALLER_EMAIL, "ACTIVE"));

        FriendResponse response = new FriendResponse(
                3L,
                otherId,
                "other@example.com",
                callerId,
                CALLER_EMAIL,
                "REJECTED",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(friendService.rejectFriendRequest(callerId, 3L, BEARER))
                .thenReturn(response);

        mockMvc.perform(post("/api/friends/requests/3/reject")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.id").value(3));

        verify(friendService).rejectFriendRequest(callerId, 3L, BEARER);
    }
}
