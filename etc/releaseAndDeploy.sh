#!/usr/bin/env bash
mvn release:clean release:prepare -Ddeploy
mvn dokka:javadocJar release:perform -Ddeploy