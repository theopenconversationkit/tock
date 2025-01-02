---
title: Fournisseurs d'observabilité des LLMs
---

# Fournisseurs d'observabilité des LLMs.

Voici la liste des fournisseurs d'observabilité des LLM pris en compte par Tock :

<table>
<thead>
<tr>
<th style="font-weight:bold">Fournisseur d'observabilité</th>
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