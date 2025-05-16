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
export GPG_TTY=$(tty)
export MAVEN_OPTS="-Dkotlin.environment.keepalive=true"
echo "branch : $TRAVIS_BRANCH"
echo "pull_request : $TRAVIS_PULL_REQUEST"
echo "tag : $TRAVIS_TAG"
if [[ $TRAVIS_BRANCH = *"build"* || $TRAVIS_BRANCH = "master" ]] && [ "$TRAVIS_PULL_REQUEST" = 'false' ]; then
  openssl aes-256-cbc -K $encrypted_30d9879477f5_key -iv $encrypted_30d9879477f5_iv -in etc/codesigning.asc.enc -out etc/codesigning.asc -d
  gpg --fast-import --batch etc/codesigning.asc
  if [ "$TRAVIS_TAG" = '' ];
  then
    if [ "$SKIP_DEPLOY" != 'true' ]; then
      echo "deploy"
      mvn -B deploy -T4C -Dtravis --settings etc/deploy-settings.xml -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
    else
      echo "test"
      mvn install -T 2C -B -q
    fi
  else
    if [[ $TRAVIS_TAG == *"build"* ]];
    then
      export TOCK_VERSION=$(cat pom.xml | grep "^    <version>.*</version>$" | awk -F'[><]' '{print $3}')
      echo "tock version : $TOCK_VERSION"
      echo "set version to $TRAVIS_TAG"
      mvn versions:set -DnewVersion="$TRAVIS_TAG"
      echo "deploy"
      mvn deploy -T4C -B -Dmilestone --settings etc/deploy-settings.xml -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
    fi
  fi
  else
    if [ "$TRAVIS_PULL_REQUEST" = 'false' ];
    then
      echo "install and test"
      mvn install -T 2C -B -q
    else
      echo "test PR : $TRAVIS_PULL_REQUEST"
      mvn install -T 2C -B -q
    fi
fi
