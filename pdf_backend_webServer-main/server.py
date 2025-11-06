import os
import shutil
import asyncio
import random
import string
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
import socketio

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

app = FastAPI()
sio = socketio.AsyncServer(async_mode="asgi", cors_allowed_origins="*")
socket_app = socketio.ASGIApp(sio, app)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

connected_codes = {}       # sid -> code
clients_in_room = {}       # code -> set of sids
valid_codes = set()        # set of generated valid codes

# Generate random 6-digit code
def generate_random_code():
    return ''.join(random.choices(string.digits, k=6))

@app.post("/generate_code")
async def generate_code():
    while True:
        code = generate_random_code()
        if code not in valid_codes:
            valid_codes.add(code)
            print(f"Generated new code: {code}")
            return {"code": code}

@sio.event
async def connect(sid, environ):
    print(f"Socket connected: {sid}")


@sio.event
async def action(sid, data):
    """
    Expected data: {
      "action": "convert" | "merge" | "split",
      "fileName": "uploaded_file.pdf"
    }
    """
    code = connected_codes.get(sid)
    if not code:
        print(f"Received action from unknown sid {sid}")
        return

    action_type = data.get("action")
    file_name = data.get("fileName")

    if not action_type or not file_name:
        print(f"Invalid action data from sid {sid}: {data}")
        return

    print(f"Received action '{action_type}' with file '{file_name}' in room {code} from {sid}")

    # Broadcast action to all clients in the same room (including sender)
    await sio.emit("action", {"action": action_type, "fileName": file_name}, room=code)


@sio.event
async def join_code(sid, data):
    # Defensive: accept string or dict
    if isinstance(data, str):
        code = data
        role = None
    else:
        code = data.get("code")
        role = data.get("role")

    print(f"Socket {sid} trying to join code: {code} as {role}")

    if code not in valid_codes:
        await sio.emit("invalid_code", room=sid)
        return

    # Enforce max 2 clients per room
    if code in clients_in_room and len(clients_in_room[code]) >= 2:
        print(f"Room {code} full, rejecting {sid}")
        await sio.emit("room_full", room=sid)
        return

    await sio.enter_room(sid, code)
    connected_codes[sid] = code

    if code not in clients_in_room:
        clients_in_room[code] = set()
    clients_in_room[code].add(sid)

    print(f"Socket {sid} joined room {code} as {role}")

    await sio.emit("joined", {"sid": sid, "role": role}, room=code)

    if len(clients_in_room[code]) == 2:
        await sio.emit("paired", room=code)


@sio.event
async def disconnect(sid):
    code = connected_codes.get(sid)
    if code:
        print(f"Socket {sid} disconnected from code {code}")
        del connected_codes[sid]

        if code in clients_in_room:
            clients_in_room[code].discard(sid)
            if len(clients_in_room[code]) == 0:
                # Remove code from valid_codes to expire it when no clients left
                valid_codes.discard(code)
                del clients_in_room[code]
                print(f"Code {code} expired (no clients left)")
            else:
                await sio.emit("code_expired", room=code)


@app.post("/upload/{code}")
async def upload_file(code: str, file: UploadFile = File(...)):
    dir_path = os.path.join(UPLOAD_DIR, code)
    os.makedirs(dir_path, exist_ok=True)
    file_location = os.path.join(dir_path, file.filename)

    with open(file_location, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    print(f"File {file.filename} uploaded for code {code}")

    async def notify_file_ready():
        await asyncio.sleep(5)  # simulate processing delay
        await sio.emit("file_ready", {"fileName": file.filename}, room=code)
        print(f"Emitted file_ready for code {code}")

    asyncio.create_task(notify_file_ready())

    return {"status": "File uploaded and processing started"}



@app.get("/download/{code}/{filename}")
async def download_file(code: str, filename: str):
    file_path = os.path.join(UPLOAD_DIR, code, filename)
    if os.path.isfile(file_path):
        return FileResponse(path=file_path, filename=filename, media_type='application/pdf')
    else:
        raise HTTPException(status_code=404, detail="File not found")



@app.post("/upload_processed/{code}")
async def upload_processed_file(code: str, file: UploadFile = File(...)):
    """
    Handles processed file upload from mobile app.
    Notifies web via processed_ready so it can download it.
    """
    dir_path = os.path.join(UPLOAD_DIR, code)
    os.makedirs(dir_path, exist_ok=True)
    file_location = os.path.join(dir_path, file.filename)

    with open(file_location, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    print(f"Processed file {file.filename} uploaded for code {code}")

    # Notify web that processed file is ready to download
    await sio.emit("processed_ready", {"fileName": file.filename}, room=code)

    return {"status": "Processed file uploaded and ready"}
