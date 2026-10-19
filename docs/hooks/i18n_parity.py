#   Copyright (C) 2026 Credit Mutuel Arkea
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
"""Warn when a page exists in one language folder (docs/en, docs/fr) but not in the other.

A missing French page silently falls back to the English one: the warning makes `mkdocs build --strict` fail,
so that the translation is not forgotten. Pages excluded by `exclude_docs` are ignored.

With the TOCK_DOC_CHECK_TRANSLATION_DATES=true environment variable, it also warns when the last git commit of an
English page is more recent than the one of its French version (translation possibly out of date). This check
is noisy (a typo fix in English is enough), so it is disabled by default: run it without --strict, for instance
`TOCK_DOC_CHECK_TRANSLATION_DATES=true mkdocs build`.
"""
import logging
import os
import subprocess
from pathlib import Path

log = logging.getLogger('mkdocs.hooks.i18n_parity')

LANGUAGES = ('en', 'fr')
DEFAULT_LANGUAGE = 'en'


def _pages(docs_dir, language, exclude_docs):
    root = docs_dir / language
    pages = set()
    for path in root.rglob('*.md'):
        relative = path.relative_to(root).as_posix()
        if exclude_docs and exclude_docs.match_file(f'{language}/{relative}'):
            continue
        pages.add(relative)
    return pages


def _last_commit_time(path):
    """Timestamp of the last commit of the file, or None if it is not committed."""
    result = subprocess.run(
        ['git', 'log', '-1', '--format=%ct', '--', path.name],
        cwd=path.parent, capture_output=True, text=True, check=False,
    )
    output = result.stdout.strip()
    return int(output) if output else None


def _check_translation_dates(docs_dir, pages):
    for page in sorted(pages[DEFAULT_LANGUAGE]):
        source = _last_commit_time(docs_dir / DEFAULT_LANGUAGE / page)
        for language in LANGUAGES:
            if language == DEFAULT_LANGUAGE or page not in pages[language]:
                continue
            translation = _last_commit_time(docs_dir / language / page)
            if source and translation and source > translation:
                log.warning("Page '%s/%s' was modified after its '%s' version", DEFAULT_LANGUAGE, page, language)


def on_pre_build(config, **kwargs):
    # the i18n plugin runs a nested build per extra language: check only once
    i18n = config.plugins.get('i18n')
    if i18n and i18n.current_language != i18n.default_language:
        return
    docs_dir = Path(config.docs_dir)
    pages = {language: _pages(docs_dir, language, config.exclude_docs) for language in LANGUAGES}
    for language in LANGUAGES:
        for other in LANGUAGES:
            if other == language:
                continue
            for page in sorted(pages[language] - pages[other]):
                log.warning("Page '%s/%s' has no '%s' version", language, page, other)
    if os.environ.get('TOCK_DOC_CHECK_TRANSLATION_DATES', '').lower() == 'true':
        _check_translation_dates(docs_dir, pages)
