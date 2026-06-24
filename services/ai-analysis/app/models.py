"""Core AI analysis model logic."""
from __future__ import annotations

from datetime import datetime
from typing import Dict, Iterable, List, Mapping, Optional
import statistics

from app.config import settings


class AIAnalysisModel:
    """Implements anomaly scoring, linear forecasting, and aggregated insights."""

    def detect_anomaly(self, metric_type: str, current_value: float, historical_values: Iterable[float]) -> Dict[str, object]:
        values = [float(value) for value in historical_values]
        if len(values) < 2:
            raise ValueError("At least two historical values are required for anomaly detection")

        baseline = statistics.mean(values)
        stddev = statistics.pstdev(values) if len(values) > 1 else 0.0
        deviation_percentage = ((current_value - baseline) / baseline) * 100 if baseline else 0.0
        z_score = abs((current_value - baseline) / stddev) if stddev else 0.0
        anomaly_score = min(round(z_score / 3.0, 4), 1.0)
        severity = self._severity(anomaly_score, deviation_percentage)
        return {
            "metric_type": metric_type,
            "metric_value": round(float(current_value), 4),
            "baseline_value": round(baseline, 4),
            "deviation_percentage": round(deviation_percentage, 4),
            "anomaly_score": anomaly_score,
            "severity": severity,
            "detection_method": "Z_SCORE",
            "is_alert_triggered": anomaly_score >= settings.ALERT_THRESHOLD,
            "description": self._description(metric_type, deviation_percentage, severity),
            "detected_at": datetime.utcnow(),
        }

    def forecastTrend(self, metric_type: str, values: Iterable[float], forecast_days: int) -> Dict[str, object]:
        samples = [float(value) for value in values]
        if len(samples) < 2:
            raise ValueError("At least two values are required for forecasting")

        n = len(samples)
        x_values = list(range(n))
        x_mean = statistics.mean(x_values)
        y_mean = statistics.mean(samples)
        numerator = sum((x - x_mean) * (y - y_mean) for x, y in zip(x_values, samples))
        denominator = sum((x - x_mean) ** 2 for x in x_values)
        slope = numerator / denominator if denominator else 0.0
        intercept = y_mean - (slope * x_mean)
        current_value = samples[-1]
        predicted_value = current_value + (slope * forecast_days)
        confidence = self._forecast_confidence(samples)
        return {
            "metric_type": metric_type,
            "forecast_days": forecast_days,
            "current_value": round(current_value, 4),
            "predicted_value": round(predicted_value, 4),
            "confidence_interval": round(confidence, 4),
            "forecast_summary": self._forecast_summary(metric_type, current_value, predicted_value, forecast_days),
            "trend_direction": round(slope, 4),
            "generated_at": datetime.utcnow(),
        }

    def generate_insights(
        self,
        tenant_id: str,
        anomalies: Optional[Iterable[Mapping[str, object]]] = None,
        forecasts: Optional[Iterable[Mapping[str, object]]] = None,
    ) -> Dict[str, object]:
        anomaly_list = list(anomalies or [])
        forecast_list = list(forecasts or [])
        distribution: Dict[str, int] = {}
        critical = 0
        for item in anomaly_list:
            metric = str(item["metric_type"])
            distribution[metric] = distribution.get(metric, 0) + 1
            if str(item["severity"]).upper() == "CRITICAL":
                critical += 1
        avg_confidence = round(
            statistics.mean(float(item["confidence_interval"]) for item in forecast_list) if forecast_list else 0.0,
            4,
        )
        return {
            "tenant_id": tenant_id,
            "total_anomalies": len(anomaly_list),
            "critical_anomalies": critical,
            "anomaly_distribution": distribution,
            "active_forecasts": len(forecast_list),
            "avg_forecast_confidence": avg_confidence,
        }

    def _severity(self, anomaly_score: float, deviation_percentage: float) -> str:
        absolute_deviation = abs(deviation_percentage)
        if anomaly_score >= 0.9 or absolute_deviation >= 80:
            return "CRITICAL"
        if anomaly_score >= 0.75 or absolute_deviation >= 50:
            return "HIGH"
        if anomaly_score >= 0.5 or absolute_deviation >= 30:
            return "MEDIUM"
        return "LOW"

    def _description(self, metric_type: str, deviation_percentage: float, severity: str) -> str:
        direction = "increased" if deviation_percentage >= 0 else "decreased"
        return f"{metric_type} {direction} by {abs(deviation_percentage):.2f}%. Severity: {severity}."

    def _forecast_confidence(self, values: List[float]) -> float:
        if len(values) < 3:
            return 0.5
        mean_value = abs(statistics.mean(values)) or 1.0
        variability = statistics.pstdev(values) if len(values) > 1 else 0.0
        return max(0.5, min(0.99, 1.0 - min(variability / mean_value, 0.49)))

    def _forecast_summary(self, metric_type: str, current_value: float, predicted_value: float, forecast_days: int) -> str:
        direction = "increase" if predicted_value >= current_value else "decrease"
        return (
            f"{metric_type} is projected to {direction} over the next {forecast_days} days "
            f"from {current_value:.2f} to {predicted_value:.2f}."
        )
