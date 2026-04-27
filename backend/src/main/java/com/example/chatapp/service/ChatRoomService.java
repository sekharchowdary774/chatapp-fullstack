package com.example.chatapp.service;

import com.example.chatapp.entity.ChatRoom;
import com.example.chatapp.entity.Message;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    /* ---------------------------------------------------------
            GET OR CREATE ROOM
       --------------------------------------------------------- */
    @Transactional
    public String getRoomId(String user1, String user2, boolean createIfMissing) {

        String[] arr = {user1, user2};
        Arrays.sort(arr);
        String userA = arr[0];
        String userB = arr[1];

        Optional<ChatRoom> existing = chatRoomRepository.findBetween(userA, userB);
        if (existing.isPresent()) {
            return existing.get().getRoomId();
        }

        if (!createIfMissing) return null;

        String roomId = userA + "_" + userB;

        ChatRoom newRoom = ChatRoom.builder()
                .userA(userA)
                .userB(userB)
                .roomId(roomId)
                .build();

        chatRoomRepository.save(newRoom);
        return roomId;
    }

    /* ---------------------------------------------------------
            SIDEBAR ROOM LIST (FINAL FIXED VERSION)
       --------------------------------------------------------- */
    public List<ChatRoom> getUserRooms(String email) {

        List<ChatRoom> rooms = chatRoomRepository.findAllForUser(email);

        for (ChatRoom room : rooms) {

            String other = room.getUserA().equals(email)
                    ? room.getUserB()
                    : room.getUserA();

            /* ---------- UNREAD COUNT ---------- */
            long unread = messageRepository.countUnreadFromUser(email, other);
            room.setUnread((int) unread);

            /* ---------- LAST MESSAGE PREVIEW ---------- */
            List<Message> lastList = messageRepository.getLastMessage(email, other);

            Message last = lastList.isEmpty() ? null : lastList.get(0);

            if (last == null) {
                room.setPreview("");
            }
            else if (last.isDeleted()) {
                room.setPreview("This message was deleted");
            }
            else if (last.getContent() != null && last.getContent().startsWith("http")) {
                room.setPreview("[file]");
            }
            else {
                room.setPreview(last.getContent());
            }
        }

        return rooms;
    }
}
