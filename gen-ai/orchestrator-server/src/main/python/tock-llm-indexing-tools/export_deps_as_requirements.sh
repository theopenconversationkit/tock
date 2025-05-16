#!/bin/bash

# Script used by maven to package those tools

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Define the file path
requirements_file="$SCRIPT_DIR/requirements.txt"

# Remove the gen-ai-orchestrator dependency that will referenced locally
pattern="orchestrator-server"

# Convert poetry to "requirements.txt"
poetry export --without-hashes --format=requirements.txt --output=$requirements_file

# -- remove absolute path reference to gen ai orchestrator server --
# Check if the platform is macOS (Darwin) or Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "/$pattern/d" "$requirements_file"
else
    # Linux
    sed -i "/$pattern/d" "$requirements_file"
fi
