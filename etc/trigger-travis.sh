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
  travis-api "voyages-sncf-technologies" "tock-corenlp"
  travis-api "voyages-sncf-technologies" "tock-bot-open-data"
  travis-api "voyages-sncf-technologies" "tock-docker"
fi
