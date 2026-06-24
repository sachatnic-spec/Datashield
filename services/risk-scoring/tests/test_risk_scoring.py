"""Unit tests for the risk scoring model."""
from pathlib import Path
import sys
import unittest

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.models import RiskScoringModel


class RiskScoringModelTests(unittest.TestCase):
    def setUp(self) -> None:
        self.model = RiskScoringModel()

    def test_score_vendor_uses_weighted_formula(self) -> None:
        result = self.model.score_vendor(
            {
                "security_score": 80,
                "compliance_score": 70,
                "operational_score": 60,
                "historical_score": 50,
            }
        )
        self.assertEqual(result["risk_score"], 70.5)
        self.assertEqual(result["risk_level"], "HIGH")

    def test_predict_risk_trend_identifies_increase(self) -> None:
        trend = self.model.predictRiskTrend("vendor-1", [40, 45, 50, 55])
        self.assertEqual(trend["trend_direction"], "INCREASING")
        self.assertGreater(trend["predicted_next_score"], 55)


if __name__ == "__main__":
    unittest.main()
