# Contributing

## Git (commits & merge requests)

To submit a feature or bugfix:

1. [Create an _issue_](https://github.com/theopenconversationkit/tock/issues/new):
    - Reccommended format for the title:
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
3. To be merged, a _pull request_ must pass the tests and be reviewed by at least two of these developers:
    - [@vsct-jburet](https://github.com/vsct-jburet),
    [@francoisno](https://github.com/francoisno),
    [@NainJaune](https://github.com/NainJaune),
    [@elebescond](https://github.com/elebescond),
    [@SarukaUsagi](https://github.com/SarukaUsagi),
    [@MaximeLeFrancois](https://github.com/MaximeLeFrancois),
    [@bakic](https://github.com/bakic),
    [@broxmik](https://github.com/broxmik),
    [@mrboizo](https://github.com/mrboizo)
        
## Code conventions

[Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) are used

## Unit tests

Every new feature or fix should embed its unit test(s).

## More...

More about [sources and contrib](https://doc.tock.ai/tock/master/about/contribute.html).

Feel free to [contact us](https://doc.tock.ai/tock/master/about/contact.html).