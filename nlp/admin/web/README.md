# NLP Admin frontend

## Local run instructions

To build/run the frontend, run NPM and launch [Angular CLI](https://cli.angular.io/):

```
npm install
npm install -g @angular/cli
ng serve
```

> Don't forget to start the
> [_NLP Admin_ server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml).

## Package instructions

To build/package the frontend:

```
mvn install
```

Maven runs NPM / Angular scripts automatically from merged sources folder.
The following assemblies are built and attached:

- the `web` assembly is the deployable _NLP Admin_ frontend application
- the `sources` assembly export sourcefiles, so that _Bot Admin_ can merge and extend them

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
