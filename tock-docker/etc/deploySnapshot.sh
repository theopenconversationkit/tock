#!/usr/bin/env bash
mvn clean validate package docker:build docker:push --settings etc/deploy-settings.xml