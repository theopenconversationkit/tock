# MkDocs Jekyll Theme

## How to build the documentation

### Tools

In order to modify Tock documentation you will need Python 3 on somes dependencies.

Optionnaly, create a clean python virtual env :
```sh
$ python3 -m venv .venv
$ source .venv/bin/activate
```

Install dependencies :
```sh
$ pip install -r requirements.txt
```

#### Dependencies references

These dependencies are needed and referenced in `requirements.txt` :
* Markdown documentation generator [MkDocs](http://www.mkdocs.org/)
* [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
* [Macros for MkDocs](https://squidfunk.github.io/mkdocs-material/reference/variables/)
* [Git Revision Date Localized for Mkdocs](https://github.com/timvink/mkdocs-git-revision-date-localized-plugin)

### Documentation

#### Edit the source files
Documentation modification starts with editing the sources in `docs-mk`, which contains a series of intertwined
[Markdown](https://fr.wikipedia.org/wiki/Markdown) documents (`.md` files).

Any modification done to these files will be visible before generation by launching a local server from
one of the Markdown root folders:

```sh
$ mkdocs serve 
```

With this command a local server will be accessible at the following address http://localhost:8000.

> If you want to modify the port you can always add the `-a \<IP:PORT\>` parameter.

There are four Mkdocs folders and docsites with their own folder structure:

* Both _FR_ and _EN_ languages
* Both _light_ and _dark_ modes

Instead of moving from a Markdown folder to another for each respective docsite,
the following shortcuts can be used directly from base directory (Git root):

```sh
$ ./etc/serve-docsite-fr.sh
$ ./etc/serve-docsite-fr-dark.sh
$ ./etc/serve-docsite-en.sh
$ ./etc/serve-docsite-en-dark.sh
```

#### Generate the documentation site

In order to generate the final documentation (and commit) you must launch at the level of the base directory (Git root)
the following command:

```sh
$ ./etc/regen-docsite.sh
```

> Before submitting your work please make sure there are no missing links and no unexpected modifications in your generated files.
>
> Several files are modified every time (namely `search_index` and `sitemap`), and any modification to the page structure /
> table of contents will result in all pages being modified.
>
> Yet in general, editing a single `md` source should not modify dozens of HTML files.

[![CircleCI](https://circleci.com/gh/vsoch/mkdocs-jekyll/tree/master.svg?style=svg)](https://circleci.com/gh/vsoch/mkdocs-jekyll/tree/master)

![https://raw.githubusercontent.com/vsoch/mkdocs-jekyll/master/assets/img/mkdocs-jekyll.png](https://raw.githubusercontent.com/vsoch/mkdocs-jekyll/master/assets/img/mkdocs-jekyll.png)

This is a [starter template](https://vsoch.github.com/mkdocs-jekyll/) for a mkdocs jekyll theme, based on these two
previous arts:

 - [alexcarpenter/material-jekyll-theme](http://alexcarpenter.github.io/material-jekyll-theme)
 - [squidfunk/mkdocs-material](https://github.com/squidfunk/mkdocs-material)

## Usage

### 1. Get the code

You can clone the repository right to where you want to host the docs:

```bash
git clone https://github.com/vsoch/mkdocs-jekyll.git docs
cd docs
```

### 2. Customize

To edit configuration values, customize the [_config.yml](https://github.com/vsoch/mkdocs-jekyll/blob/master/_config.yml).
To add pages, write them into the [pages](https://github.com/vsoch/mkdocs-jekyll/tree/master/pages) folder. 
You define urls based on the `permalink` attribute in your pages,
and then add them to the navigation by adding to the content of [_data/toc.myl](https://github.com/vsoch/mkdocs-jekyll/blob/master/_data/toc.yml).

### 3. Options

Most of the configuration values in the [_config.yml](https://github.com/vsoch/mkdocs-jekyll/blob/master/_config.yml) are self explanatory,
and for more details, see the [about page](https://vsoch.github.io/mkdocs-jekyll/about/)
rendered on the site.

### 4. Serve

Depending on how you installed jekyll:

```bash
jekyll serve
# or
bundle exec jekyll serve
```

## Updating the template
1. Clone or download the latest version of the mkdocs-material repository ([`https://github.com/squidfunk/mkdocs-material.git`](https://github.com/squidfunk/mkdocs-material.git))
2. Overwrite this repository's `docs/assets/mkdocs-material/` directory with the content of the `material/assets/` directory from the downloaded `mkdocs-material` repository
3. Rename the following files in this repository's `docs/assets/mkdocs-material/` directory:
    - in `javascripts/`, `bundle.<hash>.min.js` (eg. `bundle.405ee05f.min.js`) to `bundle.min.js`. The `.map` file should not be renamed.
    - in `javascripts/workers/`, `search.<hash>.min.js` (eg. `search.d2d1b361.min.js`) to `search.min.js`. The `.map` file should not be renamed.
    - in `stylesheets/`:
        - `colors.<hash>.min.css` to `colors.min.css`
        - `main.<hash>.min.css` to `main.min.css`
        - `palette.<hash>.min.css` to `palette.min.css`
4. Update the HTML template files (this process is currently entirely manual):
    - Update `docs/_layouts/default.html` in this repository based on the content of `src/base.html` from the mkdocs-material repository
    - `_includes/<filename>.html` in this repository based on the content of `src/partials/<filename>.html` from the mkdocs-material repository
      
Tips for updating HTML template files:
- `{% block %}` tags in mkdocs-material can be safely ignored
- `{% import "partials/language.html" as lang %}` tags should be replaced with `{% assign lang = site.data.languages[page.language] %}`
- `{{ lang.t("<key>") }}` and equivalent tags should be replaced with `{{ lang["<key>"] }}`
- `{% include "partials/<filename>.html" %}` tags should be replaced with `{% include <filename.html> %}`

