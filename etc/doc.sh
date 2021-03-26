#!/usr/bin/env bash
#
# Copyright (C) 2017/2021 e-voyageurs technologies
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
cd ../docs-mk/en
rm -rf site
mkdocs build
cd ../fr
rm -rf site
mkdocs build

cd ../..

rm -rf docs/dokka
cp -Rf dokka/target/* docs
rm -rf docs/en/*
cp -Rf docs-mk/en/site/* docs/en
rm -rf docs/fr/*
cp -Rf docs-mk/fr/site/* docs/fr
