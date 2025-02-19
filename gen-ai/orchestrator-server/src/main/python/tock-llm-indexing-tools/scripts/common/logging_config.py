import logging
import os
import sys
import traceback
from datetime import datetime
import colorlog

app_logger = None

def configure_logging(cli_args):
    global app_logger
    if app_logger is not None:
        return app_logger

    # Créer un répertoire "logs" s'il n'existe pas
    os.makedirs("logs", exist_ok=True)

    # Format de log pour la console (avec coloration)
    log_format = "%(asctime)s - %(name)s - %(levelname)s - %(module)s - %(message)s"

    # Nom du fichier de log avec horodatage
    log_filename = f"logs/log_{datetime.now().strftime('%Y-%m-%d_%H-%M-%S')}.log"

    app_logger = logging.getLogger(__name__)

    # Configuration du gestionnaire de fichier (sans couleur pour le fichier)
    file_handler = logging.FileHandler(log_filename)
    file_handler.setFormatter(logging.Formatter(log_format))

    # Configuration du gestionnaire de console (avec coloration)
    console_handler = colorlog.StreamHandler()
    console_handler.setFormatter(colorlog.ColoredFormatter(f"%(log_color)s{log_format}"))

    # Configuration du logger
    app_logger.propagate = False

    log_level = logging.DEBUG if cli_args['-v'] else logging.INFO

    # Ajouter les gestionnaires au logger
    app_logger.setLevel(log_level)
    app_logger.addHandler(file_handler)
    app_logger.addHandler(console_handler)

    # Ajouter le handler du logger à langfuse
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

    # TODO MASS anglais
    # Capture des exceptions non gérées et log dans le fichier
    def handle_exception(exc_type, exc_value, exc_traceback):
        if issubclass(exc_type, KeyboardInterrupt):
            app_logger.warning("KeyboardInterrupt: User interruption detected, program closes automatically.")

        sys.__excepthook__(exc_type, exc_value, exc_traceback)  # Comportement par défaut

        log_message = "".join(traceback.format_exception(exc_type, exc_value, exc_traceback))
        app_logger.error("Unhandled exception:\n%s", log_message)

    # Rediriger toutes les erreurs vers le logger
    sys.excepthook = handle_exception

    return app_logger