# Bot Admin frontend

## Local run instructions

```
cd bot/admin/web
npm install
npm install -g @angular/cli
ng serve
```

> Don't forget to start the
> [_Bot Admin_ server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml).

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
