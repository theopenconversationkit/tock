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

cd dokka
mvn clean dokka:dokka
cd ..
rm -rf docs/dokka
cp -Rf dokka/target/* docs

cd docs-mk/en
rm -rf site
mkdocs build
rm -rf ../../docs/en/*
cp -Rf site/* ../../docs/en
rm -rf site
mkdocs build -f mkdocs-dark.yml
rm -rf ../../docs/en-dark/*
cp -Rf site/* ../../docs/en-dark
rm -rf site

cd ../fr
rm -rf site
mkdocs build
rm -rf ../../docs/fr/*
cp -Rf site/* ../../docs/fr
rm -rf site
mkdocs build -f mkdocs-dark.yml
rm -rf ../../docs/fr-dark/*
cp -Rf site/* ../../docs/fr-dark
rm -rf site

cd ../..
