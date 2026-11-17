# Deploy in maven repository

## Local configuration

Copy/paste the contents of [etc/deploy-settings.xml](https://github.com/theopenconversationkit/tock/blob/master/etc/deploy-settings.xml)
in your .m2/settings.xml - set the login/password of your sonatype account
and the keyname and passphrase of your gpg key

## How to deploy a snapshot

In the root directory of the project, run

```sh
etc/deploySnapshot.sh
```

## How to release and deploy a release

In the root directory of the project, run

```sh
etc/releaseAndDeploy.sh
```

The script first checks, with the [GitHub CLI](https://cli.github.com/), that the
[E2E workflow](../.github/workflows/e2e.yml) succeeded on the commit to release.
This workflow runs every night on master; to run it on the current commit:

```sh
gh workflow run e2e.yml --ref master
gh run watch
```

`SKIP_E2E=1 etc/releaseAndDeploy.sh` bypasses the check.
The scenario itself lives in [tock-docker](https://github.com/theopenconversationkit/tock-docker/tree/master/e2e).

The documentation steps of the release (changelog, versions displayed in the documentation)
are described in [CONTRIBUTING.md](../CONTRIBUTING.md).
