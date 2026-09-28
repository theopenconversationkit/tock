# Tock documentation 

## How to generate website

##### Using pyenv (recommended)

It's recommended to use pyenv, see install and usage guide here:
https://gist.github.com/trongnghia203/9cc8157acb1a9faad2de95c3175aa875

Basic usage to create a venv with a specific version of Python for this project :

```sh
# In docs folder
pyenv install 3.13.9
pyenv local 3.13.9
which python # Check that you use the python version installed by pyenv
python --version # Check your python version
python -m venv .venv # Create a virtual env based on this python version
source .venv/bin/activate # Activate your virtual env
pip install -r  requirements.txt # install dependencies
mkdocs serve # Generate on default port 
mkdocs serve --dev-addr 127.0.0.1:8182 # To generate the website on the port you want 
```

Before opening a PR, check that the strict build passes (this is what the CI runs):

```sh
mkdocs build --strict
```

## Structure

- `docs/en/` is the source of truth; `docs/fr/` mirrors it with the same file names.
  A page missing in `fr/` falls back to the English version.
- The menu is defined explicitly by the `nav:` section of `mkdocs.yml`: a new page must be added there.
  Section titles are translated with `nav_translations` (in the `fr` language of the `i18n` plugin);
  page titles come from each page's `title:` front matter.
- Images go in `docs/img/`.
- `hooks/copyapiswagger.py` copies the Swagger / OpenAPI files of the web connector and the NLP API
  into `docs/api/` (git-ignored) before each build.
- `hooks/chatbot_widget.py` appends the Tock chatbot widget (`includes/chatbot.md`) to every page.
- `hooks/variables.py` replaces `{{ name }}` placeholders with the values of `extra.variables` in `mkdocs.yml`,
  e.g. `{{ tock_version }}` in dependency snippets: after a release, only `mkdocs.yml` needs to be updated.
- `hooks/redirects.py` generates a redirect page, in each language, for every old path listed in `redirects.yml`.
  When you move or rename a page, add its old path there so that external links keep working.
- `hooks/i18n_parity.py` warns (and fails the strict build) when a page exists in `en/` but not in `fr/`, or the reverse.
- Screenshots go in `docs/img/studio/`, `docs/img/gen-ai/` or `docs/img/channels/`, in PNG, taken with the light theme.
  Retake them when a _Tock Studio_ screen changes significantly, and give each image a descriptive alt text.
- `etc/list-doc-properties.py --check docs/docs/en/operate/configuration.md` lists the configuration properties
  missing from the configuration reference.
- The CI lints the pages with [markdownlint](https://github.com/DavidAnson/markdownlint) (rules in `.markdownlint-cli2.yaml`):
  `npx markdownlint-cli2 --config docs/.markdownlint-cli2.yaml "docs/docs/**/*.md"` from the repository root.
- `hooks/i18n_parity.py` also checks, with `TOCK_DOC_CHECK_TRANSLATION_DATES=true mkdocs build` (without `--strict`),
  that no English page was modified after its French version.
- `.github/workflows/doc-links.yml` checks the external links every week with [lychee](https://lychee.cli.rs)
  (configuration: `lychee.toml`) and opens an issue when some are broken.

## Actions / Workflows

- `.github/workflows/validate-build.yml` (job `doc`) runs `mkdocs build --strict` on every PR touching `docs/`.
- `.github/workflows/doc-deploy.yml` builds the site and deploys it to GitHub Pages on every push to `master`
  touching `docs/`. The site is published under `/master/`.

## Contributing and releases

The contribution rules for the documentation (English first then French, navigation, redirects, strict build)
and the documentation steps of the release checklist are in [`CONTRIBUTING.md`](../CONTRIBUTING.md).

## Dependencies

`requirements.txt` pins every Python dependency. To upgrade them, install the top-level packages
(`mkdocs`, `mkdocs-material`, `mkdocs-static-i18n`, `mkdocs-git-revision-date-localized-plugin`) in a fresh venv,
check that `mkdocs build --strict` passes, then regenerate the file with `pip freeze`.

Stay on MkDocs 1.x: MkDocs 2.0 is not compatible with the plugins and theme used by this site
(see the [Material for MkDocs analysis](https://squidfunk.github.io/mkdocs-material/blog/2026/02/18/mkdocs-2.0/)).
Material prints a warning about it during the build; set `NO_MKDOCS_2_WARNING=true` to hide it.

The chatbot widget (`includes/chatbot.md`) loads [tock-vue-kit](https://github.com/theopenconversationkit/tock-vue-kit)
from unpkg, with a fixed version. Since version 2.0, tock-vue-kit no longer bundles its icons: the page also loads
`bootstrap-icons`.

