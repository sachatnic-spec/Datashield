"""ML Model for PII Detection."""
import logging
import re
from typing import Dict, List

logger = logging.getLogger(__name__)


class PIIPattern:
    """Pattern-based PII detection with confidence scoring."""

    PATTERNS = {
        "aadhaar": (r"\b[0-9]{4}\s?[0-9]{4}\s?[0-9]{4}\b", 0.95),
        "pan": (r"[A-Z]{5}[0-9]{4}[A-Z]{1}", 0.95),
        "credit_card": (r"\b(?:\d{4}[-\s]?){3}\d{4}\b", 0.90),
        "email": (r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}", 0.85),
        "phone": (r"(?:\+91|0)?[6-9]\d{9}\b", 0.80),
        "passport": (r"[A-Z]{1}[0-9]{7}", 0.90),
        "driver_license": (r"[A-Z]{2}[0-9]{13}", 0.85),
        "bank_account": (r"\b[0-9]{9,18}\b", 0.70),
        "ssn": (r"\b\d{3}-\d{2}-\d{4}\b", 0.95),
        "ipv4": (r"\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b", 0.85),
        "medical": (r"(?i)(patient|prescription|diagnosis|treatment|medication)\s+[A-Za-z0-9#]+", 0.75),
        "dob": (r"\b(?:19|20)\d{2}[-/](?:0[1-9]|1[0-2])[-/](?:0[1-9]|[12][0-9]|3[01])\b", 0.80),
        "address": (r"\b\d+\s+[A-Za-z]+\s+(?:Street|St|Road|Rd|Avenue|Ave|Boulevard|Blvd|Lane|Ln)\b", 0.65),
        "name": (r"\b(?:[A-Z][a-z]+\s+)+[A-Z][a-z]+\b", 0.60),
    }

    @staticmethod
    def detect(text: str) -> List[Dict]:
        findings: List[Dict] = []
        for pii_type, (pattern, base_confidence) in PIIPattern.PATTERNS.items():
            for match in re.finditer(pattern, text):
                findings.append(
                    {
                        "pii_type": pii_type,
                        "matched_value": match.group(),
                        "confidence": min(base_confidence, 0.99),
                        "start": match.start(),
                        "end": match.end(),
                        "context": text[max(0, match.start() - 50):min(len(text), match.end() + 50)],
                    }
                )
        return sorted(findings, key=lambda item: item["confidence"], reverse=True)


class ContextualPIIDetector:
    CONTEXT_KEYWORDS = {
        "medical": ["patient", "diagnosis", "treatment", "medication", "hospital", "doctor"],
        "financial": ["account", "credit", "debit", "card", "bank", "transaction"],
        "identity": ["passport", "license", "id", "identification", "ssn", "pan"],
        "personal": ["name", "address", "phone", "email", "date of birth", "dob"],
    }

    @staticmethod
    def get_context_boost(text: str) -> float:
        text_lower = text.lower()
        boost = 0.0
        for keywords in ContextualPIIDetector.CONTEXT_KEYWORDS.values():
            if any(keyword in text_lower for keyword in keywords):
                boost += 0.1
        return min(boost, 0.3)


class PIIDetectionModel:
    """Main PII Detection Model combining patterns and context scoring."""

    def __init__(self):
        logger.info("Initializing PII detection model")
        self.patterns = PIIPattern()
        self.context_detector = ContextualPIIDetector()

    def detect_pii(self, text: str, threshold: float = 0.70) -> List[Dict]:
        if not text or len(text) < 3:
            return []
        findings = self.patterns.detect(text)
        context_boost = self.context_detector.get_context_boost(text)
        results: List[Dict] = []
        for finding in findings:
            boosted_confidence = min(finding["confidence"] + context_boost, 1.0)
            if boosted_confidence >= threshold:
                finding["confidence"] = boosted_confidence
                finding["requires_review"] = boosted_confidence < 0.85
                results.append(finding)
        return results

    def redact_pii(self, text: str, redaction_char: str = "*") -> str:
        findings = self.detect_pii(text)
        redacted = text
        for finding in sorted(findings, key=lambda item: item["start"], reverse=True):
            replacement = redaction_char * len(finding["matched_value"])
            redacted = redacted[:finding["start"]] + replacement + redacted[finding["end"]:]
        return redacted

    def batch_detect(self, texts: List[str], threshold: float = 0.70) -> List[List[Dict]]:
        return [self.detect_pii(text, threshold) for text in texts]

    def get_model_stats(self) -> Dict:
        return {
            "model_type": "hybrid_regex_context",
            "pii_types_supported": list(self.patterns.PATTERNS.keys()),
            "total_patterns": len(self.patterns.PATTERNS),
            "version": "1.0.0",
        }
