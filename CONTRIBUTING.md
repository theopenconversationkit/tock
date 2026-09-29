# Contributing

## Git (commits & merge requests)

To submit a feature or bugfix:

1. [Create an _issue_](https://github.com/theopenconversationkit/tock/issues/new):
    - Recommended format for the title:
        - `[Component] Title` where component might be 
    _Studio_, _Core_, _Doc_, etc. and title usually is like _Do or fix something_
2. [Create a _pull request_](https://github.com/theopenconversationkit/tock/pulls) and link it to the issue(s):
    - All commits should be [_signed_](https://help.github.com/en/github/authenticating-to-github/managing-commit-signature-verification) 
    - Please rebase and squash unnecessary commits (tips: PR can be tagged as _Draft_) before submitting
    - Recommended format for the branch name :
        - `ISSUEID_short_title`
    - Recommended format for the commit(s) message(s):
        - `resolves #ISSUEID Component: title` for features
        - `fixes #ISSUEID Component: title` for fixes
3. To be merged, a _pull request_ must pass the tests and be reviewed by at least two approvers
        
## Code conventions

[Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) are used for kotlin code.

## Unit tests

Every new feature or fix should embed its unit test(s).

## Documentation

The documentation is built with [MkDocs](https://www.mkdocs.org/) from the [`docs`](docs) folder
(see [`docs/README.md`](docs/README.md) to build it locally).

- Write in English first (`docs/docs/en/`), then add the French version (`docs/docs/fr/`), with the same file name.
- Add new pages to the `nav:` section of [`docs/mkdocs.yml`](docs/mkdocs.yml).
- When moving or renaming a page, add its old path to [`docs/redirects.yml`](docs/redirects.yml).
- Check that `mkdocs build --strict` passes before submitting the pull request (the CI runs it).
- A feature or a removal (connector, setting, _Tock Studio_ screen) should come with its documentation.
- To share the documentation before the merge, push a `docs/*` branch to this repository
  (or run the _Build and Deploy MkDocs_ workflow on any branch): it is published to
  `https://doc.tock.ai/tock/preview/<branch>/` (`/` replaced by `-`, e.g. `docs/my-page` → `preview/docs-my-page`),
  and removed when the branch is deleted.

## Release

After each release:

1. Update the versions displayed in the documentation:
   ```sh
   etc/update-doc-version.sh            # uses the latest tock-* git tag
   etc/update-doc-version.sh 26.3.5     # or an explicit version
   ```
2. Add an entry for the release to the changelog, in both languages:
   [`docs/docs/en/project/changelog.md`](docs/docs/en/project/changelog.md) and
   [`docs/docs/fr/project/changelog.md`](docs/docs/fr/project/changelog.md)
   (main changes, link to the GitHub release notes).
3. If the release requires an action from the users (breaking change, prompt or configuration to update),
   add a note to the upgrade page, in both languages:
   [`docs/docs/en/operate/upgrade.md`](docs/docs/en/operate/upgrade.md) and
   [`docs/docs/fr/operate/upgrade.md`](docs/docs/fr/operate/upgrade.md).
4. If configuration properties were added, update the configuration reference
   ([`docs/docs/en/operate/configuration.md`](docs/docs/en/operate/configuration.md) and its French version):
   `etc/list-doc-properties.py --check docs/docs/en/operate/configuration.md` lists the missing properties.

## More...

More about [sources and contrib](https://doc.tock.ai/tock/master/project/contribute.html).

Feel free to [contact us](https://doc.tock.ai/tock/master/project/community.html#contact-us).
