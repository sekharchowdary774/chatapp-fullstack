package com.example.chatapp.repository;

import com.example.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /* ---------------------------------------------------------
       FETCH VISIBLE CHAT HISTORY (EXCLUDES deletedFor)
       --------------------------------------------------------- */
    @Query("""
        SELECT m FROM Message m
        WHERE 
            ((m.sender = :s1 AND m.receiver = :r1)
            OR 
            (m.sender = :s2 AND m.receiver = :r2))
            AND (:viewer NOT IN elements(m.deletedFor))
        ORDER BY m.id ASC
    """)
    List<Message> findVisibleChatHistory(
            @Param("s1") String s1,
            @Param("r1") String r1,
            @Param("s2") String s2,
            @Param("r2") String r2,
            @Param("viewer") String viewer
    );


    /* ---------------------------------------------------------
       MARK SEEN
       --------------------------------------------------------- */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Message m 
        SET m.status = 'SEEN' 
        WHERE m.sender = :sender 
          AND m.receiver = :receiver 
          AND (m.status <> 'SEEN' OR m.status IS NULL)
    """)
    void markSeen(
            @Param("sender") String sender,
            @Param("receiver") String receiver
    );


    /* ---------------------------------------------------------
       UNREAD COUNT FOR SIDEBAR (CORRECT)
       --------------------------------------------------------- */
    @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.receiver = :user
          AND m.sender = :sender
          AND m.status <> 'SEEN'
    """)
    long countUnreadFromUser(
            @Param("user") String user,
            @Param("sender") String sender
    );


    /* ---------------------------------------------------------
       LAST MESSAGE BETWEEN TWO USERS (CORRECT)
       --------------------------------------------------------- */
    @Query("""
    SELECT m FROM Message m
    WHERE 
        (m.sender = :u1 AND m.receiver = :u2)
        OR
        (m.sender = :u2 AND m.receiver = :u1)
    ORDER BY m.id DESC
""")
    List<Message> getLastMessage(
            @Param("u1") String u1,
            @Param("u2") String u2
    );



    /* ---------------------------------------------------------
       GLOBAL UNREAD FOR BADGE (OPTIONAL)
       --------------------------------------------------------- */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :email AND m.status <> 'SEEN'")
    long getUnreadCount(@Param("email") String email);
}
