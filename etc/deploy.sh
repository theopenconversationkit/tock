#!/usr/bin/env bash
echo "branch : $TRAVIS_BRANCH"
echo "pull_request : $TRAVIS_PULL_REQUEST"
echo "tag : $TRAVIS_TAG"
if [[ $TRAVIS_BRANCH = *"build"* || $TRAVIS_BRANCH = "master" ]] && [ "$TRAVIS_PULL_REQUEST" = 'false' ]; then
  openssl aes-256-cbc -K $encrypted_30d9879477f5_key -iv $encrypted_30d9879477f5_iv -in etc/codesigning.asc.enc -out etc/codesigning.asc -d
  gpg --fast-import etc/codesigning.asc
  if [ "$TRAVIS_TAG" = '' ];
  then
    mvn test -B -Dskip.npm -Dassembly.skipAssembly=true -U -q
    if [ "$SKIP_DEPLOY" != 'true' ]; then
      mvn deploy -T 4 -DskipTests=true -Dtravis --settings etc/deploy-settings.xml -U
    fi
  else
    if [[ $TRAVIS_TAG == *"build"* ]];
    then
      export TOCK_VERSION=$(cat pom.xml | grep "^    <version>.*</version>$" | awk -F'[><]' '{print $3}')
      echo "tock version : $TOCK_VERSION"
      echo "tock tag : $TRAVIS_TAG"
      mvn versions:set -DnewVersion="$TRAVIS_TAG"
      mvn install -B -Dskip.npm -Dassembly.skipAssembly=true -U -q
      mvn deploy -T 4 -DskipTests=true -Dmilestone --settings etc/deploy-settings.xml
    fi
  fi
  else
    mvn test -B -Dskip.npm -Dassembly.skipAssembly=true -U -q
fi