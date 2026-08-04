"""Pydantic schemas for the AI analysis API."""
from datetime import datetime
from typing import Any, Dict, List

from pydantic import BaseModel, Field


class AnomalyRequest(BaseModel):
    metric_type: str = Field(..., min_length=1, max_length=128)
    current_value: float
    historical_values: List[float] = Field(..., min_length=2)


class ForecastRequest(BaseModel):
    metric_type: str = Field(..., min_length=1, max_length=128)
    values: List[float] = Field(..., min_length=2)
    forecast_days: int = Field(..., ge=1, le=365)


class AnomalyResponse(BaseModel):
    tenant_id: str
    metric_type: str
    metric_value: float
    baseline_value: float
    deviation_percentage: float
    anomaly_score: float
    severity: str
    detection_method: str
    is_alert_triggered: bool
    description: str
    detected_at: datetime


class ForecastResponse(BaseModel):
    tenant_id: str
    metric_type: str
    forecast_days: int
    current_value: float
    predicted_value: float
    confidence_interval: float
    forecast_summary: str
    trend_direction: float
    generated_at: datetime


class InsightResponse(BaseModel):
    tenant_id: str
    total_anomalies: int
    critical_anomalies: int
    anomaly_distribution: Dict[str, int]
    active_forecasts: int
    avg_forecast_confidence: float


class HealthResponse(BaseModel):
    status: str
    service: str
    port: int
