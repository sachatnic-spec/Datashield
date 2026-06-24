"""Reusable FastAPI dependencies."""
from fastapi import Header, HTTPException, Request

from app.models import AIAnalysisModel


async def require_tenant_id(x_tenant_id: str = Header(..., alias="X-Tenant-ID")) -> str:
    tenant_id = x_tenant_id.strip()
    if not tenant_id:
        raise HTTPException(status_code=400, detail="X-Tenant-ID header is required")
    return tenant_id


async def get_ai_model(request: Request) -> AIAnalysisModel:
    model = getattr(request.app.state, "ai_model", None)
    if model is None:
        raise HTTPException(status_code=503, detail="AI analysis model is unavailable")
    return model
