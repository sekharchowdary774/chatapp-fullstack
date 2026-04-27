package com.example.chatapp.repository;

import com.example.chatapp.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT c FROM ChatRoom c
        WHERE c.userA = :smaller AND c.userB = :larger
    """)
    Optional<ChatRoom> findByUsers(@Param("smaller") String smaller, @Param("larger") String larger);

    // Default method that handles sorting automatically
    default Optional<ChatRoom> findBetween(String u1, String u2) {
        String[] users = {u1, u2};
        Arrays.sort(users);
        return findByUsers(users[0], users[1]);
    }

    @Query("""
        SELECT c FROM ChatRoom c
        WHERE c.userA = :email OR c.userB = :email
    """)
    List<ChatRoom> findAllForUser(@Param("email") String email);
}