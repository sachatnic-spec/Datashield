"""Pydantic schemas for the risk scoring API."""
from datetime import datetime
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class VendorScoreRequest(BaseModel):
    vendor_id: str = Field(..., min_length=1, max_length=128)
    security_score: float = Field(..., ge=0, le=100)
    compliance_score: float = Field(..., ge=0, le=100)
    operational_score: float = Field(..., ge=0, le=100)
    historical_score: float = Field(..., ge=0, le=100)
    metadata: Optional[Dict[str, Any]] = None

    class Config:
        extra = "forbid"


class VendorScoreResponse(BaseModel):
    tenant_id: str
    vendor_id: str
    risk_score: float
    risk_level: str
    factors: Dict[str, Any]
    calculated_at: datetime


class RiskTrendResponse(BaseModel):
    vendor_id: str
    trend_direction: str
    slope: float
    intercept: float
    predicted_next_score: float
    confidence: float
    samples: int


class TopRiskItem(BaseModel):
    vendor_id: str
    risk_score: float
    risk_level: str
    calculated_at: datetime
    factors: Dict[str, Any]


class TopRiskResponse(BaseModel):
    tenant_id: str
    limit: int
    total_items: int
    items: List[TopRiskItem]


class HealthResponse(BaseModel):
    status: str
    service: str
    port: int
