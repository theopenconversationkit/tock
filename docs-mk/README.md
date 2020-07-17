## How to build the documentation

### Tools

In order to modify Tock documentation please install the following : 

* Python
* Markdown documentation generator [MkDocs](http://www.mkdocs.org/)
* [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

### Documentation

#### Edit the source files 
Documentation modification starts with editing the docs-mk content witch contains a series of intertwined markdown documents.

Any modification done to these files will be visible before generation by launching a local server:

```sh
$ mkdocs serve 
```

With this command a local server will be accessible at the following address http://localhost:8000.

> If you want to modify the port you can always add the "-a \<IP:PORT\>" parameter.

There are two Mkdocs sites with their own folder structure, for FR and EN languages.
Bot docsites can run using `mkdocs serve` from the respective path, or the following shortcuts can be used from root:

```sh
$ etc/serve-docsite-fr.sh
$ etc/serve-docsite-en.sh
```

#### Generate the documentation site 

In order to generate the final documentation (and commit) you must launch at the level of the base directory the following:

```sh
$ etc/regen-docsite.sh
```

> Before submitting your work please make sure there are no missing links and no unexpected modifications in your generated files.
>
> Several files are modified every time (namely `search_index` and `sitemap`), and any modification to the page structure / 
> table of contents will result in all pages being modified.
>
> Yet in general, editing a single `md` source should not modify dozens of HTML files. 
