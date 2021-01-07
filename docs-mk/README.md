## How to build the documentation

### Tools

In order to modify Tock documentation please install the following: 

* Python
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
