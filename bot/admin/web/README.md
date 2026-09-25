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

## Environment banner

The Studio can display a banner indicating the current environment (e.g. `REC`,
`STAGING`), so users can tell at a glance which instance they are working on. It
is driven by a single runtime file, `src/assets/env-banner.json`, read once at
application startup.

A **neutral** version of this file is committed to the repository:

```json
{ "label": null, "_comment": "Set label to display an environment banner, e.g. 'REC'. Optional: color, textColor." }
```

With `label` unset, no banner is shown. This is the intended state for
production and for any instance that should look "clean". Because the file is
present and valid, no `404` request is emitted at startup.

### Displaying a banner

To show a banner on a given environment, serve an `env-banner.json` with a
non-empty `label`:

| Field       | Required | Description                                             |
| ----------- | -------- | ------------------------------------------------------- |
| `label`     | yes      | Text shown in the banner. `null` or absent = no banner. |
| `color`     | no       | Background color (any CSS color). Defaults to amber.    |
| `textColor` | no       | Text color. Auto-derived from `color` when omitted.     |

Example:

```json
{ "label": "REC", "color": "#e2504f" }
```

A permanent 4px colored strip is also shown along the top of the window whenever
a label is set. Unlike the diagonal banner, the strip cannot be dismissed — it
guarantees the environment stays identifiable even after a user hides the
banner for the current tab.

### Configuring it per environment

The Studio ships as a single, environment-agnostic build: the same artifact runs
everywhere, and the banner is resolved **at runtime, not at build time**. The
environment-specific file must therefore be injected **at deployment**, by
overwriting `assets/env-banner.json` in the served files. The build itself is
never specialized per environment.

How you inject it depends on your deployment. Two common patterns:

**Mounted file (Kubernetes / Docker volume)** — keep one image for all
environments and mount a per-environment file over the asset. This preserves
image promotion (the exact same image moves from one environment to the next):

```yaml
# ConfigMap, one per environment
apiVersion: v1
kind: ConfigMap
metadata:
  name: studio-env-banner
data:
  env-banner.json: |
    { "label": "REC", "color": "#e2504f" }
---
# Mounted over the served asset in the Deployment
volumeMounts:
  - name: env-banner
    mountPath: <web-root>/assets/env-banner.json
    subPath: env-banner.json
```

**Container entrypoint** — write the file at startup from an environment
variable. Environments that set nothing keep the committed neutral file:

```sh
# docker-entrypoint.sh
if [ -n "$ENV_BANNER_LABEL" ]; then
  printf '{"label":"%s","color":"%s"}' \
    "$ENV_BANNER_LABEL" "${ENV_BANNER_COLOR:-#f2a900}" \
    > <web-root>/assets/env-banner.json
fi
```

In both cases, production simply injects nothing and keeps the neutral file.

### Previewing locally

Temporarily edit `src/assets/env-banner.json` to give it a label, or force one
via the query string without touching the file:

```
http://localhost:4200/?env-banner=REC
```

The banner can be dismissed for the current tab using its close button; it
reappears on the next session (new tab or window).

## Troubleshooting

### Windows setup

Windows users may get NPM errors because of missing .NET Framework SDK / Visual C++ Build Tools.
These can be installed via the [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) installer — select the **"Desktop development with C++"** workload.
