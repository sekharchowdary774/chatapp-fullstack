package com.example.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;

    private String content;
    private String timestamp;

    private String status; // SENT / DELIVERED / SEEN

    @Column(columnDefinition = "TEXT")
    private String reactions; // JSON string

    // Delete-for-everyone
    private boolean deleted = false;

    // Edit Message
    private String editedContent;

    // Delete-for-me list
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "message_deleted_for",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_email")
    private List<String> deletedFor = new ArrayList<>();

    // -------------------------
    // 🔥 NEW FIELD: ReplyTo
    // -------------------------
    @Column(columnDefinition = "TEXT")
    private String replyTo;
    /*
       Store reply as JSON:
       {
          "id": 123,
          "sender": "abc@gmail.com",
          "content": "hi bro"
       }
    */
}
