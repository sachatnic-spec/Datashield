"""Unit tests for the anomaly detection model."""
from datetime import datetime, timedelta
from pathlib import Path
import sys
import unittest

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.models import BehavioralAnomalyModel


class BehavioralAnomalyModelTests(unittest.TestCase):
    def setUp(self) -> None:
        self.model = BehavioralAnomalyModel()
        now = datetime.utcnow()
        self.history = [
            {"access_time": now - timedelta(days=offset), "location": "Bengaluru, IN", "volume": 10 + offset}
            for offset in range(5)
        ]

    def test_build_behavior_profile_uses_recent_history(self) -> None:
        profile = self.model.build_behavior_profile("user-1", self.history)
        self.assertEqual(profile["user_id"], "user-1")
        self.assertEqual(profile["sample_size"], 5)
        self.assertEqual(profile["home_location"], "Bengaluru, IN")

    def test_detect_access_anomaly_flags_large_variance(self) -> None:
        profile = self.model.build_behavior_profile("user-1", self.history)
        result = self.model.detect_access_anomaly(
            "user-1",
            datetime.utcnow(),
            "Delhi, IN",
            80,
            profile,
        )
        self.assertTrue(result["is_anomalous"])
        self.assertGreater(result["anomaly_score"], 0.5)


if __name__ == "__main__":
    unittest.main()
