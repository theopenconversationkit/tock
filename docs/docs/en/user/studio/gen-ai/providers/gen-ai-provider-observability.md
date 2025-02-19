---
title: Fournisseurs d'observabilité des LLMs
---

# LLM observability providers.

Here is the list of LLM observability providers supported by Tock:

<table>
<thead>
<tr>
<th style="font-weight:bold">LLM observability providers</th>
<th style="font-weight:bold">Configuration</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align: center;" markdown="span">

`Langfuse` <br />
([Docs](https://langfuse.com/docs))
</td>
<td style="vertical-align: top;">
<pre>
{
  "provider": "Langfuse",
  "url": "http://localhost:3000",
  "secret_key": {
    "type": "Raw",
    "value": "sk-lf-****************-ceabe45abe8f"
  },
  "public_key": "pk-lf-****************-b77e68ef7d2c"
}
</pre>
</td>
</tr>
</tbody>
</table>