"""Unit tests for the AI analysis model."""
from pathlib import Path
import sys
import unittest

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.models import AIAnalysisModel


class AIAnalysisModelTests(unittest.TestCase):
    def setUp(self) -> None:
        self.model = AIAnalysisModel()

    def test_detect_anomaly_returns_high_severity_for_large_spike(self) -> None:
        result = self.model.detect_anomaly("BREACH_INCIDENT_COUNT", 90, [10, 12, 11, 13, 12])
        self.assertEqual(result["severity"], "CRITICAL")
        self.assertTrue(result["is_alert_triggered"])

    def test_forecast_trend_projects_growth(self) -> None:
        result = self.model.forecastTrend("DSAR_REQUESTS", [10, 15, 20, 25], 7)
        self.assertGreater(result["predicted_value"], result["current_value"])
        self.assertGreater(result["trend_direction"], 0)


if __name__ == "__main__":
    unittest.main()
