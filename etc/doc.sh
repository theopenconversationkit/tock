#!/usr/bin/env bash
cd dokka
mvn clean dokka:dokka
cd ../docs-mk/en
rm -rf site
mkdocs build
cd ../fr
rm -rf site
mkdocs build

cd ../..

rm -rf docs/dokka
cp -rf dokka/target/* docs
rm -rf docs/en/*
cp -rf docs-mk/en/site/* docs/en
rm -rf docs/fr/*
cp -rf docs-mk/fr/site/* docs/fr