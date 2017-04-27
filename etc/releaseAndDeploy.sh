#!/usr/bin/env bash
mvn release:clean release:prepare -Ddeploy
mvn release:perform -Ddeploy