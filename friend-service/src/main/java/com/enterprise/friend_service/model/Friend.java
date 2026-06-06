package com.enterprise.friend_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// * This entity stores only UUIDs referencing users in the IAM service.
// * There is no JPA relationship to a User entity — users live in a different database.
@Entity
@Table(
    name = "friends",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_friend_pair",
        columnNames = {"user_id_requester", "user_id_receiver"}
    )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ! IMPORTANT: These are UUID references to the IAM service's users table.
    // ! They are plain columns — not foreign keys — because the users table
    // ! lives in a completely separate database.
    @Column(name = "user_id_requester", nullable = false, updatable = false)
    private UUID requesterId;

    @Column(name = "user_id_receiver", nullable = false, updatable = false)
    private UUID receiverId;

    // * Status: PENDING → ACCEPTED or REJECTED
    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
