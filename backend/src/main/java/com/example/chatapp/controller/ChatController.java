package com.example.chatapp.controller;

import com.example.chatapp.dto.OnlineEvent;
import com.example.chatapp.dto.TypingEvent;
import com.example.chatapp.entity.ChatRoom;
import com.example.chatapp.entity.Message;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.CloudinaryService;
import com.example.chatapp.service.OnlineService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final CloudinaryService cloudinaryService;
    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineService onlineService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* -------------------------------------------------------
                     ROOM / HISTORY
    ------------------------------------------------------- */

    @GetMapping("/room/{sender}/{receiver}")
    public ResponseEntity<Map<String, String>> getOrCreateRoom(
            @PathVariable String sender,
            @PathVariable String receiver) {

        String roomId = chatRoomService.getRoomId(sender, receiver, true);

        if (roomId == null || roomId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create room"));
        }

        return ResponseEntity.ok(Map.of("roomId", roomId));
    }

    // ✅ FIXED VERSION (THE ONLY FIX!)
    @GetMapping("/{sender}/{receiver}")
    public List<Message> getChatHistory(
            @PathVariable String sender,
            @PathVariable String receiver) {

        List<Message> list = messageRepository.findVisibleChatHistory(
                sender, receiver,      // outgoing messages
                receiver, sender,      // incoming messages (FIXED)
                sender                 // viewer
        );

        list.forEach(m -> {
            if (m.getReactions() == null || m.getReactions().trim().isEmpty()) {
                m.setReactions("{}");
            }
            try {
                objectMapper.readTree(m.getReactions());
            } catch (Exception e) {
                m.setReactions("{}");
            }
        });

        return list;
    }

    /* -------------------------------------------------------
                     ROOM LIST NORMALIZED
    ------------------------------------------------------- */

    @GetMapping("/rooms/{email}")
    public List<Map<String, Object>> getUserRooms(@PathVariable String email) {

        List<ChatRoom> rooms = chatRoomService.getUserRooms(email);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatRoom room : rooms) {

            String other = room.getUserA().equals(email)
                    ? room.getUserB()
                    : room.getUserA();

            Map<String, Object> map = new HashMap<>();
            map.put("roomId", room.getRoomId());
            map.put("receiver", other);
            map.put("preview", room.getPreview());
            map.put("unread", room.getUnread());

            result.add(map);
        }

        return result;
    }

    /* -------------------------------------------------------
                     ONLINE STATUS
    ------------------------------------------------------- */

    @GetMapping("/online")
    public Map<String, Object> getOnlineUsers() {
        return Map.of("users", onlineService.getOnlineUsers());
    }

    @GetMapping("/lastseen/{email}")
    public Map<String, String> getLastSeen(@PathVariable String email) {
        return Map.of("lastSeen", onlineService.getLastSeen().getOrDefault(email, ""));
    }

    /* -------------------------------------------------------
                   SEND PRIVATE MESSAGE
    ------------------------------------------------------- */

    @MessageMapping("/private-message")
    public void sendPrivate(@Payload Map<String, Object> payload) {
        try {
            String sender = (String) payload.get("sender");
            String receiver = (String) payload.get("receiver");
            String content = (String) payload.get("content");

            Object replyObj = payload.get("replyTo");
            String replyJson = replyObj != null ? objectMapper.writeValueAsString(replyObj) : null;

            String roomId = chatRoomService.getRoomId(sender, receiver, true);

            Message msg = new Message();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(content);
            msg.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            msg.setStatus("SENT");
            msg.setReplyTo(replyJson);

            msg = messageRepository.saveAndFlush(msg);

            messagingTemplate.convertAndSend("/topic/room." + roomId, msg);

            msg.setStatus("DELIVERED");
            messageRepository.save(msg);

            messagingTemplate.convertAndSend(
                    "/topic/unread.update",
                    Map.of("receiver", receiver, "sender", sender)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------
                        TYPING
    ------------------------------------------------------- */

    @MessageMapping("/typing")
    public void typing(TypingEvent evt) {
        String roomId = chatRoomService.getRoomId(evt.getSender(), evt.getReceiver(), false);
        if (roomId != null) {
            messagingTemplate.convertAndSend("/topic/typing." + roomId, evt);
        }
    }

    /* -------------------------------------------------------
                        ONLINE / OFFLINE
    ------------------------------------------------------- */

    @MessageMapping("/online.register")
    public void registerOnline(OnlineEvent evt) {
        onlineService.markOnline(evt.getEmail());
        messagingTemplate.convertAndSend("/topic/online",
                Map.of("email", evt.getEmail(), "online", true));

        messagingTemplate.convertAndSend("/topic/unread.refresh",
                Map.of("email", evt.getEmail(), "refresh", true));
    }

    @MessageMapping("/online.unregister")
    public void unregisterOnline(OnlineEvent evt) {
        onlineService.markOffline(evt.getEmail());
        messagingTemplate.convertAndSend("/topic/online",
                Map.of("email", evt.getEmail(), "online", false));
    }

    /* -------------------------------------------------------
                         SEEN
    ------------------------------------------------------- */

    @PutMapping("/seen/{sender}/{receiver}")
    public void markSeen(@PathVariable String sender, @PathVariable String receiver) {
        messageRepository.markSeen(sender, receiver);
        messagingTemplate.convertAndSend("/topic/seen." + sender, Map.of("from", receiver));
    }

    /* -------------------------------------------------------
                        UNREAD COUNT
    ------------------------------------------------------- */

    @GetMapping("/unread/{email}")
    public Map<String, Long> getUnread(@PathVariable String email) {
        return Map.of("unread", messageRepository.getUnreadCount(email));
    }

    @GetMapping("/unread/{user}/{sender}")
    public Map<String, Long> getUnreadFromSender(
            @PathVariable String user,
            @PathVariable String sender) {
        return Map.of("unread", messageRepository.countUnreadFromUser(user, sender));
    }

    /* -------------------------------------------------------
                       FILE UPLOAD
    ------------------------------------------------------- */

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = cloudinaryService.uploadFile(file);
        return Map.of("url", url);
    }

    /* -------------------------------------------------------
                         REACTIONS
    ------------------------------------------------------- */

    @MessageMapping("/react")
    public void handleReaction(@Payload Map<String, String> data) {
        try {
            String messageId = data.get("messageId");
            String emoji = data.get("emoji");
            String userEmail = data.get("userEmail");

            Optional<Message> optionalMsg = messageRepository.findById(Long.parseLong(messageId));
            if (optionalMsg.isEmpty()) return;

            Message msg = optionalMsg.get();

            Map<String, List<String>> reactions =
                    msg.getReactions() == null || msg.getReactions().isEmpty()
                            ? new HashMap<>()
                            : objectMapper.readValue(msg.getReactions(), new TypeReference<>() {});

            List<String> users = reactions.getOrDefault(emoji, new ArrayList<>());

            if (users.contains(userEmail)) users.remove(userEmail);
            else users.add(userEmail);

            reactions.put(emoji, users);
            msg.setReactions(objectMapper.writeValueAsString(reactions));

            messageRepository.save(msg);

            Map<String, Object> event = new HashMap<>();
            event.put("messageId", msg.getId());
            event.put("emoji", emoji);
            event.put("users", users);

            messagingTemplate.convertAndSend("/topic/reaction." + msg.getSender(), event);
            messagingTemplate.convertAndSend("/topic/reaction." + msg.getReceiver(), event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------
                        EDIT MESSAGE
    ------------------------------------------------------- */

    @PutMapping("/edit/{messageId}")
    public void editMessage(@PathVariable Long messageId, @RequestBody Map<String, String> body) {
        try {
            Optional<Message> optional = messageRepository.findById(messageId);
            if (optional.isEmpty()) return;

            Message msg = optional.get();
            String edited = body.get("editedContent");

            msg.setEditedContent(edited);
            messageRepository.save(msg);

            messagingTemplate.convertAndSend("/topic/edit." + msg.getSender(),
                    Map.of("messageId", msg.getId(), "editedContent", edited));

            messagingTemplate.convertAndSend("/topic/edit." + msg.getReceiver(),
                    Map.of("messageId", msg.getId(), "editedContent", edited));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------
                    DELETE FOR ME
    ------------------------------------------------------- */

    @PutMapping("/deleteForMe/{messageId}/{user}")
    public void deleteForMe(@PathVariable Long messageId, @PathVariable String user) {
        Optional<Message> optional = messageRepository.findById(messageId);
        if (optional.isEmpty()) return;

        Message msg = optional.get();

        List<String> deletedFor =
                msg.getDeletedFor() != null ? msg.getDeletedFor() : new ArrayList<>();

        if (!deletedFor.contains(user)) deletedFor.add(user);

        msg.setDeletedFor(deletedFor);
        messageRepository.save(msg);

        messagingTemplate.convertAndSend("/topic/deleteForMe." + user,
                Map.of("messageId", msg.getId(), "deletedFor", deletedFor));
    }

    /* -------------------------------------------------------
                    DELETE FOR EVERYONE
    ------------------------------------------------------- */

    @PutMapping("/deleteForEveryone/{messageId}/{byUser}")
    public void deleteForEveryone(@PathVariable Long messageId, @PathVariable String byUser) {
        Optional<Message> optional = messageRepository.findById(messageId);
        if (optional.isEmpty()) return;

        Message msg = optional.get();

        if (!msg.getSender().equals(byUser)) return;

        msg.setDeleted(true);
        messageRepository.save(msg);

        messagingTemplate.convertAndSend("/topic/delete." + msg.getSender(),
                Map.of("messageId", msg.getId()));

        messagingTemplate.convertAndSend("/topic/delete." + msg.getReceiver(),
                Map.of("messageId", msg.getId()));
    }
}
