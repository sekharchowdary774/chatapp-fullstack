🌐 Real-Time Chat App – Frontend

Modern React frontend for a WhatsApp-style real-time chat app with WebSockets, message reactions, edit/delete, and a clean minimal UI.

✅ Tech Stack
Category	Technologies
Frontend Framework	React.js (Vite or CRA)
State Management	useState, useEffect, useRef
WebSocket Client	SockJS + STOMP.js
HTTP Client	Axios
Routing	React Router
UI Features	Message bubbles, typing indicators, sidebar chats
Deployment	Vercel
⚡️ Features
🔐 Authentication UI

1.Login / Signup
2.JWT stored in localStorage
3.Forgot Password (local reset flow)

💬 Messaging UI

1.Real-time updates
2.Live typing indicator
3.Delivered / Seen ticks
4.Reactions (emoji picker)
5.Edit message
6.Delete for me / everyone
7.Reply and forward message
8.File & image upload preview
9.Lightbox image viewer

🔍 User Search

1.Search users by username or email
2.Debounced API calls
3.Opens new chat instantly

📁 Folder Structure

      src/
 ├── components/
 │     ├── UserSearchSidebar.jsx
 │     ├── ChatListItem.jsx
 │     ├── MessageBubble.jsx
 │
 ├── pages/
 │     ├── Login.jsx
 │     ├── Signup.jsx
 │     ├── Chat.jsx
 │     ├── ForgotPassword.jsx
 │
 ├── services/
 │     ├── api.js
 │     ├── chatApi.js
 │
 ├── styles/
 │     ├── Auth.css
 │     ├── UserSearchSidebar.css
 │
 ├── App.js
 └── main.jsx

🔌 Run Locally
1️⃣ Install Packages :  npm install
2️⃣ Start App : npm run dev
3️⃣ Configure API Base URLs
     In api.js:
          baseURL: "https://chat-backened-2.onrender.com/api/auth"
    In chatApi.js:
         baseURL: "https://chat-backened-2.onrender.com/api"

🌍 Deployment

Deployed using Vercel
Automatic builds on push to main.

🔗 WebSocket Endpoint
    wss://chat-backened-2.onrender.com/chat

🚀 Real-Time Chat App – Backend

Spring Boot backend powering a full real-time private messaging system using WebSockets (STOMP), JWT authentication, PostgreSQL, Cloudinary file uploads, and NeonDB hosting.

✅ Tech Stack
Category	Technologies
Backend Framework	Spring Boot 3 (Java 17)
WebSocket / Messaging	STOMP, SockJS, SimpMessagingTemplate
Security	Spring Security 6, JWT Token Auth
Database	PostgreSQL (NeonDB) + JPA/Hibernate
Cloud Storage	Cloudinary
Build Tool	Maven
Deployment	Render.com

⚡️ Features
🔐 Authentication

1.User Signup

2.User Login

3.JWT token generation

4.Secure protected API routes

💬 Real-Time Chat System

1.Private 1-on-1 real-time messaging

2.Online/offline status using WebSockets

3.Typing indicator

4.Delivered & Seen message status

5.Message reactions (👍 ❤️ 😂 😮 😢 🙏)

6.Message edits

7.Delete for me / Delete for everyone

8.Image/file sending through Cloudinary

🧠 Chat Room Logic

1.Auto-creates chatroom when two users start a conversation

2.Loads chat history

3.Tracks unread message count

4.Socket push updates for unread notifications

🔍 User Search

1.Search by username or email

2.Excludes logged-in user

3.Public search endpoint
📁 Project Structure

  src/main/java/com/example/chatapp
 ├── config
 │    ├── SecurityConfig.java
 │    ├── WebSocketConfig.java
 │
 ├── controller
 │    ├── AuthController.java
 │    ├── UserController.java
 │    ├── ChatController.java
 │
 ├── entity
 │    ├── User.java
 │    ├── Message.java
 │    ├── ChatRoom.java
 │
 ├── repository
 │    ├── UserRepository.java
 │    ├── MessageRepository.java
 │    ├── ChatRoomRepository.java
 │
 ├── service
 │    ├── ChatRoomService.java
 │    ├── CloudinaryService.java
 │    ├── OnlineService.java
 │
 ├── security
 │    ├── JwtFilter.java
 │    ├── JwtUtil.java
 │    ├── UserDetailsServiceImpl.java

🔌 Running Locally
1️⃣ Update application.properties

   spring.datasource.url=jdbc:postgresql://localhost:5432/chatdb
   spring.datasource.username=postgres
   spring.datasource.password=yourpassword

  cloudinary.cloud_name=xxxx
  cloudinary.api_key=xxxx
  cloudinary.api_secret=xxxx
2️⃣ Run Backend
   mvn spring-boot:run
Backend will run at:
   http://localhost:8080
📡 Important API Endpoints

 1.Auth:
 
    | Method | Endpoint           | Description        |
| ------ | ------------------ | ------------------ |
| POST   | `/api/auth/signup` | Register new user  |
| POST   | `/api/auth/login`  | Login, returns JWT |

2.Chat:
   | Method | Endpoint                             | Description           |
| ------ | ------------------------------------ | --------------------- |
| GET    | `/api/chat/rooms/{email}`            | Get chat list         |
| GET    | `/api/chat/{sender}/{receiver}`      | Get chat history      |
| POST   | WebSocket `/app/private-message`     | Send message          |
| PUT    | `/api/chat/seen/{sender}/{receiver}` | Mark messages as seen |

3.User Search
 | GET | /api/users/search?query=xx&exclude=email
4.WebSocket Endpoints
   | Destination              | Purpose              |
| ------------------------ | -------------------- |
| `/app/private-message`   | Send message         |
| `/topic/room.{roomId}`   | Receive messages     |
| `/app/typing`            | Typing events        |
| `/topic/typing.{roomId}` | Receive typing state |

🚀 Deployment

Deployed on Render Web Service with:

1.Port: 8080
2.Build: mvn clean install

    
