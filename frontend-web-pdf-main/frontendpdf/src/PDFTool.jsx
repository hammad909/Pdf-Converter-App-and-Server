import React, { useState, useEffect, useRef } from "react";
import { io } from "socket.io-client";
import { QRCodeCanvas } from "qrcode.react";


const SERVER_URL = "http://192.168.100.121:9000";

const PDFTool = () => {
  const [file, setFile] = useState(null);
  const [code, setCode] = useState(""); // connection code
  const [connected, setConnected] = useState(false);
  const [status, setStatus] = useState("Not connected");
  const [downloadUrl, setDownloadUrl] = useState("");
  const socketRef = useRef(null);

  // Fetch code from server
  const fetchCodeFromServer = async () => {
    try {
      const response = await fetch(`${SERVER_URL}/generate_code`, {
        method: "POST",
      });
      const data = await response.json();
      if (data.code) {
        setCode(data.code);
        setStatus(`Code generated: ${data.code}. Connecting...`);
      }
    } catch (error) {
      setStatus("Failed to get code from server");
    }
  };

  // Initialize socket once on mount
  useEffect(() => {
    const socket = io(SERVER_URL, { autoConnect: false });
    socketRef.current = socket;

    socket.on("connect", () => {
      setStatus(`Socket connected. Joining room with code ${code}...`);
      if (code) {
        socket.emit("join_code", { code, role: "web" });
      }
    });

    socket.on("paired", () => {
      setStatus("Paired with Mobile App");
      setConnected(true);
    });

    socket.on("code_expired", () => {
      setStatus("Code expired or disconnected");
      setConnected(false);
      setDownloadUrl("");
      socket.disconnect();
    });

   socket.on("processed_ready", ({ fileName }) => {
   setStatus("Processed file ready to download");
   setDownloadUrl(`${SERVER_URL}/download/${code}/${fileName}`);
   });


    socket.on("invalid_code", () => {
      setStatus("Invalid code entered. Please generate a new code.");
      setConnected(false);
      setDownloadUrl("");
      socket.disconnect();
    });

    socket.on("disconnect", () => {
      setStatus("Socket disconnected");
      setConnected(false);
    });

    return () => {
      socket.disconnect();
    };
  }, [code]);

  // Auto connect & join room whenever code changes
  useEffect(() => {
    const socket = socketRef.current;
    if (!socket || !code) return;

    if (socket.connected) {
      socket.emit("join_code", { code, role: "web" });
    } else {
      socket.connect();
      socket.once("connect", () => {
        socket.emit("join_code", { code, role: "web" });
      });
    }

    setConnected(false);
    setDownloadUrl("");
    setStatus(`Joining room with code ${code}...`);
  }, [code]);

  // Handle "Generate Code" button click
  const handleGenerateCode = () => {
    fetchCodeFromServer();
  };

  // File input handler
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setFile(e.target.files[0]);
    } else {
      setFile(null);
    }
  };

  // Handle action buttons: upload file, then emit action event
  const handleAction = async (actionType) => {
    if (!file) {
      alert("Please select a PDF file first");
      return;
    }
    if (!code) {
      alert("Please generate a connection code first");
      return;
    }

    setStatus(`Uploading file for ${actionType}...`);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await fetch(`${SERVER_URL}/upload/${code}`, {
        method: "POST",
        body: formData,
      });

      if (res.ok) {
        setStatus(`File uploaded. Sending ${actionType} action to server...`);

        socketRef.current.emit("action", {
          action: actionType,
          fileName: file.name,
        });
      } else {
        setStatus("File upload failed");
      }
    } catch (error) {
      setStatus("Upload error: " + error.message);
    }
  };

  // Download converted file
  const handleDownload = () => {
    if (downloadUrl) {
      window.open(downloadUrl, "_blank");
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>📄 PDF Toolbox</h2>

      <p style={styles.instructionText}>
        Press "Generate Code" to create a new connection code. The app will
        connect automatically. Use this code in your mobile app to connect.
      </p>

<div style={{ marginBottom: 20 }}>
  <strong>Your connection code:</strong>{" "}
  <span
    style={{
      marginLeft: 10,
      fontSize: 22,
      fontWeight: "bold",
      color: "#e91e63",
      userSelect: "all",
    }}
  >
    {code || "—"}
  </span>
</div>

{/* ✅ QR code preview (only show if code exists) */}
{code && (
  <div style={{ marginBottom: 20 }}>
  <QRCodeCanvas value={code} size={200} />
    <p style={{ marginTop: 10, color: "#555" }}>Scan this QR in mobile app</p>
  </div>
)}

      <button
        onClick={handleGenerateCode}
        style={{
          backgroundColor: "#e91e63",
          color: "#fff",
          border: "none",
          padding: "12px 30px",
          fontSize: "16px",
          borderRadius: "8px",
          cursor: "pointer",
          fontWeight: "600",
          marginBottom: 20,
        }}
      >
        Generate Code
      </button>

      <div style={styles.uploadSection}>
     <input
      type="file"
       accept=".pdf"
       onChange={handleFileChange}
       style={styles.input}
      />
      </div>

      <div style={styles.status}>
        <strong>Status:</strong>{" "}
        <span style={{ color: connected ? "green" : "red" }}>{status}</span>
      </div>

      {downloadUrl && (
        <button onClick={handleDownload} style={styles.downloadButton}>
          ⬇️ Download Converted File
        </button>
      )}

      <div style={styles.divider} />

      <div style={styles.actions}>
        <button
          onClick={() => handleAction("merge")}
          style={styles.actionButton}
          disabled={!file || !code}
          title="Merge PDF"
        >
          📎 Merge
        </button>
        <button
          onClick={() => handleAction("split")}
          style={styles.actionButton}
          disabled={!file || !code}
          title="Split PDF"
        >
          ✂️ Split
        </button>
        <button
          onClick={() => handleAction("convert")}
          style={styles.actionButton}
          disabled={!file || !code}
          title="Convert PDF"
        >
          🔄 Convert
        </button>
      </div>
    </div>
  );
};

const styles = {
  container: {
    backgroundColor: "#ffffff",
    padding: "40px",
    width: "90%",
    maxWidth: "600px",
    margin: "0 auto",
    borderRadius: "16px",
    boxShadow: "0 12px 24px rgba(0, 0, 0, 0.1)",
    fontFamily: "Segoe UI, sans-serif",
    textAlign: "center",
    border: "2px solid #e91e63",
    transition: "all 0.3s ease-in-out",
  },
  title: {
    fontSize: "28px",
    color: "#e91e63",
    marginBottom: "25px",
    fontWeight: "600",
  },
  instructionText: {
    fontSize: "18px",
    color: "#555",
    marginBottom: "25px",
    lineHeight: "1.5",
  },
  uploadSection: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "15px",
    marginBottom: 20,
  },
  input: {
    padding: "10px",
    fontSize: "16px",
    borderRadius: "6px",
    border: "1px solid #ccc",
    width: "100%",
    maxWidth: "300px",
  },
  status: {
    marginTop: 20,
    fontWeight: "600",
    fontSize: "16px",
    color: "#e91e63",
  },
  downloadButton: {
    marginTop: 20,
    backgroundColor: "#4caf50",
    color: "#fff",
    border: "none",
    padding: "12px 30px",
    fontSize: "16px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "500",
    transition: "background 0.3s",
  },
  divider: {
    height: "1px",
    backgroundColor: "#e91e63",
    margin: "30px 0",
    width: "100%",
  },
  actions: {
    display: "flex",
    justifyContent: "space-around",
    flexWrap: "wrap",
    gap: "15px",
  },
  actionButton: {
    backgroundColor: "#ffffff",
    color: "#e91e63",
    border: "2px solid #e91e63",
    padding: "12px 24px",
    fontSize: "16px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "500",
    transition: "all 0.3s ease-in-out",
  },
};

export default PDFTool;
