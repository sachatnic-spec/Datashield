"""Database models and session management for anomaly detection."""
from __future__ import annotations

from datetime import datetime
import uuid

from sqlalchemy import Boolean, Column, DateTime, Float, String, Text, UniqueConstraint, create_engine, text
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
USER_PROFILE_TABLE_ARGS = (
    (UniqueConstraint("tenant_id", "user_id", name="uq_user_profile_tenant_user"),)
    if _is_sqlite()
    else (UniqueConstraint("tenant_id", "user_id", name="uq_user_profile_tenant_user"), {"schema": settings.DB_SCHEMA})
)


class BehavioralAnomaly(Base):
    __tablename__ = "behavioral_anomaly"
    __table_args__ = TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    user_id = Column(String(128), nullable=False, index=True)
    anomaly_score = Column(Float, nullable=False, index=True)
    anomaly_type = Column(String(64), nullable=False, index=True)
    location = Column(String(256), nullable=False)
    access_volume = Column(Float, nullable=False)
    access_time = Column(DateTime, nullable=False)
    details_json = Column(Text, nullable=False)
    flagged_unauthorized = Column(Boolean, default=False, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)


class UserProfile(Base):
    __tablename__ = "user_profile"
    __table_args__ = USER_PROFILE_TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    user_id = Column(String(128), nullable=False, index=True)
    profile_json = Column(Text, nullable=False)
    home_location = Column(String(256), nullable=False)
    baseline_volume = Column(Float, nullable=False)
    volume_stddev = Column(Float, nullable=False)
    typical_hours_json = Column(Text, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)


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
