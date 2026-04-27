package com.example.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userA", "userB"})
)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userA;   // ALWAYS smaller alphabetically
    private String userB;   // ALWAYS larger alphabetically
    private String roomId;

    @Transient
    private int unread;

    @Transient
    private String preview;


}
