"""
PII Redaction API routes
"""
from fastapi import APIRouter, Depends, HTTPException, Header, Request
from pydantic import BaseModel
from typing import List
from sqlalchemy.orm import Session
from app.database import get_db
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

class RedactionRequest(BaseModel):
    text: str
    redaction_char: str = "*"

class RedactionResponse(BaseModel):
    original_length: int
    redacted_text: str
    pii_count: int

@router.post("/redact", response_model=RedactionResponse)
async def redact_pii(
    request: RedactionRequest,
    http_request: Request,
    x_tenant_id: str = Header(..., alias="X-Tenant-ID"),
    db: Session = Depends(get_db)
):
    """Redact all detected PII in text"""
    try:
        pii_model = http_request.app.state.pii_model
        findings = pii_model.detect_pii(request.text)
        redacted = pii_model.redact_pii(request.text, request.redaction_char)
        
        return RedactionResponse(
            original_length=len(request.text),
            redacted_text=redacted,
            pii_count=len(findings)
        )
    except Exception as e:
        logger.error(f"Error redacting PII: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
