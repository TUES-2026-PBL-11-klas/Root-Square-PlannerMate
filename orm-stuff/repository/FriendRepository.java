package com.rootsquare.planyourday.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rootsquare.planyourday.model.Friend;

public interface FriendRepository extends JpaRepository<Friend, Integer> {
}