"""Configuration settings for the PII detection service."""
from functools import lru_cache
from pathlib import Path
import os

from pydantic import BaseModel, Field


BASE_DIR = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = BASE_DIR / "pii_detection.db"


class Settings(BaseModel):
    SERVICE_NAME: str = "pii-detection-service"
    SERVICE_PORT: int = Field(default=8019, gt=0, lt=65536)
    DEBUG: bool = False

    DATABASE_URL: str = f"sqlite:///{DEFAULT_DB_PATH}"
    DB_SCHEMA: str = "pii_detection"
    DB_POOL_SIZE: int = Field(default=5, ge=1)
    DB_MAX_OVERFLOW: int = Field(default=10, ge=0)
    DB_POOL_TIMEOUT: int = Field(default=30, ge=1)

    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9092"
    KAFKA_TOPIC_PII_DETECTED: str = "pii-detected"

    PII_CONFIDENCE_THRESHOLD: float = Field(default=0.70, ge=0, le=1)
    REDACTION_CHAR: str = "*"
    MAX_BATCH_SIZE: int = Field(default=1000, ge=1)
    CONTEXT_WINDOW: int = Field(default=100, ge=1)
    MODEL_PATH: str = "./models/"


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings(
        SERVICE_NAME=os.getenv("SERVICE_NAME", "pii-detection-service"),
        SERVICE_PORT=int(os.getenv("SERVICE_PORT", "8019")),
        DEBUG=os.getenv("DEBUG", "false").lower() == "true",
        DATABASE_URL=os.getenv("DATABASE_URL", f"sqlite:///{DEFAULT_DB_PATH}"),
        DB_SCHEMA=os.getenv("DB_SCHEMA", "pii_detection"),
        DB_POOL_SIZE=int(os.getenv("DB_POOL_SIZE", "5")),
        DB_MAX_OVERFLOW=int(os.getenv("DB_MAX_OVERFLOW", "10")),
        DB_POOL_TIMEOUT=int(os.getenv("DB_POOL_TIMEOUT", "30")),
        KAFKA_BOOTSTRAP_SERVERS=os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
        KAFKA_TOPIC_PII_DETECTED=os.getenv("KAFKA_TOPIC_PII_DETECTED", "pii-detected"),
        PII_CONFIDENCE_THRESHOLD=float(os.getenv("PII_CONFIDENCE_THRESHOLD", "0.7")),
        REDACTION_CHAR=os.getenv("REDACTION_CHAR", "*"),
        MAX_BATCH_SIZE=int(os.getenv("MAX_BATCH_SIZE", "1000")),
        CONTEXT_WINDOW=int(os.getenv("CONTEXT_WINDOW", "100")),
        MODEL_PATH=os.getenv("MODEL_PATH", "./models/"),
    )


settings = get_settings()
