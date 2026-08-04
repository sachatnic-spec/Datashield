"""PII detection API routes."""
from __future__ import annotations

import logging

from fastapi import APIRouter, Depends, HTTPException, Header, Request
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.database import PIIDetectionResult, get_db

logger = logging.getLogger(__name__)
router = APIRouter()


class DetectionRequest(BaseModel):
    text: str = Field(..., min_length=1)
    threshold: float = Field(default=0.7, ge=0, le=1)


class DetectionResponse(BaseModel):
    total_findings: int
    findings: list[dict]


@router.post("/detect", response_model=DetectionResponse)
async def detect_pii(
    request: DetectionRequest,
    http_request: Request,
    x_tenant_id: str = Header(..., alias="X-Tenant-ID"),
    db: Session = Depends(get_db),
) -> DetectionResponse:
    try:
        pii_model = getattr(http_request.app.state, "pii_model", None)
        if pii_model is None:
            raise HTTPException(status_code=503, detail="PII detection model is unavailable")
        findings = pii_model.detect_pii(request.text, request.threshold)
        for finding in findings:
            db.add(
                PIIDetectionResult(
                    tenant_id=x_tenant_id,
                    input_text=request.text,
                    pii_type=finding["pii_type"],
                    confidence_score=finding["confidence"],
                    context=finding.get("context"),
                    redacted_text=pii_model.redact_pii(request.text),
                    requires_human_review=finding.get("requires_review", False),
                )
            )
        db.commit()
        return DetectionResponse(total_findings=len(findings), findings=findings)
    except HTTPException:
        db.rollback()
        raise
    except Exception as exc:
        db.rollback()
        logger.exception("PII detection failed")
        raise HTTPException(status_code=500, detail="Unable to detect PII") from exc
