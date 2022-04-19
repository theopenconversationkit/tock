This entity type provider uses an API that find entities. 

This API could be provided by engine of your choice like flairNLP, spacy...

Just add `tock-nlp-entity-rest`dependency in your classpath and
set the `tock_nlp_entity_type_url` property targeting the external engine url.

# Example of dependency adding in classpath :
As an exemple we use `nlp-api-client`, it can be added somewhere else.
- In intelliJ `Project structure`>`Modules`>`nlp-api-client` and add the module `tock-nlp-entity-rest`
- Or you can add in `nlp-api-client` pom.xml
```
  <dependency>
    <groupId>ai.tock</groupId>
    <artifactId>tock-nlp-entity-rest</artifactId>
    <version>${version}</version>
  </dependency>
```
Don't forget the restart your local BotAdmin. 

## FYI
The module use the SPI dependencies available in java. It means it will load with the service loading and the class in ai.tock.nlp.core.service.entity.EntityTypeProvider implementation in META-INF wich can be loaded at the start.

## Sample server with flairNlp
A flairNLP server sample is available: https://github.com/theopenconversationkit/tock-flair
Be careful the `tock_nlp_entity_type_url` is `http://localhost:5000/api/v1` don't forget to set it properly.