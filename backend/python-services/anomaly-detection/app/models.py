"""Core behavior anomaly detection logic."""
from __future__ import annotations

from collections import Counter
from datetime import datetime, timedelta
from typing import Any, Dict, Iterable, List, Mapping, Optional
import math
import statistics

from app.config import settings


class BehavioralAnomalyModel:
    """Implements baseline profiling and access anomaly detection."""

    def detect_access_anomaly(
        self,
        user_id: str,
        access_time: datetime,
        location: str,
        volume: float,
        profile: Optional[Mapping[str, Any]] = None,
    ) -> Dict[str, Any]:
        profile = dict(profile or {})
        baseline_volume = float(profile.get("average_volume", volume))
        volume_stddev = float(profile.get("volume_stddev", 0.0))
        typical_hours = {int(hour) for hour in profile.get("typical_hours", [])}
        home_location = profile.get("home_location", location)

        if volume_stddev > 0:
            volume_z_score = abs((volume - baseline_volume) / volume_stddev)
        else:
            volume_z_score = 0.0 if volume == baseline_volume else 3.0

        normalized_volume_score = min(volume_z_score / 3.0, 1.0)
        time_score = 0.0 if not typical_hours or access_time.hour in typical_hours else 1.0
        location_result = self.detect_location_anomaly(location, home_location)
        anomaly_score = min(
            1.0,
            round((normalized_volume_score * 0.5) + (time_score * 0.25) + (location_result["location_score"] * 0.25), 4),
        )
        return {
            "user_id": user_id,
            "anomaly_score": anomaly_score,
            "is_anomalous": anomaly_score >= 0.5,
            "location_score": location_result["location_score"],
            "volume_z_score": round(volume_z_score, 4),
            "time_score": round(time_score, 4),
            "flagged_unauthorized": self.flag_unauthorized_access(user_id, anomaly_score)["flagged_unauthorized"],
            "profile": profile,
            "evaluated_at": datetime.utcnow(),
        }

    def build_behavior_profile(self, user_id: str, historical_accesses: Iterable[Mapping[str, Any]]) -> Dict[str, Any]:
        now = datetime.utcnow()
        recent_records: List[Dict[str, Any]] = []
        for access in historical_accesses:
            access_time = access["access_time"]
            if isinstance(access_time, str):
                access_time = datetime.fromisoformat(access_time)
            if access_time >= now - timedelta(days=settings.PROFILE_LOOKBACK_DAYS):
                recent_records.append(
                    {
                        "access_time": access_time,
                        "location": access["location"],
                        "volume": float(access["volume"]),
                    }
                )

        if not recent_records:
            raise ValueError("At least one historical access record is required to build a behavior profile")

        volumes = [record["volume"] for record in recent_records]
        hours = [record["access_time"].hour for record in recent_records]
        locations = [record["location"].strip() for record in recent_records if record.get("location")]
        common_hours = [hour for hour, _ in Counter(hours).most_common(6)]
        home_location = Counter(locations).most_common(1)[0][0] if locations else "UNKNOWN"

        return {
            "user_id": user_id,
            "sample_size": len(recent_records),
            "average_volume": round(statistics.mean(volumes), 4),
            "volume_stddev": round(statistics.pstdev(volumes), 4) if len(volumes) > 1 else 0.0,
            "typical_hours": sorted(common_hours),
            "home_location": home_location,
            "last_updated": datetime.utcnow().isoformat(),
        }

    def flag_unauthorized_access(self, user_id: str, anomaly_score: float) -> Dict[str, Any]:
        flagged = anomaly_score > settings.UNAUTHORIZED_THRESHOLD
        return {
            "user_id": user_id,
            "anomaly_score": round(anomaly_score, 4),
            "flagged_unauthorized": flagged,
            "response_priority": "IMMEDIATE" if flagged else "MONITOR",
        }

    def detect_location_anomaly(self, current_location: str, user_home_location: str) -> Dict[str, Any]:
        current = (current_location or "").strip().lower()
        expected = (user_home_location or "").strip().lower()
        if not current or not expected:
            return {
                "location_score": 0.5,
                "is_anomalous": False,
                "reason": "Insufficient location context",
            }
        if current == expected:
            return {
                "location_score": 0.0,
                "is_anomalous": False,
                "reason": "Current location matches the baseline location",
            }

        current_parts = {part.strip() for part in current.replace("-", ",").split(",") if part.strip()}
        expected_parts = {part.strip() for part in expected.replace("-", ",").split(",") if part.strip()}
        overlap = len(current_parts & expected_parts)
        denominator = max(len(current_parts | expected_parts), 1)
        similarity = overlap / denominator
        location_score = round(1.0 - similarity, 4)
        return {
            "location_score": location_score,
            "is_anomalous": location_score >= 0.5,
            "reason": "Location diverges from the baseline pattern",
        }
