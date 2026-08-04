"""API routes for behavioral anomaly detection."""
from __future__ import annotations

from datetime import datetime
import json
import logging

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import BehavioralAnomaly, UserProfile, get_db
from app.dependencies import get_behavior_model, require_tenant_id
from app.models import BehavioralAnomalyModel
from app.schemas import (
    AccessAnomalyRequest,
    AccessAnomalyResponse,
    BehaviorProfileResponse,
    LocationAnomalyResponse,
    UnauthorizedFlagRequest,
    UnauthorizedFlagResponse,
)

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/detect-access-anomaly", response_model=AccessAnomalyResponse, summary="Detect unusual access behavior")
async def detect_access_anomaly(
    request: AccessAnomalyRequest,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: BehavioralAnomalyModel = Depends(get_behavior_model),
) -> AccessAnomalyResponse:
    try:
        stored_profile = (
            db.query(UserProfile)
            .filter(UserProfile.tenant_id == tenant_id, UserProfile.user_id == request.user_id)
            .one_or_none()
        )
        profile = json.loads(stored_profile.profile_json) if stored_profile else None
        if request.historical_accesses:
            history_payload = [record.model_dump() for record in request.historical_accesses]
            profile = model.build_behavior_profile(request.user_id, history_payload)
            if stored_profile is None:
                stored_profile = UserProfile(
                    tenant_id=tenant_id,
                    user_id=request.user_id,
                    profile_json=json.dumps(profile),
                    home_location=profile["home_location"],
                    baseline_volume=profile["average_volume"],
                    volume_stddev=profile["volume_stddev"],
                    typical_hours_json=json.dumps(profile["typical_hours"]),
                    updated_at=datetime.utcnow(),
                )
                db.add(stored_profile)
            else:
                stored_profile.profile_json = json.dumps(profile)
                stored_profile.home_location = profile["home_location"]
                stored_profile.baseline_volume = profile["average_volume"]
                stored_profile.volume_stddev = profile["volume_stddev"]
                stored_profile.typical_hours_json = json.dumps(profile["typical_hours"])
                stored_profile.updated_at = datetime.utcnow()

        if profile is None:
            raise HTTPException(status_code=404, detail="No behavior profile found. Provide historical_accesses to build one.")

        result = model.detect_access_anomaly(
            user_id=request.user_id,
            access_time=request.access_time,
            location=request.location,
            volume=request.volume,
            profile=profile,
        )
        anomaly = BehavioralAnomaly(
            tenant_id=tenant_id,
            user_id=request.user_id,
            anomaly_score=result["anomaly_score"],
            anomaly_type="ACCESS_BEHAVIOR",
            location=request.location,
            access_volume=request.volume,
            access_time=request.access_time,
            details_json=json.dumps(result, default=str),
            flagged_unauthorized=result["flagged_unauthorized"],
        )
        db.add(anomaly)
        db.commit()
        return AccessAnomalyResponse(
            tenant_id=tenant_id,
            user_id=request.user_id,
            anomaly_score=result["anomaly_score"],
            is_anomalous=result["is_anomalous"],
            location_score=result["location_score"],
            volume_z_score=result["volume_z_score"],
            time_score=result["time_score"],
            flagged_unauthorized=result["flagged_unauthorized"],
            profile=result["profile"],
            evaluated_at=result["evaluated_at"],
        )
    except HTTPException:
        db.rollback()
        raise
    except ValueError as exc:
        db.rollback()
        logger.warning("Behavior profile validation failed: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        db.rollback()
        logger.exception("Access anomaly detection failed")
        raise HTTPException(status_code=500, detail="Unable to detect access anomaly") from exc


@router.get("/profile/{user_id}", response_model=BehaviorProfileResponse, summary="Get a stored behavior profile")
async def get_profile(
    user_id: str,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
) -> BehaviorProfileResponse:
    profile = (
        db.query(UserProfile)
        .filter(UserProfile.tenant_id == tenant_id, UserProfile.user_id == user_id)
        .one_or_none()
    )
    if profile is None:
        raise HTTPException(status_code=404, detail="Behavior profile not found")
    return BehaviorProfileResponse(
        tenant_id=tenant_id,
        user_id=user_id,
        profile=json.loads(profile.profile_json),
        updated_at=profile.updated_at,
    )


@router.post("/flag-unauthorized", response_model=UnauthorizedFlagResponse, summary="Evaluate unauthorized access risk")
async def flag_unauthorized_access(
    request: UnauthorizedFlagRequest,
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: BehavioralAnomalyModel = Depends(get_behavior_model),
) -> UnauthorizedFlagResponse:
    try:
        result = model.flag_unauthorized_access(request.user_id, request.anomaly_score)
        if request.anomaly_id:
            anomaly = (
                db.query(BehavioralAnomaly)
                .filter(BehavioralAnomaly.id == request.anomaly_id, BehavioralAnomaly.tenant_id == tenant_id)
                .one_or_none()
            )
            if anomaly is not None:
                anomaly.flagged_unauthorized = result["flagged_unauthorized"]
                db.commit()
        return UnauthorizedFlagResponse(**result)
    except Exception as exc:
        db.rollback()
        logger.exception("Unauthorized access flagging failed")
        raise HTTPException(status_code=500, detail="Unable to evaluate unauthorized access") from exc


@router.get("/location-anomaly/{user_id}", response_model=LocationAnomalyResponse, summary="Evaluate location anomaly")
async def detect_location_anomaly(
    user_id: str,
    current_location: str = Query(..., min_length=1),
    tenant_id: str = Depends(require_tenant_id),
    db: Session = Depends(get_db),
    model: BehavioralAnomalyModel = Depends(get_behavior_model),
) -> LocationAnomalyResponse:
    profile = (
        db.query(UserProfile)
        .filter(UserProfile.tenant_id == tenant_id, UserProfile.user_id == user_id)
        .one_or_none()
    )
    if profile is None:
        raise HTTPException(status_code=404, detail="Behavior profile not found")

    profile_payload = json.loads(profile.profile_json)
    result = model.detect_location_anomaly(current_location, profile_payload.get("home_location", profile.home_location))
    return LocationAnomalyResponse(
        user_id=user_id,
        current_location=current_location,
        expected_location=profile_payload.get("home_location", profile.home_location),
        location_score=result["location_score"],
        is_anomalous=result["is_anomalous"],
        reason=result["reason"],
    )
