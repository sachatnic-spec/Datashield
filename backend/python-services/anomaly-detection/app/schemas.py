"""Pydantic schemas for the anomaly detection API."""
from datetime import datetime
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class AccessRecord(BaseModel):
    access_time: datetime
    location: str = Field(..., min_length=1, max_length=256)
    volume: float = Field(..., ge=0)


class AccessAnomalyRequest(BaseModel):
    user_id: str = Field(..., min_length=1, max_length=128)
    access_time: datetime
    location: str = Field(..., min_length=1, max_length=256)
    volume: float = Field(..., ge=0)
    historical_accesses: Optional[List[AccessRecord]] = None

    class Config:
        extra = "forbid"


class BehaviorProfileResponse(BaseModel):
    tenant_id: str
    user_id: str
    profile: Dict[str, Any]
    updated_at: datetime


class AccessAnomalyResponse(BaseModel):
    tenant_id: str
    user_id: str
    anomaly_score: float
    is_anomalous: bool
    location_score: float
    volume_z_score: float
    time_score: float
    flagged_unauthorized: bool
    profile: Dict[str, Any]
    evaluated_at: datetime


class UnauthorizedFlagRequest(BaseModel):
    user_id: str = Field(..., min_length=1, max_length=128)
    anomaly_score: float = Field(..., ge=0, le=1)
    anomaly_id: Optional[str] = None


class UnauthorizedFlagResponse(BaseModel):
    user_id: str
    anomaly_score: float
    flagged_unauthorized: bool
    response_priority: str


class LocationAnomalyResponse(BaseModel):
    user_id: str
    current_location: str
    expected_location: str
    location_score: float
    is_anomalous: bool
    reason: str


class HealthResponse(BaseModel):
    status: str
    service: str
    port: int
