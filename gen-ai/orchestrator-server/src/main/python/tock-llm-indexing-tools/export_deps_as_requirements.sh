#!/bin/bash

#
# Copyright (C) 2017/2021 e-voyageurs technologies
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
