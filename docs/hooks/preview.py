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
# Branch previews (see .github/workflows/doc-deploy.yml): when DOC_PREVIEW_BRANCH is set,
# pages are hidden from search engines and show a banner linking to the official documentation.
import html
import os
import re

from mkdocs.config.defaults import MkDocsConfig
from mkdocs.structure.pages import Page

OFFICIAL_DOC_URL = 'https://doc.tock.ai/tock/master/'


def on_post_page(output: str, *, page: Page, config: MkDocsConfig):
    branch = os.environ.get('DOC_PREVIEW_BRANCH')
    if not branch:
        return output

    meta = '<meta name="robots" content="noindex, nofollow">'
    banner = (
        '<div style="background:#b71c1c;color:#fff;padding:.4rem 1rem;text-align:center;font-size:.8rem">'
        f'Preview of the branch <code>{html.escape(branch)}</code>, not the official documentation: '
        f'<a style="color:#fff;text-decoration:underline" href="{OFFICIAL_DOC_URL}">{OFFICIAL_DOC_URL}</a>'
        '</div>'
    )
    output = output.replace('<head>', '<head>' + meta, 1)
    return re.sub(r'(<body[^>]*>)', lambda m: m.group(1) + banner, output, count=1)
