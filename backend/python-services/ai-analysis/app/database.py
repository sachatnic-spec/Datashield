"""Database models and session management for AI analysis."""
from __future__ import annotations

from datetime import datetime
import uuid

from sqlalchemy import Boolean, Column, DateTime, Float, Integer, String, Text, create_engine, text
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


class AnomalyDetection(Base):
    __tablename__ = "anomaly_detection"
    __table_args__ = TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    metric_type = Column(String(128), nullable=False, index=True)
    metric_value = Column(Float, nullable=False)
    baseline_value = Column(Float, nullable=False)
    deviation_percentage = Column(Float, nullable=False)
    anomaly_score = Column(Float, nullable=False, index=True)
    severity = Column(String(32), nullable=False, index=True)
    description = Column(Text, nullable=False)
    detection_method = Column(String(64), nullable=False)
    is_alert_triggered = Column(Boolean, default=False, nullable=False)
    detected_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)


class TrendForecast(Base):
    __tablename__ = "trend_forecast"
    __table_args__ = TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    metric_type = Column(String(128), nullable=False, index=True)
    forecast_days = Column(Integer, nullable=False)
    current_value = Column(Float, nullable=False)
    predicted_value = Column(Float, nullable=False)
    confidence_interval = Column(Float, nullable=False)
    forecast_summary = Column(Text, nullable=False)
    trend_direction = Column(Float, nullable=False)
    generated_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)


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
