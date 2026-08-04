"""Core business logic for vendor risk scoring."""
from __future__ import annotations

from datetime import datetime
from typing import Any, Dict, Iterable, List, Mapping, Optional
import math
import statistics


class RiskScoringModel:
    """Implements weighted vendor risk scoring and trend analysis."""

    WEIGHTS = {
        "security_score": 0.40,
        "compliance_score": 0.35,
        "operational_score": 0.15,
        "historical_score": 0.10,
    }

    def score_vendor(self, vendor_data: Mapping[str, Any]) -> Dict[str, Any]:
        normalized = self._normalize_vendor_data(vendor_data)
        weighted_score = round(
            sum(normalized[name] * weight for name, weight in self.WEIGHTS.items()),
            2,
        )
        return {
            "risk_score": weighted_score,
            "risk_level": self._risk_level(weighted_score),
            "factors": {
                "weights": self.WEIGHTS,
                "inputs": normalized,
                "weighted_components": {
                    key: round(normalized[key] * weight, 2)
                    for key, weight in self.WEIGHTS.items()
                },
            },
            "calculated_at": datetime.utcnow(),
        }

    def predictRiskTrend(self, vendor_id: str, historical_scores: Iterable[float]) -> Dict[str, Any]:
        scores = [float(value) for value in historical_scores]
        if len(scores) < 2:
            raise ValueError("At least two historical scores are required to calculate a trend")

        n = len(scores)
        x_values = list(range(n))
        x_mean = statistics.mean(x_values)
        y_mean = statistics.mean(scores)
        numerator = sum((x - x_mean) * (y - y_mean) for x, y in zip(x_values, scores))
        denominator = sum((x - x_mean) ** 2 for x in x_values)
        slope = numerator / denominator if denominator else 0.0
        intercept = y_mean - (slope * x_mean)
        predicted_next = max(0.0, min(100.0, intercept + slope * n))
        confidence = self._trend_confidence(scores, slope)

        if abs(slope) < 0.5:
            direction = "STABLE"
        elif slope > 0:
            direction = "INCREASING"
        else:
            direction = "DECREASING"

        return {
            "vendor_id": vendor_id,
            "trend_direction": direction,
            "slope": round(slope, 4),
            "intercept": round(intercept, 4),
            "predicted_next_score": round(predicted_next, 2),
            "confidence": round(confidence, 4),
            "samples": n,
        }

    def get_top_risks(
        self,
        tenant_id: str,
        limit: int = 10,
        vendor_records: Optional[Iterable[Mapping[str, Any]]] = None,
    ) -> Dict[str, Any]:
        ranked_records = sorted(
            list(vendor_records or []),
            key=lambda item: float(item["risk_score"]),
            reverse=True,
        )[:limit]
        return {
            "tenant_id": tenant_id,
            "limit": limit,
            "total_items": len(ranked_records),
            "items": ranked_records,
        }

    def _normalize_vendor_data(self, vendor_data: Mapping[str, Any]) -> Dict[str, float]:
        normalized: Dict[str, float] = {}
        for field_name in self.WEIGHTS:
            if field_name not in vendor_data:
                raise ValueError(f"Missing required risk factor: {field_name}")
            value = float(vendor_data[field_name])
            if value < 0 or value > 100:
                raise ValueError(f"{field_name} must be between 0 and 100")
            normalized[field_name] = value
        return normalized

    def _risk_level(self, risk_score: float) -> str:
        if risk_score >= 80:
            return "CRITICAL"
        if risk_score >= 60:
            return "HIGH"
        if risk_score >= 40:
            return "MEDIUM"
        return "LOW"

    def _trend_confidence(self, scores: List[float], slope: float) -> float:
        if len(scores) < 3:
            return 0.5
        volatility = statistics.pstdev(scores) if len(scores) > 1 else 0.0
        average = abs(statistics.mean(scores)) or 1.0
        stability = max(0.0, 1.0 - min(volatility / average, 1.0))
        slope_impact = min(abs(slope) / 10.0, 0.25)
        return max(0.5, min(0.99, stability + slope_impact))
