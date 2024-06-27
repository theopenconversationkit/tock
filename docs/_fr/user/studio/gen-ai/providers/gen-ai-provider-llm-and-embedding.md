
# Fournisseurs de l'IA pris en compte par Tock.


<table>
<tr>
<td>

**Fournisseur de l'IA**
</td> 
<td> 

**Configuration du LLM**
</td>
<td> 

**Configuration de l'Embedding**
</td>
</tr>
<tr>
<td style="text-align: center;">

`OpenAI` <br />
[Docs](https://platform.openai.com/docs/introduction)
</td>
<td style="vertical-align: top;">

```json
{
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "value": "aebb4b****************7b25e3371"
  },
  "temperature": "1.0",
  "prompt": "Customized prompt for the use case",
  "model": "gpt-3.5-turbo"
}
```
</td>
<td style="vertical-align: top;">

```json
{
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "value": "aebb4b****************7b25e3371"
  },
  "model": "text-embedding-ada-002"
}
```
</td>
</tr>
<tr>
<td style="text-align: center;">

`AzureOpenAIService` <br />
[Docs](https://azure.microsoft.com/fr-fr/products/ai-services/openai-service)
</td>
<td style="vertical-align: top;">

```json
{
  "provider": "AzureOpenAIService",
  "api_key": {
    "type": "Raw",
    "value": "aebb4b****************7b25e3371"
  },
  "temperature": "1.0",
  "prompt": "Customized prompt for the use case",
  "api_base": "https://custom-api-name.azure-api.net",
  "deployment_name": "custom-deployment-name",
  "api_version": "2024-03-01-preview"
}
```
</td>
<td style="vertical-align: top;">

```json
{
  "provider": "AzureOpenAIService",
  "api_key": {
    "type": "Raw",
    "value": "aebb4b****************7b25e3371"
  },
  "api_base": "https://custom-api-name.azure-api.net",
  "deployment_name": "custom-deployment-name",
  "api_version": "2024-03-01-preview"
}
```
</td>
</tr>
</table>