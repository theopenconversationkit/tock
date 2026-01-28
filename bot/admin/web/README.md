# Bot Admin Frontend

## Local run instructions

```
cd bot/admin/web
npm install
npm run start
```

> `npm install` will automatically install the git pre-commit hook (see [Git hooks](#git-hooks) below).

> Don't forget to start the
> [_Bot Admin_ server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml).

## Git hooks

A `pre-commit` hook is included in this repository. It automatically sanitizes `package-lock.json` before each commit by replacing any private registry URLs with the standard public registry (`https://registry.npmjs.org`).

This is useful when working behind a corporate npm mirror: the mirror URLs never end up committed to the repository.

### Installation

The hook is installed automatically when you run `npm install`. No manual step required.

If for any reason you need to install it manually:

```
node scripts/hooks/install-hooks.js
```

### Dry-run

To preview what the hook would replace without modifying any file:

```
DRY_RUN=1 bash .git/hooks/pre-commit
```

## Troubleshooting

### Windows setup

Windows users may get NPM errors because of missing .NET Framework SDK / Visual C++ Build Tools.
These can be installed via the [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) installer — select the **"Desktop development with C++"** workload.
