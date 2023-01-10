"""Logging configuration for the project.
"""

from loguru import logger
import sys

from .utils import touch_file

logger.remove()

# Add a stdout handler
logger.add(
    sink=sys.stdout,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> <level>{level: <8}</level> <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> <level>{message}</level>",
    level="DEBUG",
    colorize=True,
    backtrace=True,
    diagnose=True
)

# Add a file handler
logger.add(
    sink=touch_file("debug.log", "logs"),
    level="DEBUG",
    rotation="1 day",
    retention="7 days",
    compression="zip",
    serialize=False,
    backtrace=True,
    diagnose=True,
    enqueue=True
)
