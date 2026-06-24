"""API routes for vendor risk scoring."""
from __future__ import annotations

from datetime import datetime
import json
import logging
from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import VendorRiskScore, get_db
from app.dependencies import get_risk_model, require_tenant_id
from app.models import RiskScoringModel
from app.schemas import RiskTrendResponse, TopRiskItem, TopRiskResponse, VendorScoreRequest, VendorScoreResponse

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/score-vendor", response_model=VendorScoreResponse, summary="Score a vendor risk profile")
async def score_vendor(
    request: VendorScoreRequest,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: RiskScoringModel = Depends(get_risk_model),
) -> VendorScoreResponse:
    try:
        result = model.score_vendor(request.model_dump())
        record = VendorRiskScore(
            tenant_id=tenant_id,
            vendor_id=request.vendor_id,
            risk_score=result["risk_score"],
            risk_level=result["risk_level"],
            factors_json=json.dumps(result["factors"]),
            calculated_at=result["calculated_at"],
        )
        db.add(record)
        db.commit()
        return VendorScoreResponse(
            tenant_id=tenant_id,
            vendor_id=request.vendor_id,
            risk_score=result["risk_score"],
            risk_level=result["risk_level"],
            factors=result["factors"],
            calculated_at=result["calculated_at"],
        )
    except ValueError as exc:
        db.rollback()
        logger.warning("Vendor scoring validation failed: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        db.rollback()
        logger.exception("Vendor scoring failed")
        raise HTTPException(status_code=500, detail="Unable to score vendor") from exc


@router.get("/trend/{vendor_id}", response_model=RiskTrendResponse, summary="Predict vendor risk trend")
async def get_vendor_trend(
    vendor_id: str,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: RiskScoringModel = Depends(get_risk_model),
) -> RiskTrendResponse:
    try:
        history = (
            db.query(VendorRiskScore)
            .filter(VendorRiskScore.tenant_id == tenant_id, VendorRiskScore.vendor_id == vendor_id)
            .order_by(VendorRiskScore.calculated_at.asc())
            .all()
        )
        if len(history) < 2:
            raise HTTPException(status_code=404, detail="Not enough historical scores for trend analysis")
        scores = [item.risk_score for item in history]
        result = model.predictRiskTrend(vendor_id, scores)
        return RiskTrendResponse(**result)
    except HTTPException:
        raise
    except ValueError as exc:
        logger.warning("Trend calculation validation failed: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Trend calculation failed")
        raise HTTPException(status_code=500, detail="Unable to calculate trend") from exc


@router.get("/top-risks/{tenant_id}", response_model=TopRiskResponse, summary="Get top vendor risks")
async def get_top_risks(
    tenant_id: str,
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
    model: RiskScoringModel = Depends(get_risk_model),
) -> TopRiskResponse:
    try:
        rows = (
            db.query(VendorRiskScore)
            .filter(VendorRiskScore.tenant_id == tenant_id)
            .order_by(VendorRiskScore.vendor_id.asc(), VendorRiskScore.calculated_at.desc())
            .all()
        )
        latest_by_vendor = {}
        for row in rows:
            latest_by_vendor.setdefault(
                row.vendor_id,
                {
                    "vendor_id": row.vendor_id,
                    "risk_score": row.risk_score,
                    "risk_level": row.risk_level,
                    "calculated_at": row.calculated_at,
                    "factors": row.factors(),
                },
            )
        result = model.get_top_risks(tenant_id, limit=limit, vendor_records=latest_by_vendor.values())
        return TopRiskResponse(
            tenant_id=result["tenant_id"],
            limit=result["limit"],
            total_items=result["total_items"],
            items=[TopRiskItem(**item) for item in result["items"]],
        )
    except Exception as exc:
        logger.exception("Top risk retrieval failed")
        raise HTTPException(status_code=500, detail="Unable to fetch top risks") from exc
