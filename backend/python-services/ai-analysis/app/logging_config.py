"""Logging configuration for the AI analysis service."""
from logging.config import dictConfig


def configure_logging(service_name: str, debug: bool = False) -> None:
    level = "DEBUG" if debug else "INFO"
    dictConfig(
        {
            "version": 1,
            "disable_existing_loggers": False,
            "formatters": {
                "standard": {
                    "format": "%(asctime)s | %(levelname)s | %(name)s | %(message)s"
                }
            },
            "handlers": {
                "default": {
                    "class": "logging.StreamHandler",
                    "formatter": "standard",
                }
            },
            "root": {"handlers": ["default"], "level": level},
            "loggers": {
                service_name: {
                    "handlers": ["default"],
                    "level": level,
                    "propagate": False,
                }
            },
        }
    )
