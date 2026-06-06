package com.enterprise.friend_service.repository;

import com.enterprise.friend_service.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("""
	    select (count(f) > 0) from Friend f
	    where (f.requesterId = :userA and f.receiverId = :userB)
	       or (f.requesterId = :userB and f.receiverId = :userA)
	    """)
    boolean existsBetweenUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
	    select f from Friend f
	    where f.status = 'ACCEPTED'
	      and (f.requesterId = :userId or f.receiverId = :userId)
	    """)
    List<Friend> findAcceptedFriends(@Param("userId") UUID userId);

    List<Friend> findAllByReceiverIdAndStatus(UUID receiverId, String status);

    List<Friend> findAllByRequesterIdAndStatus(UUID requesterId, String status);
}
