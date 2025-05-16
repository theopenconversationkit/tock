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

body='{
  "request": {
    "branch":"master"
  }
}'

function travis-api {
 echo "launch build for $1/$2"
 curl -s -X POST \
   -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token $TRAVIS_ACCESS_TOKEN" \
  -d "$body" \
  "https://api.travis-ci.com/repo/$1%2F$2/requests"
}

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ] && [ "$TRAVIS_TAG" = '' ]; then
  travis-api "theopenconversationkit" "tock-corenlp"
  travis-api "theopenconversationkit" "tock-bot-open-data"
fi
