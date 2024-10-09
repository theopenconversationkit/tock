---
title: Fournisseurs de base vectorielle pris en compte par Tock
---

# Fournisseurs de base vectorielle pris en compte par Tock.


<table>
<tr>
<td>

**Fournisseur de base vectorielle**
</td>
<td> 

**Configuration**
</td>
</tr>
<tr>
<td style="text-align: center;">

`OpenSearch` <br />
[Docs](https://opensearch.org/docs/latest/about/)
</td>
<td style="vertical-align: top;">

```json
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
```
</td>
</tr>
<tr>
<td style="text-align: center;">

`PGVector` <br />
[Docs](https://github.com/pgvector/pgvector)
</td>
<td style="vertical-align: top;">

```json
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
```
</td>
</tr>
</table>