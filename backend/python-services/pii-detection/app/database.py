"""Database models and utilities for PII detection."""
from __future__ import annotations

from datetime import datetime
import uuid

from sqlalchemy import Boolean, Column, DateTime, Float, String, Text, create_engine, text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

from app.config import settings


Base = declarative_base()


def _is_sqlite() -> bool:
    return settings.DATABASE_URL.startswith("sqlite")


def _engine_kwargs() -> dict:
    kwargs = {"pool_pre_ping": True}
    if _is_sqlite():
        kwargs["connect_args"] = {"check_same_thread": False}
    else:
        kwargs.update(
            {
                "pool_size": settings.DB_POOL_SIZE,
                "max_overflow": settings.DB_MAX_OVERFLOW,
                "pool_timeout": settings.DB_POOL_TIMEOUT,
            }
        )
    return kwargs


engine = create_engine(settings.DATABASE_URL, **_engine_kwargs())
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, expire_on_commit=False)
TABLE_ARGS = {} if _is_sqlite() else {"schema": settings.DB_SCHEMA}


class PIIDetectionResult(Base):
    __tablename__ = "pii_detection_result"
    __table_args__ = TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    input_text = Column(Text)
    pii_type = Column(String(50), nullable=False, index=True)
    confidence_score = Column(Float, nullable=False)
    context = Column(String(500))
    redacted_text = Column(Text)
    detection_source = Column(String(50), default="ML_MODEL", nullable=False)
    requires_human_review = Column(Boolean, default=False, nullable=False)
    detected_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db() -> None:
    if not _is_sqlite():
        with engine.begin() as connection:
            connection.execute(text(f"CREATE SCHEMA IF NOT EXISTS {settings.DB_SCHEMA}"))
    Base.metadata.create_all(bind=engine)
