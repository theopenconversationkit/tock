---
title: Fournisseurs de l'IA pris en compte par Tock
---

# Fournisseurs de l'IA pris en compte par Tock.



<table>
<thead>
<tr>
<th style="font-weight:bold">Fournisseur de l'IA</th>
<th style="font-weight:bold">Configuration du LLM</th>
<th style="font-weight:bold">Configuration de l'Embedding</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align: center;">
OpenAI<br/>
(<a href="https://platform.openai.com/docs/introduction">Docs</a>)
</td>
<td style="vertical-align: top;">
<pre>
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
</pre>
</td>
<td style="vertical-align: top;">
<pre>
{
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "value": "aebb4b****************7b25e3371"
  },
  "model": "text-embedding-ada-002"
}
</pre>
</td>
</tr>
<tr>
<td style="text-align: center;">
AzureOpenAIService <br />
(<a href="https://azure.microsoft.com/fr-fr/products/ai-services/openai-service">Docs</a>)
</td>
<td style="vertical-align: top;">
<pre>
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
</pre>
</td>
<td style="vertical-align: top;">
<pre>
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
</pre>
</td>
</tr>
</tbody>
</table>