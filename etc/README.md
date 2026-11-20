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

## How to release from GitHub Actions

The [Release workflow](../.github/workflows/release.yml) releases, in this order:

1. tock (Maven Central), then commits the versions displayed in the documentation;
2. [tock-corenlp](https://github.com/theopenconversationkit/tock-corenlp),
   [tock-bot-open-data](https://github.com/theopenconversationkit/tock-bot-open-data) and
   [tock-bot-demo](https://github.com/theopenconversationkit/tock-bot-demo), in parallel (Maven Central);
3. [tock-docker](https://github.com/theopenconversationkit/tock-docker): tag, then the images on Docker Hub,
   in parallel (tags `<version>`, `<major>` and `latest`), then `TAG` of its `.env`.

```sh
# dry run (default, and on each pull request that changes the release): commits and tags stay on the runners,
# nothing is published
gh workflow run release.yml --ref master
# release, once the dry run succeeded; version and next-version are optional
gh workflow run release.yml --ref master -f dry-run=false -f version=26.9.2 -f next-version=26.9.3-SNAPSHOT
```

- The release requires a successful [E2E](../.github/workflows/e2e.yml) run on the released commit (`skip-e2e` bypasses it),
  and the approval of a reviewer of the `release-approval` environment.
- The tests are not run again: the release builds each project once.
- Each job hands its artifacts over to the next ones, so that they do not wait for Maven Central.
- After a failure, fix the cause, then use **Re-run failed jobs**: the jobs that succeeded (and published) are not run
  again. The steps run by each job are in [release/release.sh](release/release.sh), and can be run locally.

Prerequisites:

- A GitHub App with the *Contents: read & write* permission, installed on the five repositories, and allowed to bypass
  the protection rules of their `master` branch and of their tags.
- An environment `release-approval`, with required reviewers.
- An environment `release`, restricted to `master`, with the secrets `RELEASE_APP_ID`, `RELEASE_APP_PRIVATE_KEY`,
  `CENTRAL_USERNAME`, `CENTRAL_TOKEN` (Maven Central user token), `GPG_PRIVATE_KEY` (armored), `GPG_PASSPHRASE`,
  `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`.

## How to release and deploy a release locally

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
