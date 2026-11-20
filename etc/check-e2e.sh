#!/usr/bin/env bash
#
# Copyright (C) 2017/2025 SNCF Connect & Tech
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

# Checks, with the GitHub CLI, that the e2e scenario (.github/workflows/e2e.yml) succeeded on a commit.
#
# Usage: etc/check-e2e.sh [sha]   (HEAD by default)
# The repository is $GITHUB_REPOSITORY when defined (GitHub Actions), theopenconversationkit/tock otherwise.
# E2E_REF is the branch suggested to run the scenario on (master by default).

set -euo pipefail

repo="${GITHUB_REPOSITORY:-theopenconversationkit/tock}"
sha="${1:-$(git rev-parse HEAD)}"

if ! run="$(gh run list --repo "$repo" --workflow e2e.yml --commit "$sha" --status success \
  --limit 1 --json url --jq '.[0].url // empty')"; then
  echo "Unable to check the e2e runs with the GitHub CLI (gh auth login?)" >&2
  exit 1
fi
if [ -z "$run" ]; then
  echo "No successful e2e run on $sha: run it, then release again once it succeeded:" >&2
  echo "  gh workflow run e2e.yml --repo $repo --ref ${E2E_REF:-master}" >&2
  echo "  gh run watch --repo $repo" >&2
  exit 1
fi
echo "e2e OK on $sha: $run"
