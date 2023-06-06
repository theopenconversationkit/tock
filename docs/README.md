# MkDocs Jekyll Theme

## How to build the documentation

### Edit the source files
Documentation modification starts with editing the sources in [`docs/_en`](_en) or [`docs/_fr`](_fr) 
(respectively for English and French versions of the documentation), which contain a series of intertwined
[Markdown](https://en.wikipedia.org/wiki/Markdown) documents (`.md` files).

If you add new pages, please also add them to the navigation by updating the navigation
for the relevant language in [`docs/_data/tocs`](_data/tocs).

To create a global page like [`about`](pages/about.md), add it to the [`docs/pages`](pages) directory.
You can define urls using the `permalink` attribute in your pages.

#### Markdown additions
This documentation uses the [Kramdown](https://kramdown.gettalong.org/) variant of Markdown.
Additionally, some features are implemented through templates in this repository.

- You can make tabbed code samples with this syntax:
  ```md
  - test.java
      ```java
      public static void main(String[] args) {
          System.out.println("Hello, World!");
      }
      ```
  
  - test.kt
      ```kt
      fun main() = println("Hello, World!")
      ```
  {: .tabbed-code}
  ```
  You can additionally highlight specific lines of codes by adding `{: data-hl-lines="<lines>"}` between the list
  item start and the file name, where `<lines>` is the list of line numbers separated by spaces. 
  For example: `- {: data-hl-lines="2 3"} test.java` will highlight the second and third lines of the java code sample.

### Testing changes
#### On GitHub

You can configure your github fork to publish your modifications under your own account.
For this, follow [the instructions from the GitHub documentation](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site#choosing-a-publishing-source).
Then, go to `https://<your_github_account>.github.io/tock/en` to see the new website as it will appear when your changes get merged upstream.

#### Locally

To run the site locally, follow these steps:
1. Clone this repository
   - If you already installed Jekyll, skip to step 5
2. Install [Ruby](https://www.ruby-lang.org/en/downloads/) with its development headers
3. Install Bundler (requires admin rights):
   ```bash
   $ gem install bundler
   ```
4. Setup Jekyll (from the `docs` folder of this repository, requires admin rights):
   ```bash
   $ bundle install
   > Fetching gem metadata from https://rubygems.org/............
   > Fetching version metadata from https://rubygems.org/...
   > Fetching dependency metadata from https://rubygems.org/..
   > Resolving dependencies...
   ```
5. Launch a local server (from the `docs` directory of this repository).
   Depending on how you installed jekyll, you can run `jekyll serve` or `bundle exec jekyll serve`:

   ```bash
   bundle exec jekyll serve
   > Configuration file: /Users/octocat/tock/_config.yml
   >            Source: /Users/octocat/tock
   >       Destination: /Users/octocat/tock/_site
   > Incremental build: disabled. Enable with --incremental
   >      Generating...
   >                    done in 0.309 seconds.
   > Auto-regeneration: enabled for '/Users/octocat/tock'
   > Configuration file: /Users/octocat/tock/_config.yml
   >    Server address: http://127.0.0.1:4000/
   >  Server running... press ctrl-c to stop.
   ```
6. Don't forget to comment the window redirection in `docs/index.html` to avoid default redirection on `https://doc.tock.ai/tock`
7. Preview your copy of the site in your web browser at [http://localhost:4000](http://localhost:4000).

> If you want to modify the port you can always add the `-P <PORT>` parameter (see also [serve command options](https://jekyllrb.com/docs/configuration/options/#serve-command-options)).

> Before submitting your work please make sure there are no missing links and no unexpected modifications in your generated files.

## How to configure the documentation's templates

To edit configuration values, customize the [_config.yml](_config.yml) file.
Most of the configuration values are self-explanatory,
and for more details, see the [*Mkdocs Jekyll*'s about page](https://vsoch.github.io/mkdocs-jekyll/about/)
rendered on the site.

## Updating the template
The template files for this documentation website are based on these
previous arts:

- [squidfunk/mkdocs-material](https://github.com/squidfunk/mkdocs-material)
- [vsoch/mkdocs-jekyll](https://vsoch.github.com/mkdocs-jekyll/)
- [alexcarpenter/material-jekyll-theme](http://alexcarpenter.github.io/material-jekyll-theme)

To integrate the latest Mkdocs-Material updates in this repository, follow these steps:

1. Clone or download the latest version of the mkdocs-material repository ([`https://github.com/squidfunk/mkdocs-material.git`](https://github.com/squidfunk/mkdocs-material.git))
2. Overwrite this repository's `docs/assets/mkdocs-material/` directory with the content of the `material/assets/` directory from the downloaded `mkdocs-material` repository
3. Rename the following files in this repository's `docs/assets/mkdocs-material/` directory:
    - in `javascripts/`, `bundle.<hash>.min.js` (eg. `bundle.405ee05f.min.js`) to `bundle.min.js`. The `.map` file should not be renamed.
    - in `javascripts/workers/`, `search.<hash>.min.js` (eg. `search.d2d1b361.min.js`) to `search.min.js`. The `.map` file should not be renamed.
    - in `stylesheets/`:
        - `colors.<hash>.min.css` to `colors.min.css`
        - `main.<hash>.min.css` to `main.min.css`
        - `palette.<hash>.min.css` to `palette.min.css`
4. Update the content of this repository's `_docs/_data/languages/en.json` and `_docs/data/languages/fr.json`
   based on the content of `src/partials/languages/en.html` and `src/partials/languages/fr.html` from the mkdocs-material repository
5. Update the HTML template files (this process is currently entirely manual):
    - Update `docs/_layouts/default.html` in this repository based on the content of `src/base.html` from the mkdocs-material repository
    - `_includes/<filename>.html` in this repository based on the content of `src/partials/<filename>.html` from the mkdocs-material repository
      
Tips for updating HTML template files:
- `{% block %}` tags in mkdocs-material can be safely ignored
- `{% import "partials/language.html" as lang %}` tags should be replaced with `{% assign lang = site.data.languages[page.language] %}`
- `{{ lang.t("<key>") }}` and equivalent tags should be replaced with `{{ lang["<key>"] }}`
- `{% include "partials/<filename>.html" %}` tags should be replaced with `{% include <filename.html> %}`
