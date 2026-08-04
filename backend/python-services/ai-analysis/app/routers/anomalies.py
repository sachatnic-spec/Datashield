"""API routes for AI-driven anomaly and forecast analysis."""
from __future__ import annotations

import logging

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import AnomalyDetection, TrendForecast, get_db
from app.dependencies import get_ai_model, require_tenant_id
from app.models import AIAnalysisModel
from app.schemas import AnomalyRequest, AnomalyResponse, ForecastRequest, ForecastResponse, InsightResponse

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/anomalies/detect", response_model=AnomalyResponse, summary="Detect metric anomalies")
async def detect_anomaly(
    request: AnomalyRequest,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: AIAnalysisModel = Depends(get_ai_model),
) -> AnomalyResponse:
    try:
        result = model.detect_anomaly(request.metric_type, request.current_value, request.historical_values)
        record = AnomalyDetection(tenant_id=tenant_id, **result)
        db.add(record)
        db.commit()
        return AnomalyResponse(tenant_id=tenant_id, **result)
    except ValueError as exc:
        db.rollback()
        logger.warning("AI anomaly validation failed: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        db.rollback()
        logger.exception("AI anomaly detection failed")
        raise HTTPException(status_code=500, detail="Unable to detect anomaly") from exc


@router.post("/forecast/trend", response_model=ForecastResponse, summary="Forecast metric trends")
async def forecast_trend(
    request: ForecastRequest,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: AIAnalysisModel = Depends(get_ai_model),
) -> ForecastResponse:
    try:
        result = model.forecastTrend(request.metric_type, request.values, request.forecast_days)
        record = TrendForecast(tenant_id=tenant_id, **result)
        db.add(record)
        db.commit()
        return ForecastResponse(tenant_id=tenant_id, **result)
    except ValueError as exc:
        db.rollback()
        logger.warning("AI forecast validation failed: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        db.rollback()
        logger.exception("AI trend forecasting failed")
        raise HTTPException(status_code=500, detail="Unable to forecast trend") from exc


@router.get("/anomalies/tenant/{tenant_id}", response_model=list[AnomalyResponse], summary="List anomalies for a tenant")
async def get_tenant_anomalies(tenant_id: str, db: Session = Depends(get_db)) -> list[AnomalyResponse]:
    rows = db.query(AnomalyDetection).filter(AnomalyDetection.tenant_id == tenant_id).order_by(AnomalyDetection.detected_at.desc()).all()
    return [
        AnomalyResponse(
            tenant_id=row.tenant_id,
            metric_type=row.metric_type,
            metric_value=row.metric_value,
            baseline_value=row.baseline_value,
            deviation_percentage=row.deviation_percentage,
            anomaly_score=row.anomaly_score,
            severity=row.severity,
            detection_method=row.detection_method,
            is_alert_triggered=row.is_alert_triggered,
            description=row.description,
            detected_at=row.detected_at,
        )
        for row in rows
    ]


@router.get("/anomalies/tenant/{tenant_id}/critical", response_model=list[AnomalyResponse], summary="List critical anomalies for a tenant")
async def get_critical_anomalies(tenant_id: str, db: Session = Depends(get_db)) -> list[AnomalyResponse]:
    rows = (
        db.query(AnomalyDetection)
        .filter(AnomalyDetection.tenant_id == tenant_id, AnomalyDetection.severity == "CRITICAL")
        .order_by(AnomalyDetection.detected_at.desc())
        .all()
    )
    return [
        AnomalyResponse(
            tenant_id=row.tenant_id,
            metric_type=row.metric_type,
            metric_value=row.metric_value,
            baseline_value=row.baseline_value,
            deviation_percentage=row.deviation_percentage,
            anomaly_score=row.anomaly_score,
            severity=row.severity,
            detection_method=row.detection_method,
            is_alert_triggered=row.is_alert_triggered,
            description=row.description,
            detected_at=row.detected_at,
        )
        for row in rows
    ]


@router.get("/insights/{tenant_id}", response_model=InsightResponse, summary="Generate aggregated AI insights")
async def generate_insights(
    tenant_id: str,
    db: Session = Depends(get_db),
    model: AIAnalysisModel = Depends(get_ai_model),
) -> InsightResponse:
    anomalies = db.query(AnomalyDetection).filter(AnomalyDetection.tenant_id == tenant_id).all()
    forecasts = db.query(TrendForecast).filter(TrendForecast.tenant_id == tenant_id).all()
    result = model.generate_insights(
        tenant_id,
        anomalies=[
            {"metric_type": row.metric_type, "severity": row.severity}
            for row in anomalies
        ],
        forecasts=[
            {"confidence_interval": row.confidence_interval}
            for row in forecasts
        ],
    )
    return InsightResponse(**result)
