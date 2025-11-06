import React from "react";
import PDFTool from "./PDFTool";
import sampleImg from "./assets/pdf.png";

const App = () => {
  return (
    <div style={styles.appBackground}>
      <header style={styles.header}>
        <div style={styles.navbar}>
          <h1 style={styles.logo}>📲 PDF Bridge</h1>
          <span style={styles.tagline}>send pdf from Web to Mobile app Seamlessly</span>
        </div>
      </header>

      <main style={styles.mainContent}>
        {/* ⬅️ Image on the left */}
        <div style={styles.imageSection}>
          <img src={sampleImg} alt="PDF Illustration" style={styles.image} />
          <p style={styles.imageCaption}>PDF Converter</p>
        </div>

        {/* 🛠 PDF tool on the right */}
        <div style={styles.toolSection}>
          <PDFTool />
        </div>
      </main>

      <footer style={styles.footer}>
        <p>© 2025 PDF Bridge — Empowering PDF workflows across devices.</p>
      </footer>
    </div>
  );
};

const styles = {
  appBackground: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #fce4ec, #ffffff)",
    display: "flex",
    flexDirection: "column",
    fontFamily: "Segoe UI, sans-serif",
  },
  header: {
    backgroundColor: "#e91e63",
    padding: "20px 40px",
    boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
  },
  navbar: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    flexWrap: "wrap",
  },
  logo: {
    color: "#ffffff",
    margin: 0,
    fontSize: "28px",
    fontWeight: "bold",
  },
  tagline: {
    color: "#fce4ec",
    fontSize: "14px",
    fontStyle: "italic",
  },
  mainContent: {
    flex: 1,
    padding: "30px 20px",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    flexWrap: "wrap",
    gap: "20px",
  },
  imageSection: {
    flex: "1 1 350px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  image: {
    width: "80%",
    height: "auto",
  },
  imageCaption: {
    marginTop: "10px",
    fontSize: "50px",
    color: "#e91e63",
    fontWeight: "bold",
  },
  toolSection: {
    flex: "1 1 400px",
  },
  footer: {
    textAlign: "center",
    padding: "20px",
    backgroundColor: "#fce4ec",
    fontSize: "14px",
    color: "#555",
    borderTop: "1px solid #e91e63",
  },
};

export default App;
