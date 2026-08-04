"""Database models and session management for risk scoring."""
from __future__ import annotations

from datetime import datetime
import json
import uuid

from sqlalchemy import Column, DateTime, Float, String, Text, create_engine, text
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


class VendorRiskScore(Base):
    __tablename__ = "vendor_risk_score"
    __table_args__ = TABLE_ARGS

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    tenant_id = Column(String(128), nullable=False, index=True)
    vendor_id = Column(String(128), nullable=False, index=True)
    risk_score = Column(Float, nullable=False, index=True)
    risk_level = Column(String(32), nullable=False, index=True)
    factors_json = Column(Text, nullable=False)
    calculated_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    def factors(self) -> dict:
        return json.loads(self.factors_json or "{}")


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
