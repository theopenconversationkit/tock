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

# The e2e scenario (.github/workflows/e2e.yml) must have succeeded on the released commit.
# SKIP_E2E=1 bypasses this check.
if [ "${SKIP_E2E:-}" != "1" ]; then
  GITHUB_REPOSITORY=theopenconversationkit/tock E2E_REF="$(git branch --show-current)" \
    "$(dirname "$0")/check-e2e.sh" || exit 1
fi

mvn release:clean release:prepare -Ddeploy
mvn release:perform -Ddeploy -T 20C
