"""
PII Detection Service - Main FastAPI Application
Port 8019: ML-based PII detection and redaction
"""
from fastapi import FastAPI, HTTPException, Header
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
import logging
from app.models import PIIDetectionModel
from app.routers import detection, redaction, bulk
from app.config import settings
from app.database import init_db

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Loading PII Detection ML model...")
    app.state.pii_model = PIIDetectionModel()
    init_db()
    logger.info("PII Detection Service started on port %s", settings.SERVICE_PORT)
    yield
    # Shutdown
    logger.info("Shutting down PII Detection Service")

app = FastAPI(
    title="DataShield PII Detection Service",
    description="ML-based PII detection and redaction for DPDP compliance",
    version="1.0.0",
    lifespan=lifespan
)

# Include routers
app.include_router(detection.router, prefix="/api/v1/pii", tags=["Detection"])
app.include_router(redaction.router, prefix="/api/v1/pii", tags=["Redaction"])
app.include_router(bulk.router, prefix="/api/v1/pii", tags=["Bulk Operations"])

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "pii-detection"}

@app.get("/")
async def root():
    return {"message": "DataShield PII Detection Service v1.0.0", "port": settings.SERVICE_PORT}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.SERVICE_PORT)
