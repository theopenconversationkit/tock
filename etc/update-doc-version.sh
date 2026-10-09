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

# Updates the versions displayed in the documentation (docs/mkdocs.yml, extra.variables):
# - tock_version: the given version, or the latest tock-* git tag
# - kotlin_version: the Kotlin version of bom/pom.xml
#
# Usage: etc/update-doc-version.sh [version]

set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
mkdocs_file="$root_dir/docs/mkdocs.yml"

version="${1:-$(git -C "$root_dir" describe --tags --abbrev=0 --match 'tock-*' | sed 's/^tock-//')}"
kotlin_version="$(sed -n 's:.*<kotlin>\(.*\)</kotlin>.*:\1:p' "$root_dir/bom/pom.xml" | head -1)"

if [[ -z "$version" || -z "$kotlin_version" ]]; then
  echo "Unable to determine the Tock or Kotlin version" >&2
  exit 1
fi

sed -i.bak \
  -e "s/^\(    tock_version:\).*/\1 $version/" \
  -e "s/^\(    kotlin_version:\).*/\1 $kotlin_version/" \
  "$mkdocs_file"
rm -f "$mkdocs_file.bak"

echo "Documentation versions: tock_version=$version, kotlin_version=$kotlin_version"
