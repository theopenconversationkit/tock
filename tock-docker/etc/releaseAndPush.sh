#!/usr/bin/env bash
echo "Release target: $1 -> version / $2 -> major"
mvn release:clean release:prepare release:perform -Ddeploy -Dtock="$1" -Dmajor="$2" -Dlatest=latest -Darguments="-Dtock=\"$1\" -Dmajor=\"$2\" -Dlatest=latest"