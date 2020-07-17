## How to build the documentation

### Tools
In order to modify Tock documentation please install the following : 
* Markdown documentation generator [MkDocs](http://www.mkdocs.org/)
* [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

### Documentation

#### Edit the source files 
Documentation modification starts with editing the docs-mk content witch contains a series of intertwined markdown documents.

Any modification done to these files will be visible before generation by launching a local server:

```sh
mkdocs serve 
```

With this command a local server will be accessible at the following address http://localhost:8000.

> If you want to modify the port you can always add the "-a \<IP:PORT\>" parameter.

#### Generate the documentation site 

In order to generate the final documentation you must launch at the level of the base directory the following:
```sh
etc/regen-docsite.sh
```
> Before submitting your work please make sure there are no missing links and no unexpected modifications in your generated files. 
