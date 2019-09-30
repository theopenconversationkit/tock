#!/usr/bin/env bash

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
  "https://api.travis-ci.org/repo/$1%2F$2/requests"
}

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ] && [ "$TRAVIS_TAG" = '' ]; then
  travis-api "theopenconversationkit" "tock-corenlp"
  travis-api "theopenconversationkit" "tock-bot-open-data"
fi
