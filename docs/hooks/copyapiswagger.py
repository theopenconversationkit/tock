import os
import shutil
from pathlib import Path
from mkdocs.plugins import BasePlugin

tock_apistatic = {
    "../../bot/connector-web/web-connector.html": "api/web-connector.html",
    "../../bot/connector-web/Swagger_TOCKWebConnector.yaml": "api/Swagger_TOCKWebConnector.yaml",
    "../../nlp/api/doc/src/main/doc/admin.html": "api/admin.html",
    "../../nlp/api/doc/src/main/doc/admin.yaml": "api/admin.yaml",
    "../../nlp/api/doc/src/main/doc/index.html": "api/index.html",
    "../../nlp/api/doc/src/main/doc/nlp.yaml": "api/nlp.yaml"
}

def on_pre_build(config):

    docs_dir = Path(config['docs_dir'])
    
    global tock_apistatic

    for source, relative_dest in tock_apistatic.items():

        destination = docs_dir / relative_dest
        source_path = docs_dir / source
        

        destination.parent.mkdir(parents=True, exist_ok=True)

        if destination.exists():
            try:
                destination.unlink()
            except Exception as e:
                print(f"Error deleting {destination}: {e}")

      
        try:
            shutil.copy(source_path, destination)
        except FileNotFoundError:
            print(f"Error: Source file {source_path} not found.")
        except Exception as e:
            print(f"Error copying {source_path} to {destination}: {e}")
