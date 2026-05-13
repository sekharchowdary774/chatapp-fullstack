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

    
