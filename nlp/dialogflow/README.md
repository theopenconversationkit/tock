## Usage

To use Dialogflow as NLP in a Tock bot :
- Add the module dependency to your pom.xml

        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>tock-nlp-dialogflow</artifactId>
            <version>${tock}</version>
        </dependency>
        
- Install your bot by adding a module with the following configuration :

```
    bind<NlpClient>(overrides = true) with singleton { TockDialogflowNlpClient() }
    bind<NlpController>(overrides = true) with singleton { DialogflowNlp() }
```    
    
- Set the environment variable `dialogflow_project_id` with your Dialogflow project id

- Set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` with the file path of the JSON file that contains your service account key