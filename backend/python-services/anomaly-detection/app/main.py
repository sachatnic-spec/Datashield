"""FastAPI entrypoint for the anomaly detection service."""
from __future__ import annotations

from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config import settings
from app.database import init_db
from app.logging_config import configure_logging
from app.models import BehavioralAnomalyModel
from app.routers import detection
from app.schemas import HealthResponse

configure_logging(settings.SERVICE_NAME, settings.DEBUG)
logger = logging.getLogger(settings.SERVICE_NAME)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Initializing anomaly detection service")
    init_db()
    app.state.behavior_model = BehavioralAnomalyModel()
    logger.info("Anomaly detection service started on port %s", settings.SERVICE_PORT)
    yield
    logger.info("Anomaly detection service shutdown complete")


app = FastAPI(
    title="DataShield Anomaly Detection Service",
    description="Behavior profiling and access anomaly detection for DPDP compliance.",
    version="1.0.0",
    lifespan=lifespan,
)
app.include_router(detection.router, prefix="/api/v1/anomaly", tags=["Behavioral Anomaly Detection"])


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("Unhandled application error on %s", request.url.path)
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check() -> HealthResponse:
    return HealthResponse(status="healthy", service=settings.SERVICE_NAME, port=settings.SERVICE_PORT)


@app.get("/", tags=["Health"])
async def root() -> dict:
    return {"message": "DataShield Anomaly Detection Service", "port": settings.SERVICE_PORT}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.SERVICE_PORT, reload=False)
