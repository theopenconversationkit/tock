#   Copyright (C) 2025-2026 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""Write an HTML redirect page for each old page listed in redirects.yml.

mkdocs-redirects does not support the i18n "folder" structure (it expects the
language folder in every path), so the redirects are generated here, once per
language build: the i18n plugin runs a nested build per extra language, in the
same site_dir, with pages written under a "<locale>/" prefix.
"""
import logging
import posixpath
from pathlib import Path

import yaml

log = logging.getLogger('mkdocs.hooks.redirects')

TEMPLATE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Redirecting...</title>
<link rel="canonical" href="{url}">
<meta http-equiv="refresh" content="0; url={url}">
<script>location.replace("{url}" + location.hash)</script>
</head>
<body>Redirecting to <a href="{url}">{url}</a>...</body>
</html>
"""


def _html(path):
    return path[: -len('.md')] + '.html'


def on_post_build(config, **kwargs):
    redirects_file = Path(config.config_file_path).parent / 'redirects.yml'
    redirects = yaml.safe_load(redirects_file.read_text(encoding='utf-8')) or {}
    site_dir = Path(config.site_dir)
    i18n = config.plugins.get('i18n')
    if i18n and i18n.current_language != i18n.default_language:
        site_dir = site_dir / i18n.current_language
    for old, new in redirects.items():
        old_html, new_html = _html(old), _html(new)
        if not (site_dir / new_html).exists():
            log.warning("Redirect target '%s' does not exist", new)
            continue
        target = posixpath.relpath(new_html, posixpath.dirname(old_html) or '.')
        dest = site_dir / old_html
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(TEMPLATE.format(url=target), encoding='utf-8')
