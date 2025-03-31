import json
from typing import Dict


def save_json(filename: str, data: Dict|list[dict]):
    """Save data as JSON file."""
    with open(filename, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)


def read_file(filepath: str) -> str:
    """Read and return file content."""
    with open(filepath, "r", encoding="utf-8") as f:
        return f.read()


def write_file(filepath: str, content: str):
    """Write content to file."""
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)
