# Getting Started with the Conversational Framework

## The Open Data Bot

A good starting point is the source code of the [Open Data Bot](https://github.com/theopenconversationkit/tock-bot-open-data) 

Follow the instructions of the README file of the project, to start the bot in the IDE (do not configure Messenger or Google Assistant at this point),
then connect to the administration interface. The bot is already testable.

## The Test Tab

Go to this tab, and test the bot:

![bot test](img/test.png "bot test")

This is a test mode, so the interface is minimal.

The real goal is to have your users interact with the bot via channels like Messenger, Google Assistant ...
or your sites or applications.

## The Monitoring Tab

It is then possible to consult the discussion that you just had with the bot via the Monitoring tab:

![Dialog monitoring](img/monitoring.png "Dialog monitoring")

In this sample, this dialog has the Messenger flag, as it was tested for this channel.

## The Build Tab

### Add a new answer

With the category **Add new Answer**, it is possible to add directly a new answer:
 
![Add a new answer](img/build-1.png "Add a new answer")

Then test the new intention and its answer:

![Test the new answer](img/build-2.png "Test the new answer")

### Modify the Answers and Internationalization

Finally it is possible to modify each answer of the bot, by type of interface (chat / voice), by type of connector and by language
with the ** i18n ** tab.

The ability to add alternative answers (a response from the list will be chosen each time at random) is also provided (with the "plus" button).

![i18n](img/i18n.png "i18n")