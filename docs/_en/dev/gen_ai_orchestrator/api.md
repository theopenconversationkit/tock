<a id="top"></a>
# Tock LLM Orchestrator APIs
| #  | Resource                                        | Description                                                   | HTTP Method   | Query params | Body - Response                             |
|----|-------------------------------------------------|---------------------------------------------------------------|---------------|--------------|---------------------------------------------|
| 01 | **/llm-providers**                              | Get all Large Language Model providers covered                | `GET`         |              | [Voir](#api-llm-providers)                  |
| 02 | **/llm-providers/{provider-id}**                | Get a specific Large Language Model provider covered          | `GET`         |              | [Voir](#api-llm-providers-get-one)          |
| 03 | **/llm-providers/{provider-id}/setting**        | Get an example for a specific Large Language Model setting    | `GET`         |              | [Voir](#api-llm-providers-get-setting)      |
| 04 | **/llm-providers/{provider-id}/setting/status** | Check the setting for a specific Large Language Model setting | `POST`        |              | [Voir](#api-llm-providers-post-setting)     |
| 05 | **/em-providers**                               | Get all Embedding Model providers covered                     | `GET`         |              | [Voir](#api-em-providers)                   |
| 06 | **/em-providers/{provider-id}**                 | Get a specific Embedding Model provider covered               | `GET`         |              | [Voir](#api-em-providers-get-one)           |
| 07 | **/em-providers/{provider-id}/setting**         | Get an example for a specific Embedding Model setting         | `GET`         |              | [Voir](#api-em-providers-get-setting)       |
| 08 | **/em-providers/{provider-id}/setting/status**  | Check the setting for a specific Embedding Model setting      | `POST`        |              | [Voir](#api-em-providers-post-setting)      |
| 09 | **/rag**                                        | Ask question by using a knowledge base (documents) as context | `POST`        | debug=true   | [Voir](#api-rag)                            |
| 10 | **/completion/sentence-generation**             | Generate sentences                                            | `POST`        | debug=true   | [Voir](#api-completion-sentence-generation) |
---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-01 : `[GET]` /llm-providers
<a id="api-llm-providers"></a>
#### Response

```python
    class LLMProvidersResponse(BaseModel)
        providers: list[LLMProvider]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-02 : `[GET]` /llm-providers/{provider-id}
<a id="api-llm-providers-get-one"></a>
#### Response

```python
    class LLMProviderResponse(BaseModel):
        provider: LLMProvider
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-03 : `[GET]` /llm-providers/{provider-id}/setting/example
<a id="api-llm-providers-get-setting"></a>
#### Response

```python
    class LLMProviderSettingExampleResponse(BaseModel):
        setting: LLMSetting
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-04 : `[POST]` /llm-providers/{provider-id}/setting/status
<a id="api-llm-providers-post-setting"></a>
#### Body

```python
    class LLMProviderSettingStatusQuery(BaseModel):
        setting: LLMSetting
```
#### Response

```python
    class LLMProviderSettingStatusResponse(BaseModel):
        valid: bool,
        errors: list[Error]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-05 : `[GET]` /em-providers
<a id="api-em-providers"></a>
#### Response

```python
    class EMProvidersResponse(BaseModel)
        providers: list[LLMProvider]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-06 : `[GET]` /em-providers/{provider-id}
<a id="api-em-providers-get-one"></a>
#### Response

```python
    class EMProviderResponse(BaseModel):
        provider: LLMProvider
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-07 : `[GET]` /em-providers/{provider-id}/setting/example
<a id="api-em-providers-get-setting"></a>
#### Response

```python
    class EMProviderSettingExampleResponse(BaseModel):
        setting: EMSetting
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-08 : `[POST]` /em-providers/{provider-id}/setting/status
<a id="api-em-providers-post-setting"></a>
#### Body

```python
    class EMProviderSettingStatusQuery(BaseModel):
        setting: EMSetting
```
#### Response

```python
    class EMProviderSettingStatusResponse(BaseModel):
        valid: bool,
        errors: list[Error]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-09 : `[POST]` /rag
<a id="api-rag"></a>
#### Body :

```python
    class RagQuery(BaseModel):
        history: list[ChatMessage]
        # condense_question_llm_setting: LLMSetting
        # condense_question_prompt_inputs: Any
        question_answering_llm_setting: LLMSetting
        question_answering_prompt_inputs: Any
        embedding_question_em_setting: EMSetting
        document_index_name: str
        document_search_params: DocumentSearchParams
```

#### Response :

```python
    class RagResponse(BaseModel):
        answer: TextWithFootnotes
        debug: Optional[Any] = None
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-10 : `[POST]` /completion/sentence-generation
<a id="api-completion-sentence-generation"></a>
#### Body :

```python
    class SentenceGenerationQuery(BaseModel):
        llm_setting: LLMSetting
```

#### Response :

```python
    class SentenceGenerationResponse(BaseModel):
        sentences: list[str]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

## Schemas
```python
    class LLMProvider(str, Enum):
        OPEN_AI = 'OpenAI'
        AZURE_OPEN_AI_SERVICE = 'AzureOpenAIService'

    class BaseLLMSetting(BaseModel):
        provider: LLMProvider
        api_key: str
        temperature: str
        prompt: str

    class BaseEMSetting(BaseModel):
        provider: LLMProvider
        api_key: str

    class OpenAILLMSetting(BaseLLMSetting):
        provider: Literal[LLMProvider.OPEN_AI]
        model: str

    class AzureOpenAILLMSetting(BaseLLMSetting):
        provider: Literal[LLMProvider.AZURE_OPEN_AI_SERVICE]
        deployment_name: str
        model: Optional[str]
        api_base: str
        api_version: str

    LLMSetting = Annotated[
        Union[OpenAILLMSetting, AzureOpenAILLMSetting],
        Body(discriminator='provider')
    ]

    class OpenAIEMSetting(BaseEMSetting):
        provider: Literal[LLMProvider.OPEN_AI]
        model: str

    class AzureOpenAIEMSetting(BaseEMSetting):
        provider: Literal[LLMProvider.AZURE_OPEN_AI_SERVICE]
        deployment_name: str
        model: Optional[str]
        api_base: str
        api_version: str

    EMSetting = Annotated[
        Union[OpenAIEMSetting, AzureOpenAIEMSetting],
        Body(discriminator='provider')
    ]

    class VectorStoreProvider(str, Enum):
        OPEN_SEARCH = 'OpenSearch'

    class BaseVectorStoreSearchParams(ABC, BaseModel):
        provider: VectorStoreProvider

        
    class OpenSearchParams(BaseVectorStoreSearchParams):
        provider: Literal[VectorStoreProvider.OPEN_SEARCH]
        k: int
        filter: List[OpenSearchTermParams]
        
    class OpenSearchTermParams(BaseModel):
        term: dict
    
    DocumentSearchParams = Annotated[
        Union[OpenSearchParams], Body(discriminator='provider')
    ]

    class Footnote(BaseModel):
        identifier: str
        title: str
        url: Optional[str] = None

    class TextWithFootnotes(BaseModel):
        text: str
        footnotes: list[Footnote]

    class ChatMessageType(str, Enum):
        USER = 'HUMAN'
        AI = 'AI'

    class ChatMessage(BaseModel):
        text: str
        type: ChatMessageType

    class Error(BaseModel):
        code: str
        message: str
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

## Errors
#### General :
```json
    [
      {
        "code": 1000,
        "message": "Unknown AI provider.",
        "detail": null,
        "info": {
          "provider": "TOTO",
          "error": "NotFoundError",
          "cause": "'TOTO' is not accepted. Accepted values are : ['OpenAI', 'AzureOpenAIService']",
          "request": "[POST] http://localhost:8000/llm-providers/TOTO/setting/status"
        }
      },
      {
        "code": 1001,
        "message": "Bad query.",
        "detail": "The request seems to be invalid.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "BadRequestError",
          "cause": "'AzureOpenAIService' is different from the provider ID 'OpenAI' given for setting.",
          "request": "[POST] http://localhost:8000/llm-providers/AzureOpenAIService/setting/status"
        }
      }
    ]
```
#### LLMProvider OpenAI :
```json
    [
      {
        "code": 2001,
        "message": "Connection error to the AI provider API.",
        "detail": "Check the requested URL, your network settings, proxy configuration, SSL certificates, or firewall rules.",
        "info": {
          "provider": "OpenAI",
          "error": "APIConnectionError",
          "cause": "Connection error.",
          "request": "[POST] https://api.openai.com/v1/chat/completions"
        }
      },
      {
        "code": 2002,
        "message": "Authentication error to the AI provider API.",
        "detail": "Check your API key or token and make sure it is correct and active.",
        "info": {
          "provider": "OpenAI",
          "error": "AuthenticationError",
          "cause": "Error code: 401 - {'error': {'message': 'Incorrect API key provided: ab7*****************************IV4B. You can find your API key at https://platform.openai.com/account/api-keys.', 'type': 'invalid_request_error', 'param': None, 'code': 'invalid_api_key'}}",
          "request": "[POST] https://api.openai.com/v1/chat/completions"
        }
      },
      {
        "code": 2003,
        "message": "An AI provider resource was not found.",
        "detail": "The request URL base is correct, but the path or a query parameter is not.",
        "info": {
          "provider": "OpenAI",
          "error": "NotFoundError",
          "cause": "Error code: 404 - {'error': {'message': 'This is not a chat model and thus not supported in the v1/chat/completions endpoint. Did you mean to use v1/completions?', 'type': 'invalid_request_error', 'param': 'model', 'code': None}}",
          "request": "[POST] https://api.openai.com/v1/chat/completions"
        }
      },
      {
        "code": 2004,
        "message": "Unknown AI provider model.",
        "detail": "Consult the official documentation for accepted values.",
        "info": {
          "provider": "OpenAI",
          "error": "NotFoundError",
          "cause": "Error code: 404 - {'error': {'message': 'The model `gpt-3.5-TOTO` does not exist', 'type': 'invalid_request_error', 'param': None, 'code': 'model_not_found'}}",
          "request": "[POST] https://api.openai.com/v1/chat/completions"
        }
      },
      {
        "code": 2007,
        "message": "The model's context length has been exceeded.",
        "detail": "Reduce the length of the prompt message.",
        "info": {
          "provider": "OpenAI",
          "error": "BadRequestError",
          "cause": "Error code: 400 - {'error': {'message': \"This model's maximum context length is 4097 tokens. However, your messages resulted in 29167 tokens. Please reduce the length of the messages.\", 'type': 'invalid_request_error', 'param': 'messages', 'code': 'context_length_exceeded'}}",
          "request": "[POST] https://api.openai.com/v1/chat/completions"
        }
      }
    ]
```

#### LLMProvider AzureOpenAIService :
```json
    [
      {
        "code": 2001,
        "message": "Connection error to the AI provider API.",
        "detail": "Check the requested URL, your network settings, proxy configuration, SSL certificates, or firewall rules.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "APIConnectionError",
          "cause": "Connection error.",
          "request": "[POST] https://conversationnel-api-arkea.azure-oapi.net///openai/deployments/squadconv-gpt4/chat/completions?api-version=2023-03-15-preview"
        }
      },
      {
        "code": 2002,
        "message": "Authentication error to the AI provider API.",
        "detail": "Check your API key or token and make sure it is correct and active.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "AuthenticationError",
          "cause": "Error code: 401 - {'statusCode': 401, 'message': 'Access denied due to invalid subscription key. Make sure to provide a valid key for an active subscription.'}",
          "request": "[POST] https://conversationnel-api-arkea.azure-api.net///openai/deployments/squadconv-gpt4/chat/completions?api-version=2023-03-15-preview"
        }
      },
      {
        "code": 2003,
        "message": "An AI provider resource was not found.",
        "detail": "The request URL base is correct, but the path or a query parameter is not.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "NotFoundError",
          "cause": "Error code: 404 - {'error': {'code': '404', 'message': 'Resource not found'}}",
          "request": "[POST] https://conversationnel-api-arkea.azure-api.net///openai/deployments/squadconv-gpt4/chat/completions?api-version=2023-03-15-toto"
        }
      },
      {
        "code": 2005,
        "message": "Unknown AI provider deployment.",
        "detail": "Consult the official documentation for accepted values.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "NotFoundError",
          "cause": "Error code: 404 - {'error': {'code': 'DeploymentNotFound', 'message': 'The API deployment for this resource does not exist. If you created the deployment within the last 5 minutes, please wait a moment and try again.'}}",
          "request": "[POST] https://conversationnel-api-arkea.azure-api.net///openai/deployments/squadconv-gpt4f/chat/completions?api-version=2023-03-15-preview"
        }
      },
      {
        "code": 2006,
        "message": "AI provider API error.",
        "detail": "Bad request.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "BadRequestError",
          "cause": "Error code: 400 - {'error': {'code': 'OperationNotSupported', 'message': 'The embeddings operation does not work with the specified model, gpt-4. Please choose different model and try again. You can learn more about which models can be used with each operation here: https://go.microsoft.com/fwlink/?linkid=2197993.'}}",
          "request": "[POST] https://conversationnel-api-arkea.azure-api.net//openai/deployments/squadconv-gpt4/embeddings?api-version=2023-03-15-preview"
        }
      },
      {
        "code": 2007,
        "message": "The model's context length has been exceeded.",
        "detail": "Reduce the length of the prompt message.",
        "info": {
          "provider": "AzureOpenAIService",
          "error": "BadRequestError",
          "cause": "Error code: 400 - {'error': {'message': \"This model's maximum context length is 8192 tokens. However, your messages resulted in 29167 tokens. Please reduce the length of the messages.\", 'type': 'invalid_request_error', 'param': 'messages', 'code': 'context_length_exceeded'}}",
          "request": "[POST] https://conversationnel-api-arkea.azure-api.net///openai/deployments/squadconv-gpt4/chat/completions?api-version=2023-03-15-preview"
        }
      }
    ]
```
