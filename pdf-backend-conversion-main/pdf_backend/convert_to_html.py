import subprocess
from pathlib import Path
from fastapi import APIRouter, UploadFile, File, HTTPException

router = APIRouter()

UPLOAD_DIR = Path("uploads")
PDF_DIR = Path("converted")
UPLOAD_DIR.mkdir(exist_ok=True)
PDF_DIR.mkdir(exist_ok=True)

ALLOWED_HTML_TYPES = ["text/html"]

LIBREOFFICE_PATH = r"C:\Program Files\LibreOffice\program\soffice.exe"  # adjust if needed


@router.post("/upload/html_to_pdf/")
async def convert_html_to_pdf(file: UploadFile = File(...)):
    if file.content_type not in ALLOWED_HTML_TYPES:
        raise HTTPException(status_code=400, detail="Only HTML files are supported")

    html_path = UPLOAD_DIR / file.filename
    pdf_path = PDF_DIR / f"{Path(file.filename).stem}.pdf"

    # Save uploaded HTML
    with open(html_path, "wb") as f:
        f.write(await file.read())

    try:
        if not Path(LIBREOFFICE_PATH).exists():
            raise HTTPException(
                status_code=500,
                detail=f"LibreOffice not found at {LIBREOFFICE_PATH}"
            )

        # Convert HTML → PDF
        cmd = [
            str(LIBREOFFICE_PATH),
            "--headless",
            "--convert-to", "pdf",
            "--outdir", str(PDF_DIR),
            str(html_path)
        ]
        subprocess.run(cmd, check=True)

        if not pdf_path.exists():
            raise HTTPException(
                status_code=500,
                detail="PDF file was not created by LibreOffice"
            )

    except subprocess.CalledProcessError as e:
        raise HTTPException(status_code=500, detail=f"LibreOffice conversion failed: {e}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Conversion failed: {e}")

    return {
        "message": "HTML converted to PDF successfully",
        "file": pdf_path.name,
        "download_url": f"/download/{pdf_path.name}"
    }
