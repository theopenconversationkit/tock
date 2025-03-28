import logging
import os
import sys
import traceback
import warnings
from datetime import datetime

import colorlog

app_logger = None

def configure_logging(cli_args):
    global app_logger
    if app_logger is not None:
        return app_logger

    app_logger = logging.getLogger(__name__)
    log_level = logging.DEBUG if cli_args['-v'] else logging.INFO
    app_logger.setLevel(log_level)
    app_logger.propagate = False

    os.makedirs("logs", exist_ok=True)
    log_format = "%(asctime)s - %(name)s - %(levelname)s - %(module)s - %(message)s"
    log_filename = f"logs/log_{datetime.now().strftime('%Y-%m-%d_%Hh%Mm%S')}.log"

    file_handler = logging.FileHandler(log_filename)
    file_handler.setFormatter(logging.Formatter(log_format))
    app_logger.addHandler(file_handler)

    console_handler = colorlog.StreamHandler()
    console_handler.setFormatter(colorlog.ColoredFormatter(f"%(log_color)s{log_format}"))
    app_logger.addHandler(console_handler)

    langfuse_logger = logging.getLogger("langfuse")
    langfuse_logger.setLevel(log_level)
    langfuse_logger.addHandler(file_handler)
    langfuse_logger.addHandler(console_handler)
    langfuse_logger.propagate = False

    opensearch_logger = logging.getLogger("opensearch")
    opensearch_logger.setLevel(logging.INFO)
    opensearch_logger.addHandler(file_handler)
    opensearch_logger.addHandler(console_handler)
    opensearch_logger.propagate = False

    gen_ai_orchestrator_logger = logging.getLogger("gen_ai_orchestrator")
    gen_ai_orchestrator_logger.setLevel(log_level)
    gen_ai_orchestrator_logger.addHandler(file_handler)
    gen_ai_orchestrator_logger.addHandler(console_handler)
    gen_ai_orchestrator_logger.propagate = False

    httpx_logger = logging.getLogger("httpx")
    httpx_logger.setLevel(log_level)
    httpx_logger.addHandler(file_handler)
    httpx_logger.addHandler(console_handler)
    httpx_logger.propagate = False

    openai_logger = logging.getLogger("openai")
    openai_logger.setLevel(logging.INFO)
    openai_logger.addHandler(file_handler)
    openai_logger.addHandler(console_handler)
    openai_logger.propagate = False

    langfuse_logger = logging.getLogger("backoff")
    langfuse_logger.setLevel(log_level)
    langfuse_logger.addHandler(file_handler)
    langfuse_logger.addHandler(console_handler)
    langfuse_logger.propagate = False

    def handle_exception(exc_type, exc_value, exc_traceback):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        log_message = "".join(traceback.format_exception(exc_type, exc_value, exc_traceback))
        app_logger.error("Unhandled exception:\n%s", log_message)

    def log_warning(message, category, filename, lineno, file=None, line=None):
        app_logger.warning(f"{category.__name__}: {message} (from {filename}, line {lineno})")

    sys.excepthook = handle_exception
    warnings.showwarning = log_warning

    return app_logger