📘 Project Overview – PDF Converter App & Web Server

The PDF Converter App is a powerful Android application built to simplify document management. It enables users to convert, merge, split, and share PDF files effortlessly — all within a modern and user-friendly interface. The project also includes a web server that connects with the mobile app, allowing seamless file transfers and remote PDF processing through real-time socket communication.

⚙️ Key Features
🧩 1. PDF Conversion

Convert files between formats such as PDF ↔️ Image, PDF ↔️ Text, and more.

High-speed conversion using backend processing for efficiency.

Supports both offline and online conversion (via the web server).

📑 2. Merge & Split PDFs

Merge multiple PDF files into a single organized document.

Split large PDFs into smaller, more manageable parts.

Offers custom selection for specific pages to extract or combine.

💾 3. File Management & History

Maintains a history of all past conversions, merges, and splits.

Allows users to reopen, rename, or delete processed files easily.

Files are organized by operation type (e.g., Converted, Merged, Split).

🔗 4. Web Connectivity (Socket Integration)

The app connects with a web-based interface built on Node.js (or similar backend).

Enables real-time communication using WebSockets.

Users can:

Upload a file on the web interface.

Send it directly to their connected mobile app for conversion.

Receive the processed file back instantly on the web or phone.

This feature bridges desktop and mobile workflows seamlessly.

📤 5. File Sharing & Transfer

Quickly share converted PDFs through email, social apps, or direct device-to-device transfer.

Option to upload and download files between web and mobile.

🖥️ 6.  Backend (Web Server)

Developed using Python / Node.js (based on your implementation).

Handles:

File uploads and storage.

Conversion processing.

Real-time communication using Socket.IO.

Allows users to interact with PDFs from any web browser and sync with the app.

🧠 Tech Stack

Mobile App:

Android (Kotlin + Jetpack Compose)

Retrofit / Ktor for networking

Material Design 3

Socket.IO client

Local file storage (Room / File System)

Web Backend:

Node.js (Express.js + Socket.IO) or Python (Flask / FastAPI)

File handling and conversion logic

Secure REST and WebSocket endpoints

🚀 How It Works (Flow)

User selects or uploads a PDF in the app or on the web.

App performs conversion / merging / splitting (locally or via web server).

Processed file is saved and logged in history for reuse.

WebSocket connection syncs files between devices in real time.

User can share or download the result instantly.

## APP IMAGES :
## APP IMAGES :

<p align="center">
  <img src="https://github.com/user-attachments/assets/c2e8a8e3-ba0d-4cba-b778-717f8618bb0a" alt="pdf 5" width="30%">
  <img src="https://github.com/user-attachments/assets/02ec3500-def9-4952-8b98-385b1aaa6101" alt="pdf 5" width="30%">
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/e7745e1d-6011-4a22-8226-f150443e67ed" alt="pdf 5" width="30%">
  <img src="https://github.com/user-attachments/assets/9abe85d8-46ff-4dbf-8f48-ae0d1bd88908" alt="pdf 5" width="30%">
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/b5d4440b-1beb-4099-9baf-aed73e74da5b" alt="pdf 5" width="30%">
</p>


## WEB IMAGES :
<img width="1308" height="634" alt="image" src="https://github.com/user-attachments/assets/d1320fa2-89e1-4a58-94ff-e706478de715" />
<img width="1341" height="647" alt="image" src="https://github.com/user-attachments/assets/c6e6e8ae-dbae-4c1d-ac3e-1f1e22f337e5" />

