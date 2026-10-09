---
title: Fournisseurs d'observabilité des LLMs
description: "Les outils d'observabilité LLM pris en charge par Tock pour tracer prompts, réponses, latence et coût."
---

# Fournisseurs d'observabilité des LLMs

Un fournisseur d'observabilité trace les appels aux LLM faits par l'orchestrateur Gen AI (prompts, réponses, latence, coût).
Il se configure dans le menu [_Observability settings_](../observability.md).

Voici les fournisseurs d'observabilité des LLM pris en compte par Tock :

| Fournisseur | Valeur de `provider` |
|-------------|----------------------|
| [Langfuse](https://langfuse.com/docs) | `Langfuse` |

## Langfuse

```json
{
  "provider": "Langfuse",
  "url": "http://localhost:3000",
  "public_url": "https://langfuse.example.com",
  "secret_key": {
    "type": "Raw",
    "secret": "sk-lf-****************-ceabe45abe8f"
  },
  "public_key": "pk-lf-****************-b77e68ef7d2c"
}
```

| Champ | Description |
|-------|-------------|
| `url` | URL utilisée par l'orchestrateur pour joindre le serveur Langfuse (par défaut : `http://localhost:3000`) |
| `public_url` | URL facultative du serveur Langfuse vue depuis le navigateur des utilisateurs, utilisée pour les liens vers les traces dans _Tock Studio_. Vaut `url` par défaut |
| `secret_key` | Clé secrète Langfuse |
| `public_key` | Clé publique Langfuse |
