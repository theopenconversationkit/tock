---
title: Fournisseurs de base vectorielle
---

# Vector base providers.

Here is the list of vector base providers supported by Tock:

<table>
<thead>
<tr>
<th style="font-weight:bold">Vector base providers</th>
<th style="font-weight:bold">Configuration</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align: center;" markdown="span">

`OpenSearch` <br />
[Docs](https://opensearch.org/docs/latest/about/)
</td>
<td style="vertical-align: top;">
<pre>
{
  "provider": "OpenSearch",
  "host": "localhost",
  "port": "9200",
  "user": "admin",
  "password": {
    "type": "Raw",
    "value": "admin"
  }
}
</pre>
</td>
</tr>
<tr>
<td style="text-align: center;" markdown="span">

`PGVector` <br />
[Docs](https://github.com/pgvector/pgvector)
</td>
<td style="vertical-align: top;">
<pre>
{
  "provider": "PGVector",
  "host": "localhost",
  "port": "5432",
  "user": "postgres",
  "password": {
    "type": "Raw",
    "value": "postgres"
  },
  "database": "postgres"
}
</pre>
</td>
</tr>
</tbody>
</table>