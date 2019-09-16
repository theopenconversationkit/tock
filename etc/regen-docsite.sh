#!/usr/bin/env bash

rm -rf docs/fr/*
rm -rf docs/en/*

mkdocs build -d ../../docs/fr/ -f docs-mk/fr/mkdocs.yml
mkdocs build -d ../../docs/en/ -f docs-mk/en/mkdocs.yml