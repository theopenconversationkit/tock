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

cp nlp/api/doc/src/main/doc/*.* docs/api/
cp bot/connector-web/web-connector.html docs/api/
cp bot/connector-web/Swagger_TOCKWebConnector.yaml docs/api/

rm -rf docs/fr/*
rm -rf docs/fr-dark/*
rm -rf docs/en/*
rm -rf docs/en-dark/*

mkdocs build -d ../../docs/fr/ -f docs-mk/fr/mkdocs.yml
mkdocs build -d ../../docs/fr-dark/ -f docs-mk/fr/mkdocs-dark.yml
mkdocs build -d ../../docs/en/ -f docs-mk/en/mkdocs.yml
mkdocs build -d ../../docs/en-dark/ -f docs-mk/en/mkdocs-dark.yml
