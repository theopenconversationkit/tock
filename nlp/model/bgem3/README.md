Integration of Bge-m3 model : https://huggingface.co/BAAI/bge-m3 (multilingual, more than 100 languages)

Bgem3EngineProvider is the entry point of the module, loaded using the service loader mecanism defined in the file resources/META-INF/services/ai.tock.nlp.model.service.engine.NlpEngineProvider

2 sagemaker endpoints on aws are used in order to call : 
- intent model :   (see Bgem3IntentClassifier)
- entities model : (see Bgem3EntityClassifier)

Intent and entities models are independent, unlike the older models

To see sagemaker endpoints : 
- connect to aws
- choose the good aws profile
- open sagemaker studio
- open deployments/Endpoints. Normally, the 2 endpoints appear

Check the status of the endpoints (normally in service). If not, contact the data scientists
Then, you will be able to test the endpoints locally (see Bgem3AwsClientIntegrationTest)