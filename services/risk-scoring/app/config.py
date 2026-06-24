"""Configuration settings for the risk scoring service."""
from functools import lru_cache
from pathlib import Path
import os

from pydantic import BaseModel, Field


BASE_DIR = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = BASE_DIR / "risk_scoring.db"


class Settings(BaseModel):
    SERVICE_NAME: str = "risk-scoring-service"
    SERVICE_PORT: int = Field(default=8020, gt=0, lt=65536)
    DEBUG: bool = False

    DATABASE_URL: str = f"sqlite:///{DEFAULT_DB_PATH}"
    DB_SCHEMA: str = "risk_scoring"
    DB_POOL_SIZE: int = Field(default=5, ge=1)
    DB_MAX_OVERFLOW: int = Field(default=10, ge=0)
    DB_POOL_TIMEOUT: int = Field(default=30, ge=1)

    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9092"
    KAFKA_TOPIC_VENDOR_RISK: str = "vendor-risk-scored"

    DEFAULT_TOP_RISK_LIMIT: int = Field(default=10, ge=1, le=100)
    TREND_LOOKBACK_LIMIT: int = Field(default=30, ge=2, le=365)


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings(
        SERVICE_NAME=os.getenv("SERVICE_NAME", "risk-scoring-service"),
        SERVICE_PORT=int(os.getenv("SERVICE_PORT", "8020")),
        DEBUG=os.getenv("DEBUG", "false").lower() == "true",
        DATABASE_URL=os.getenv("DATABASE_URL", f"sqlite:///{DEFAULT_DB_PATH}"),
        DB_SCHEMA=os.getenv("DB_SCHEMA", "risk_scoring"),
        DB_POOL_SIZE=int(os.getenv("DB_POOL_SIZE", "5")),
        DB_MAX_OVERFLOW=int(os.getenv("DB_MAX_OVERFLOW", "10")),
        DB_POOL_TIMEOUT=int(os.getenv("DB_POOL_TIMEOUT", "30")),
        KAFKA_BOOTSTRAP_SERVERS=os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
        KAFKA_TOPIC_VENDOR_RISK=os.getenv("KAFKA_TOPIC_VENDOR_RISK", "vendor-risk-scored"),
        DEFAULT_TOP_RISK_LIMIT=int(os.getenv("DEFAULT_TOP_RISK_LIMIT", "10")),
        TREND_LOOKBACK_LIMIT=int(os.getenv("TREND_LOOKBACK_LIMIT", "30")),
    )


settings = get_settings()
