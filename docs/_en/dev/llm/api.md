<a id="top"></a>
# Tock LLM Orchestrator APIs
| #  | Resource                                 | Description                                                   | HTTP Method   | Query params | Body - Response                                 | 
|----|------------------------------------------|---------------------------------------------------------------|---------------|--------------|-------------------------------------------------|
| 01 | **/llm-providers**                       | Get all Large Language Model providers covered                | `GET`         |              | [Voir](#api-llm-providers)                      |
| 02 | **/llm-providers/{provider-id}**         | Get a specific Large Language Model provider covered          | `GET`         |              | [Voir](#api-llm-providers-get-one)         |
| 03 | **/llm-providers/{provider-id}/setting** | Get an example for a specific Large Language Model setting    | `GET`         |              | [Voir](#api-llm-providers-get-setting)     |
| 04 | **/llm-providers/{provider-id}/setting** | Check the setting for a specific Large Language Model setting | `POST`        |              | [Voir](#api-llm-providers-post-setting)    |
| 05 | **/em-providers**                        | Get all Embedding Model providers covered                     | `GET`         |              | [Voir](#api-em-providers)                  |
| 06 | **/em-providers/{provider-id}**          | Get a specific Embedding Model provider covered               | `GET`         |              | [Voir](#api-em-providers-get-one)          |
| 07 | **/em-providers/{provider-id}/setting**  | Get an example for a specific Embedding Model setting         | `GET`         |              | [Voir](#api-em-providers-get-setting)      |
| 08 | **/em-providers/{provider-id}/setting**  | Check the setting for a specific Embedding Model setting      | `POST`        |              | [Voir](#api-em-providers-post-setting)     |
| 09 | **/rag**                                 | Ask question by using a knowledge base (documents) as context | `POST`        | debug=true   | [Voir](#api-rag)                           |
| 10 | **/completion/generate-sentences**       | Generate sentences                                            | `POST`        | debug=true   | [Voir](#api-completion-generate-sentences) |
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
        question: str
        history: list[ChatMessage]
        # condense_question_llm_setting: LLMSetting
        question_answering_llm_setting: LLMSetting
        embedding_question_em_setting: EMSetting
        index_name: str
        metadata_filters: list[MetadataFilter]
```

#### Response :

```python
    class RagResponse(BaseModel):
        answer: TextWithFootnotes
        debug: list[Any]
```

---
<p align="right">(<a href="#top">back to top</a>)</p>

#### API-10 : `[POST]` /completion/generate-sentences
<a id="api-completion-generate-sentences"></a>
#### Body :

```python
    class GenerateSentencesQuery(BaseModel):
        llm_setting: LLMSetting
```

#### Response :

```python
    class GenerateSentencesResponse(BaseModel):
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
        model: str
        temperature: str
        prompt: str

    class BaseEMSetting(BaseModel):
        provider: LLMProvider
        api_key: str
        model: str

    class OpenAILLMSetting(BaseLLMSetting):
        provider: Literal[LLMProvider.OPEN_AI]

    class AzureOpenAILLMSetting(BaseLLMSetting):
        provider: Literal[LLMProvider.AZURE_OPEN_AI_SERVICE]
        deployment_name: str
        api_base: str
        api_version: str

    LLMSetting = Annotated[
        Union[OpenAILLMSetting, AzureOpenAILLMSetting], 
        Body(discriminator='provider')
    ]

    class OpenAIEMSetting(BaseEMSetting):
        provider: Literal[LLMProvider.OPEN_AI]

    class AzureOpenAIEMSetting(BaseEMSetting):
        provider: Literal[LLMProvider.AZURE_OPEN_AI_SERVICE]
        deployment_name: str
        api_vase: str
        api_version: str

    EMSetting = Annotated[
        Union[OpenAIEMSetting, AzureOpenAIEMSetting], 
        Body(discriminator='provider')
    ]

    class Footnote(BaseModel):
        identifier: str
        title: str
        url: Union[str, None] = None

    class TextWithFootnotes(BaseModel):
        text: str
        footnotes: list[Footnote]

    class ChatMessageType(str, Enum):
        USER = 'HUMAN'
        AI = 'AI'

    class ChatMessage(BaseModel):
        text: str
        type: ChatMessageType

    class MetadataFilter(BaseModel):
        name: str
        value: str

    class Error(BaseModel):
        code: str
        message: str

```
