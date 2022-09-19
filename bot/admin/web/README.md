# Bot Admin frontend

## Local run instructions

Since Tock can be used as a Bot platform or a NLP-only platform,
the _Bot Admin_ Web frontent extends the _NLP Admin_ Web frontend.

To build/run the frontend, one can:

- merge NLP sources (from NLP assembly) to a temporary folder
  (namely `target/frontend`),
- or set symbolic links to NLP sources.

> For CI/release builds, Maven merges sources to a temporary folder.

### Merge sources to target folder

This is how Maven builds the frontend for deployment.

Use Maven to merge sources to the `target/frontend` temporary folder:

```
mvn generate-resources
cd target/frontend
```

Then run NPM to setup and launch [Angular CLI](https://cli.angular.io/):

```
npm install
npm install -g @angular/cli
ng serve
```

> Don't forget to start the
> [_Bot Admin_ server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml).

### Symlinks alternative

One can alternatively add these symbolic links to merge sources in `src` folder:

> For unix users, in the current directory (bot/admin/web):

```
ln -s "$(pwd)/../../../nlp/admin/web/src/app/applications" src/app/applications
ln -s "$(pwd)/../../../nlp/admin/web/src/app/archive" src/app/archive
ln -s "$(pwd)/../../../nlp/admin/web/src/app/build" src/app/build
ln -s "$(pwd)/../../../nlp/admin/web/src/app/core-nlp" src/app/core-nlp
ln -s "$(pwd)/../../../nlp/admin/web/src/app/entities" src/app/entities
ln -s "$(pwd)/../../../nlp/admin/web/src/app/inbox" src/app/inbox
ln -s "$(pwd)/../../../nlp/admin/web/src/app/intents" src/app/intents
ln -s "$(pwd)/../../../nlp/admin/web/src/app/logs" src/app/logs
ln -s "$(pwd)/../../../nlp/admin/web/src/app/model" src/app/model
ln -s "$(pwd)/../../../nlp/admin/web/src/app/nlp-tabs" src/app/nlp-tabs
ln -s "$(pwd)/../../../nlp/admin/web/src/app/quality-nlp" src/app/quality-nlp
ln -s "$(pwd)/../../../nlp/admin/web/src/app/scroll" src/app/scroll
ln -s "$(pwd)/../../../nlp/admin/web/src/app/search" src/app/search
ln -s "$(pwd)/../../../nlp/admin/web/src/app/sentence-analysis" src/app/sentence-analysis
ln -s "$(pwd)/../../../nlp/admin/web/src/app/sentences-scroll" src/app/sentences-scroll
ln -s "$(pwd)/../../../nlp/admin/web/src/app/shared-nlp" src/app/shared-nlp
ln -s "$(pwd)/../../../nlp/admin/web/src/app/test-nlp" src/app/test-nlp
ln -s "$(pwd)/../../../nlp/admin/web/src/app/try" src/app/try
```

> For Windows users, in the current directory (bot/admin/web):

Use the CMD and not PowerShell to execute these commands

```
mklink /D "src/app/applications" "%cd%/../../../nlp/admin/web/src/app/applications/"
mklink /D "src/app/archive" "%cd%/../../../nlp/admin/web/src/app/archive/"
mklink /D "src/app/build" "%cd%/../../../nlp/admin/web/src/app/build/"
mklink /D "src/app/core-nlp" "%cd%/../../../nlp/admin/web/src/app/core-nlp/"
mklink /D "src/app/entities" "%cd%/../../../nlp/admin/web/src/app/entities/"
mklink /D "src/app/inbox" "%cd%/../../../nlp/admin/web/src/app/inbox/"
mklink /D "src/app/intents" "%cd%/../../../nlp/admin/web/src/app/intents/"
mklink /D "src/app/logs" "%cd%/../../../nlp/admin/web/src/app/logs/"
mklink /D "src/app/model" "%cd%/../../../nlp/admin/web/src/app/model/"
mklink /D "src/app/nlp-tabs" "%cd%/../../../nlp/admin/web/src/app/nlp-tabs/"
mklink /D "src/app/quality-nlp" "%cd%/../../../nlp/admin/web/src/app/quality-nlp/"
mklink /D "src/app/scroll" "%cd%/../../../nlp/admin/web/src/app/scroll/"
mklink /D "src/app/search" "%cd%/../../../nlp/admin/web/src/app/search/"
mklink /D "src/app/sentence-analysis" "%cd%/../../../nlp/admin/web/src/app/sentence-analysis/"
mklink /D "src/app/sentences-scroll" "%cd%/../../../nlp/admin/web/src/app/sentences-scroll/"
mklink /D "src/app/shared-nlp" "%cd%/../../../nlp/admin/web/src/app/shared-nlp/"
mklink /D "src/app/test-nlp" "%cd%/../../../nlp/admin/web/src/app/test-nlp/"
mklink /D "src/app/try" "%cd%/../../../nlp/admin/web/src/app/try/"
```

Then run NPM to setup and launch [Angular CLI](https://cli.angular.io/):

```
npm install
npm install -g @angular/cli
ng serve
```

> Don't forget to start the
> [_Bot Admin_ server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml).

## Package instructions

To build/package the frontend:

```
mvn install
```

Maven runs NPM / Angular scripts automatically from merged sources folder. They produce the `web`
assembly, ie. the deployable _Bot Admin_ frontend application.

## Troubleshooting

### Windows setup

Windows users may get NPM errors because of missing Python 2 and/or .NET Framework SDK / Visual C++ Build Tools.
Although these build tools can be installed separately, some users prefer the following one-liner:

> To run as Administrator:

```
// install Python 2 and Visual C++ Build Tools
npm install --global windows-build-tools
```

> Note that the installation may take time...

Please visit [the `windows-build-tools` project](https://github.com/felixrieseberg/windows-build-tools) for details and available options.

### Python 2.7 not in Path

Build takes Python from `Path`, and requires [Python 2.7](https://www.python.org/downloads/release/python-272/).
If you have different/multiple Python versions, or Python is not in your `Path`, tell NPM like this:

> The following examples mention `python.exe` for Windows users

```
npm install --python=D:/devhome/opt/Python27/python.exe
npm install --python=D:/devhome/opt/Python27/python.exe -g @angular/cli
ng serve
```

Alternative:

```
npm config set python D:/devhome/opt/Python27/python.exe
```
