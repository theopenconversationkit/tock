Integration of a sagemaker model, Bge-m3 model by default : https://huggingface.co/BAAI/bge-m3 (multilingual, support more than 100 languages)
In fact, it's possible to integrate any sagemaker model (adapt the endpoints and the service contract if you want to add an other sagemaker model)

SagemakerEngineProvider is the entry point of the module, loaded using the service loader mecanism defined in the file resources/META-INF/services/ai.tock.nlp.model.service.engine.NlpEngineProvider

2 sagemaker endpoints on aws are used in order to call : 
- intent model :   (see SagemakerIntentClassifier)
- entities model : (see SagemakerEntityClassifier)

Intent and entities models are independent, unlike the older models

To see sagemaker endpoints : 
- connect to aws
- choose the good aws profile
- open sagemaker studio
- open deployments/Endpoints. Normally, the 2 endpoints appear

Check the status of the endpoints (normally in service). If not, contact the data scientists
Then, you will be able to test the endpoints locally (see SagemakerAwsClientIntegrationTest)