#!/usr/bin/env bash
echo "branch : $TRAVIS_BRANCH"
echo "pull_request : $TRAVIS_PULL_REQUEST"
echo "tag : $TRAVIS_TAG"
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" = 'false' ] && [ "$TRAVIS_TAG" = '' ]; then
  openssl aes-256-cbc -K $encrypted_30d9879477f5_key -iv $encrypted_30d9879477f5_iv -in etc/codesigning.asc.enc -out etc/codesigning.asc -d
  gpg --fast-import etc/codesigning.asc
  if [ "$TOCK_MILESTONE" = '' ];
  then
    mvn deploy -DskipTests=true -Dtravis --settings etc/deploy-settings.xml -U
  else
    export TOCK_VERSION=$(cat pom.xml | grep "^    <version>.*</version>$" | awk -F'[><]' '{print $3}')
    echo "tock version : $TOCK_VERSION"
    export TOCK_TAG=${TOCK_VERSION%"-SNAPSHOT"}
    echo "tock tag : build-$TOCK_TAG-$TRAVIS_BUILD_NUMBER"
    mvn versions:set -DnewVersion="build-$TOCK_TAG-$TRAVIS_BUILD_NUMBER"
    mvn deploy -Dmilestone --settings etc/deploy-settings.xml
  fi
fi