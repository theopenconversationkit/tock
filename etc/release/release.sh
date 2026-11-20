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

# Release steps of Tock and of its satellite repositories, run by .github/workflows/release.yml.
# Each step can also be run locally, on clones of the repositories side by side in $RELEASE_DIR.
#
# Usage: etc/release/release.sh <step> [args]
#
#   versions                 prints version, major and next (key=value lines, for $GITHUB_OUTPUT)
#   tock                     releases tock, then commits the versions displayed in the documentation
#   corenlp                  updates the tock parent of tock-corenlp, then releases it
#   open-data | demo         updates the tock version of tock-bot-open-data / tock-bot-demo, then releases it
#   docker-prepare <tar>     updates the versions of tock-docker, tags it (release:prepare only),
#                            then writes the sources of the tag to <tar>
#   docker-image <module>    builds (and pushes) the image of a JVM module, from the current directory
#                            (the sources of the tock-docker tag)
#   docker-finish            sets TAG of tock-docker/.env to the released version
#
# Environment:
#   VERSION        released version, such as 26.9.2 (versions: defaults to the tock version without -SNAPSHOT)
#   NEXT_VERSION   next development version (versions: defaults to the next patch, as a SNAPSHOT)
#   MAJOR          major Docker tag, such as 26.9 (defaults to VERSION without its patch number)
#   DRY_RUN        true (default): commits and tags stay local, the artifacts are installed in the local Maven
#                  repository but not published, and the images are built but not pushed
#   RELEASE_DIR    directory of the clones (tock, tock-corenlp, tock-bot-open-data, tock-bot-demo, tock-docker),
#                  defaults to the parent directory of this tock clone

set -euo pipefail

tock_dir="$(cd "$(dirname "$0")/../.." && pwd)"
RELEASE_DIR="${RELEASE_DIR:-$(dirname "$tock_dir")}"
DRY_RUN="${DRY_RUN:-true}"

dry_run() {
  [ "$DRY_RUN" != "false" ]
}

require_versions() {
  : "${VERSION:?VERSION is required}" "${NEXT_VERSION:?NEXT_VERSION is required}"
  MAJOR="${MAJOR:-${VERSION%.*}}"
}

# Runs the release plugin in a repository: release:prepare only builds what the pom requires (validate by default),
# release:perform builds the tag once, without the tests (they ran on the released commit), and deploys it.
# The perform build checks out the local clone, so that it needs no remote access.
# In dry run, the commits and the tag stay local, and the artifacts are neither signed nor published (but installed).
# The goals and preparationGoals set in a pom take precedence over these defaults.
release() {
  local dir="$1" goals="$2"
  shift 2
  local push=true arguments="-DskipTests $*"
  if dry_run; then
    push=false
    arguments="$arguments -DskipPublishing=true -Dgpg.skip=true"
  fi
  # shellcheck disable=SC2086 # one word per goal
  (cd "$dir" && mvn $goals -Ddeploy \
    -DreleaseVersion="$VERSION" -DdevelopmentVersion="$NEXT_VERSION" \
    -DpreparationGoals=validate -DlocalCheckout=true -DpushChanges="$push" -Darguments="$arguments")
}

# Commits the changes of a repository, when there are any, and pushes them unless in dry run
commit_and_push() {
  local dir="$1" message="$2"
  if git -C "$dir" diff --quiet; then
    echo "$dir: nothing to commit for '$message'"
  else
    git -C "$dir" commit -am "$message"
  fi
  if ! dry_run; then
    git -C "$dir" push origin HEAD
  fi
}

# Sets the value of <property>...</property> in a pom
set_property() {
  local pom="$1" property="$2" value="$3"
  grep -q "<$property>[^<]*</$property>" "$pom" || { echo "No <$property> in $pom" >&2; exit 1; }
  perl -pi -e "s{<\Q$property\E>[^<]*</\Q$property\E>}{<$property>$value</$property>}" "$pom"
}

step="${1:?Usage: $0 <step> [args]}"
shift

case "$step" in
  versions)
    current="$(sed -n 's:^    <version>\(.*\)</version>$:\1:p' "$tock_dir/pom.xml" | head -1)"
    VERSION="${VERSION:-${current%-SNAPSHOT}}"
    if ! [[ "$VERSION" =~ ^([0-9]+\.[0-9]+)\.([0-9]+)$ ]]; then
      echo "Invalid version '$VERSION', expected <major>.<minor>.<patch>" >&2
      exit 1
    fi
    major="${BASH_REMATCH[1]}"
    NEXT_VERSION="${NEXT_VERSION:-$major.$((BASH_REMATCH[2] + 1))-SNAPSHOT}"
    if ! [[ "$NEXT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]]; then
      echo "Invalid next version '$NEXT_VERSION', expected <major>.<minor>.<patch>-SNAPSHOT" >&2
      exit 1
    fi
    echo "version=$VERSION"
    echo "major=$major"
    echo "next=$NEXT_VERSION"
    ;;

  tock)
    require_versions
    release "$tock_dir" "release:clean release:prepare release:perform" "-T 1C"
    "$tock_dir/etc/update-doc-version.sh" "$VERSION"
    commit_and_push "$tock_dir" "doc $VERSION"
    ;;

  corenlp)
    require_versions
    dir="$RELEASE_DIR/tock-corenlp"
    # the first <version> after <parent> is the version of the tock parent
    perl -0pi -e "s{(<parent>.*?<version>)[^<]*(</version>)}{\${1}$VERSION\${2}}s" "$dir/pom.xml"
    commit_and_push "$dir" "tock $VERSION"
    release "$dir" "release:clean release:prepare release:perform"
    ;;

  open-data | demo)
    require_versions
    [ "$step" = open-data ] && dir="$RELEASE_DIR/tock-bot-open-data" || dir="$RELEASE_DIR/tock-bot-demo"
    set_property "$dir/pom.xml" tock "$VERSION"
    commit_and_push "$dir" "tock $VERSION"
    release "$dir" "release:clean release:prepare release:perform"
    ;;

  docker-prepare)
    require_versions
    tar="${1:?Usage: $0 docker-prepare <tar>}"
    dir="$RELEASE_DIR/tock-docker"
    for property in tock bot-open-data stanford demo; do
      set_property "$dir/pom.xml" "$property" "$VERSION"
    done
    commit_and_push "$dir" "Update Tock to $VERSION"
    # the images are built from the tag by the docker-image step, in parallel: no release:perform
    release "$dir" "release:clean release:prepare"
    git -C "$dir" archive --format=tar --output="$(realpath "$tar")" "tock-$VERSION"
    ;;

  docker-image)
    require_versions
    module="${1:?Usage: $0 docker-image <module>}"
    goals="package docker:build"
    dry_run || goals="$goals docker:push"
    # shellcheck disable=SC2086 # one word per goal
    mvn -pl "$module" -am $goals -Ddeploy -Dtock="$VERSION" -Dmajor="$MAJOR" -Dlatest=latest
    ;;

  docker-finish)
    require_versions
    dir="$RELEASE_DIR/tock-docker"
    sed -i.bak "s/^TAG=.*/TAG=$VERSION/" "$dir/.env"
    rm -f "$dir/.env.bak"
    commit_and_push "$dir" "$VERSION .env"
    ;;

  *)
    echo "Unknown step '$step'" >&2
    exit 1
    ;;
esac
