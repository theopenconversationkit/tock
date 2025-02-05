# Documentation of the Multilingual Synchronization Feature

This documentation provides detailed information about our multilingual synchronization feature now accessible via the settings of your application. It is presented as follows:

![Synchronization](../../img/synchronization.png)

This feature requires the user to specify a source and a target, each associated with a namespace and an application, to synchronize data between different bots. It facilitates the development, testing, and maintenance of your chatbots, whether in monolingual or multilingual mode.

## Activation

Please ask your administrator to switch the `tock_namespace_open_access`property to `true` in tock studio to enable the feature.

## Key Features:

The synchronization function offers several essential features:

- **Copying of Stories and Intentions:** You can synchronize "stories" and their intentions between a Source bot and a Target bot, whether in a development or production environment. This facilitates testing and improvements without directly affecting the production version.

  _**WARNING:** It is important to note that during synchronization, the "stories" of the target bot that are not found in the source bot will be deleted, and the rest will be overwritten by the "stories" from the source bot._

- **Overwriting of Training:** The training of the Source bot overwrites that of the Target bot.

  _**INFO:** Only pre-existing information in the Source bot is affected, thus preserving the specific information of the Target bot that is not derived from the Source bot._

- **Copying of Untrained Sentences (Copy Inbox Messages):** when this feature is activated, untrained sentences are also retrieved. The typical use case for this feature is to copy a production bot with untrained sentences to a pre-prod bot to train these sentences on newly created intentions.

## Benefits of the Feature with Usage Examples:

Synchronization offers significant advantages:

- **Efficient Improvement:** Synchronization allows for improving a production chatbot without disrupting the current version. For example, if you have a customer service chatbot in production (Target bot) and you want to add new features developed in the development environment (Source bot).

  _**Usage Example:** You can integrate new "stories," intentions, and training data from the original bot into the destination bot without disrupting current customer service. This allows expanding the capabilities of the production chatbot while maintaining service continuity._

- **Enrichment of Training:** Copying the training from the original bot into the destination bot allows for enriching the capabilities of the destination bot.

  _**Usage Example:** If the destination bot had learned that "hello" is linked to the "greetings" intention, but the original bot was trained with "hello" associated with the "test" intention, synchronization will update the destination bot to include this new understanding, while preserving the previous training. This improves the accuracy and understanding of the destination bot without losing pre-existing knowledge._

## Conclusion:

The multilingual synchronization function improves the management of chatbots by allowing efficient synchronization between different bots, whether in development or in production. It facilitates improvement, adding new "stories" and intentions, and enriching the training of the destination bot, while preserving its previous entity. Although limitations remain, the team continues to work to improve this feature and offer a versatile solution for chatbot management.
