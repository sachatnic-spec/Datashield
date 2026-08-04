"""
Bulk PII detection operations
"""
from fastapi import APIRouter, Depends, HTTPException, Header, Request
from pydantic import BaseModel
from typing import List
from sqlalchemy.orm import Session
from app.database import get_db
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

class BulkDetectionRequest(BaseModel):
    texts: List[str]
    max_batch: int = 100

class BulkDetectionResponse(BaseModel):
    total_processed: int
    total_pii_found: int
    processing_time_ms: float

@router.post("/bulk-detect", response_model=BulkDetectionResponse)
async def bulk_detect(
    request: BulkDetectionRequest,
    http_request: Request,
    x_tenant_id: str = Header(..., alias="X-Tenant-ID"),
    db: Session = Depends(get_db)
):
    """Bulk detect PII in multiple texts"""
    import time
    
    start = time.time()
    try:
        pii_model = http_request.app.state.pii_model
        total_pii = 0
        for text in request.texts[:request.max_batch]:
            findings = pii_model.detect_pii(text)
            total_pii += len(findings)
        
        elapsed = (time.time() - start) * 1000
        
        return BulkDetectionResponse(
            total_processed=len(request.texts[:request.max_batch]),
            total_pii_found=total_pii,
            processing_time_ms=elapsed
        )
    except Exception as e:
        logger.error(f"Error in bulk detection: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
