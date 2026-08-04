"""Configuration settings for the anomaly detection service."""
from functools import lru_cache
from pathlib import Path
import os

from pydantic import BaseModel, Field


BASE_DIR = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = BASE_DIR / "anomaly_detection.db"


class Settings(BaseModel):
    SERVICE_NAME: str = "anomaly-detection-service"
    SERVICE_PORT: int = Field(default=8021, gt=0, lt=65536)
    DEBUG: bool = False

    DATABASE_URL: str = f"sqlite:///{DEFAULT_DB_PATH}"
    DB_SCHEMA: str = "anomaly_detection"
    DB_POOL_SIZE: int = Field(default=5, ge=1)
    DB_MAX_OVERFLOW: int = Field(default=10, ge=0)
    DB_POOL_TIMEOUT: int = Field(default=30, ge=1)

    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9092"
    KAFKA_TOPIC_ACCESS_ANOMALY: str = "behavioral-access-anomaly"

    PROFILE_LOOKBACK_DAYS: int = Field(default=30, ge=1, le=365)
    UNAUTHORIZED_THRESHOLD: float = Field(default=0.8, ge=0, le=1)


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings(
        SERVICE_NAME=os.getenv("SERVICE_NAME", "anomaly-detection-service"),
        SERVICE_PORT=int(os.getenv("SERVICE_PORT", "8021")),
        DEBUG=os.getenv("DEBUG", "false").lower() == "true",
        DATABASE_URL=os.getenv("DATABASE_URL", f"sqlite:///{DEFAULT_DB_PATH}"),
        DB_SCHEMA=os.getenv("DB_SCHEMA", "anomaly_detection"),
        DB_POOL_SIZE=int(os.getenv("DB_POOL_SIZE", "5")),
        DB_MAX_OVERFLOW=int(os.getenv("DB_MAX_OVERFLOW", "10")),
        DB_POOL_TIMEOUT=int(os.getenv("DB_POOL_TIMEOUT", "30")),
        KAFKA_BOOTSTRAP_SERVERS=os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
        KAFKA_TOPIC_ACCESS_ANOMALY=os.getenv("KAFKA_TOPIC_ACCESS_ANOMALY", "behavioral-access-anomaly"),
        PROFILE_LOOKBACK_DAYS=int(os.getenv("PROFILE_LOOKBACK_DAYS", "30")),
        UNAUTHORIZED_THRESHOLD=float(os.getenv("UNAUTHORIZED_THRESHOLD", "0.8")),
    )


settings = get_settings()
