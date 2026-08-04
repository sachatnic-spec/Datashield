"""FastAPI entrypoint for the risk scoring service."""
from __future__ import annotations

from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config import settings
from app.database import init_db
from app.logging_config import configure_logging
from app.models import RiskScoringModel
from app.routers import scoring
from app.schemas import HealthResponse

configure_logging(settings.SERVICE_NAME, settings.DEBUG)
logger = logging.getLogger(settings.SERVICE_NAME)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Initializing risk scoring service")
    init_db()
    app.state.risk_model = RiskScoringModel()
    logger.info("Risk scoring service started on port %s", settings.SERVICE_PORT)
    yield
    logger.info("Risk scoring service shutdown complete")


app = FastAPI(
    title="DataShield Risk Scoring Service",
    description="Vendor risk scoring and trend forecasting for DPDP compliance operations.",
    version="1.0.0",
    lifespan=lifespan,
)
app.include_router(scoring.router, prefix="/api/v1/risk", tags=["Risk Scoring"])


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("Unhandled application error on %s", request.url.path)
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check() -> HealthResponse:
    return HealthResponse(status="healthy", service=settings.SERVICE_NAME, port=settings.SERVICE_PORT)


@app.get("/", tags=["Health"])
async def root() -> dict:
    return {"message": "DataShield Risk Scoring Service", "port": settings.SERVICE_PORT}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.SERVICE_PORT, reload=False)
