#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ] && ["$TRAVIS_TAG" == '']; then
openssl aes-256-cbc -K $encrypted_30d9879477f5_key -iv $encrypted_30d9879477f5_iv -in etc/codesigning.asc.enc -out etc/codesigning.asc -d
mvn clean package dokka:javadocJar deploy -Ddeploy --settings etc/deploy-settings.xml
fi