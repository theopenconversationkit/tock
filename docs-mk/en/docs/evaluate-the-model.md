# Evaluate the relevance of a NLP model

In _Model Quality_ various views give insights about the relevance of the model:

1. **Model Stats**: Monitores model perfomance in production:
    * self-evaluation of the model about its relevance in terms of recognition of intent and entities
    * number of calls and errors
    * average execution time
2. **Intent Distance**: the distance between intents    
3. **Model Builds**: history and details about model builds    
4. **Test Trends**: evolution of the relevance of [model tests](#partial-model-tests) 
5. **Test Intent Errors**: the list of intent errors found with model tests
6. **Test Entity Errors**: the list of entity errors found with model tests


## Model Tests 101

Model tests are used to detect qualifications errors.

Temporarily models are built from a random part of the whole sentence set of the model (90% for example)
and then tested against the remaining sentences. 

The process is repeated a number of times and the most frequent errors are pushed to an admin user.

Model tests are useful only with large models.

## Test Intent errors

Click on the *Intent Errors* tab:

![schéma Tock](img/intent-errors.png "Intent Errors Detection")

Since the picture above is built from a very simple model, no real error has been detected.
 We can nevertheless note that in some cases the model is systematically wrong with a high probability.  

## Test Entity errors

These errors can be viewed via the *Entity Errors* tab.

![schéma Tock](img/entity-errors.png "Entity Errors Detection")