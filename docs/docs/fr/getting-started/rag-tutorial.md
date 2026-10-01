---
title: RAG sur la documentation Tock
---

# Construire un bot RAG sur la documentation Tock

Dans ce tutoriel, vous allez construire un bot qui répond aux questions sur Tock, en utilisant la documentation de Tock
elle-même comme base de connaissances. Le bot s'appuie sur le [RAG](../gen-ai/rag.md) (_Retrieval-Augmented Generation_) :
pour chaque question, Tock recherche les passages les plus pertinents de la documentation,
puis demande à un LLM de rédiger une réponse à partir de ces passages, avec les liens vers les sources.

Tout tourne sur votre machine : la plateforme Tock avec Docker, et les modèles avec [Ollama](https://ollama.com/).

## Ce que vous allez créer

* Une plateforme Tock locale avec l'orchestrateur Gen AI et une base vectorielle [PGVector](https://github.com/pgvector/pgvector)
* Un index vectoriel de la documentation Tock
* Un bot qui répond à des questions comme _"Which vector stores are supported?"_, avec les liens vers les pages de la documentation

## Prérequis

* Environ 30 minutes
* [Git](https://git-scm.com/), [Python 3](https://www.python.org/) (aucun paquet supplémentaire n'est nécessaire),
  et des versions récentes de [Docker](https://www.docker.com/) et [Docker Compose](https://docs.docker.com/compose/)
* [Ollama](https://ollama.com/download), et environ 16 Go de RAM pour faire tourner la plateforme et un modèle 7B
* Tock **26.3.5** ou plus récent : les versions précédentes de l'outil d'indexation échouent au démarrage, et leur
  schéma PostgreSQL ne permet pas la recherche hybride utilisée plus bas

> **GPU ou pas ?** Avec un GPU (ou un Mac Apple Silicon), une réponse prend de quelques secondes à une trentaine
> de secondes (20 à 35 secondes sur un Mac M4 avec 16 Go de RAM). Sur CPU uniquement, un modèle 7B demande
> **plusieurs minutes** par réponse : la plateforme doit alors être démarrée avec des délais d'attente plus longs,
> voir [Utilisation sur CPU](#utilisation-sur-cpu).
> Vous pouvez aussi utiliser un LLM hébergé, voir [Utiliser OpenAI au lieu d'Ollama](#utiliser-openai-au-lieu-dollama).

### Sous macOS

* Installez l'[application Ollama](https://ollama.com/download/mac), et non une image Docker :
  les conteneurs Docker n'ont pas accès au GPU du Mac.
* Docker peut être fourni par [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  ou par [Colima](https://github.com/abiosoft/colima). Avec Colima :
    * la machine virtuelle n'a que 2 Go de mémoire par défaut, alors que la plateforme en utilise près de 3 :
      démarrez-la avec plus de ressources, par exemple `colima start --cpu 4 --memory 6`
      (les nouvelles valeurs sont conservées pour les démarrages suivants) ;
    * la commande `docker compose` nécessite le plugin Docker Compose : `brew install docker-compose`,
      puis ajoutez son dossier à `cliPluginsExtraDirs` dans `~/.docker/config.json`, comme indiqué à la fin de l'installation ;
    * clonez les dépôts dans votre dossier personnel : par défaut, Colima ne partage que celui-ci avec la machine virtuelle.
      Ailleurs (par exemple dans `/tmp`), les fichiers montés par Docker Compose apparaissent vides dans les conteneurs :
      ni le replica set MongoDB ni le schéma PostgreSQL ne sont initialisés.

## Préparer les modèles

Le bot utilise deux modèles :

* un **modèle d'embedding**, `mxbai-embed-large`, qui transforme les textes en vecteurs pour rechercher dans la documentation,
* un **LLM**, `qwen2.5:7b`, qui rédige les réponses.

```shell
ollama pull mxbai-embed-large
ollama pull qwen2.5:7b
```

Le prompt RAG de Tock, avec les documents retrouvés, dépasse la fenêtre de contexte par défaut d'Ollama
(4096 tokens) : le prompt serait tronqué, et le LLM ne répondrait pas au format JSON attendu.
Créez une variante du modèle avec une fenêtre de contexte plus grande :

```shell
printf 'FROM qwen2.5:7b\nPARAMETER num_ctx 16384\n' > Modelfile
ollama create qwen2.5-16k -f Modelfile
```

Les conteneurs Tock joignent Ollama via l'adresse `host.docker.internal`
(les fichiers Docker Compose de Tock la font pointer vers l'hôte, y compris sous Linux).
Sous Linux, Ollama n'écoute par défaut que sur `127.0.0.1` : faites-le écouter sur toutes les interfaces, par exemple
lorsqu'il tourne comme service systemd :

```shell
sudo systemctl edit ollama
# ajoutez les lignes suivantes, puis enregistrez :
#   [Service]
#   Environment="OLLAMA_HOST=0.0.0.0"
sudo systemctl restart ollama
```

> Sous macOS (Docker Desktop ou Colima) et Windows, `host.docker.internal` atteint Ollama sans configuration :
> cette étape n'est pas nécessaire.

## Démarrer la plateforme

Le dépôt [Tock Docker](https://github.com/theopenconversationkit/tock-docker) fournit un fichier Docker Compose
avec tout le nécessaire pour le RAG : _Tock Studio_, _Bot API_, l'orchestrateur Gen AI, et une base PostgreSQL
avec l'extension PGVector.

```shell
git clone https://github.com/theopenconversationkit/tock-docker.git && cd tock-docker
chmod +x scripts/setup.sh
docker compose -f docker-compose-rag-pgvector.yml up -d
```

> La version de Tock est définie par la variable `TAG` du fichier `.env`.

Au premier démarrage, la base PostgreSQL est initialisée avec le schéma de l'orchestrateur Gen AI,
dont la colonne de recherche plein texte utilisée par la recherche hybride.

> Si le volume `pgvector-postgres-vl` a été créé par une version antérieure de Tock Docker, ce schéma est absent
> et la recherche hybride échoue (le bot répond _"Technical error"_) : supprimez le volume avec
> `docker compose -f docker-compose-rag-pgvector.yml down -v` (ce qui supprime aussi les données Tock), ou appliquez
> `scripts/pgvector/init.sql` à la base.

Après une minute environ, _Tock Studio_ est disponible sur [http://localhost](http://localhost).
Connectez-vous avec `admin@app.com` / `password`.

## Créer l'application

À la première connexion, un assistant vous invite à créer une application :

* _Choose your language_ : sélectionnez _English_, puis _Next_
* _Select a first Channel_ : sélectionnez _web_, puis _Next_
* _Create your Assistant_ : _Create_

L'assistant crée une application nommée `new_assistant`, dans le namespace `app` (le namespace des utilisateurs par défaut
de la plateforme Docker). Les documents sont indexés pour un bot donné : l'outil d'indexation a besoin de ce namespace
et de ce nom d'application.

## Convertir la documentation en fichier CSV

L'[outil d'indexation](../gen-ai/indexing.md) lit les documents dans un fichier CSV avec les colonnes `title`, `source` et `text`.
La documentation Tock est un ensemble de fichiers Markdown, dans le dossier `docs/docs` du dépôt Tock :
le script suivant transforme chaque section de chaque page en une ligne du fichier CSV, avec comme source l'URL
de la section sur [doc.tock.ai](https://doc.tock.ai/).

Récupérez la documentation, à côté du dossier `tock-docker`, et créez un dossier de travail :

```shell
cd ..
git clone --depth 1 https://github.com/theopenconversationkit/tock.git
mkdir -p tock-rag && cd tock-rag
```

Créez un fichier `docs_to_csv.py` dans le dossier `tock-rag` :

```python
"""Converts the Tock documentation (Markdown) into a CSV file for the Tock indexing tools.

Usage: python3 docs_to_csv.py <docs folder> <output csv> [<base url>]

Each section (## heading) of each page becomes a row: title | source | text.
"""
import csv
import re
import sys
import unicodedata
from pathlib import Path

docs_dir = Path(sys.argv[1])
output = Path(sys.argv[2])
base_url = sys.argv[3] if len(sys.argv) > 3 else 'https://doc.tock.ai/tock/master/'
# Pages that are not published on doc.tock.ai (exclude_docs in mkdocs.yml)
excluded_pages = 'prompt-example-type-*'


def slugify(heading):
    # Same anchors as the MkDocs table of contents
    custom_id = re.search(r'\{#([\w-]+)\}$', heading)
    if custom_id:
        return custom_id.group(1)
    text = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', heading)  # links: keep the label
    text = re.sub(r'[`*]|\b_|_\b', '', text)  # code and emphasis markers, not underscores inside words
    text = unicodedata.normalize('NFKD', text).encode('ascii', 'ignore').decode()
    text = re.sub(r'[^\w\s-]', '', text).strip().lower()
    return re.sub(r'[-\s]+', '-', text)


def clean(text):
    text = re.sub(r'<!--.*?-->', '', text, flags=re.DOTALL)  # comments
    text = re.sub(r'!\[[^\]]*\]\([^)]*\)', '', text)  # images
    text = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', text)  # links: keep the label
    text = re.sub(r'-{4,}', '---', text)  # table separators: fewer tokens
    return re.sub(r'\n{3,}', '\n\n', text).strip()


rows = []
for page in sorted(docs_dir.rglob('*.md')):
    if page.match(excluded_pages):
        continue
    content = page.read_text(encoding='utf-8')
    page_title = page.stem
    front_matter = re.match(r'^---\n(.*?)\n---\n', content, re.DOTALL)
    if front_matter:
        content = content[front_matter.end():]
        title = re.search(r'^title:\s*(.+)$', front_matter.group(1), re.MULTILINE)
        if title:
            page_title = title.group(1).strip().strip('"\'')
    url = base_url + page.relative_to(docs_dir).with_suffix('.html').as_posix()

    # Split the page on level 2 headings, outside code blocks
    sections, heading, lines, in_code = [], None, [], False
    for line in content.splitlines():
        if line.lstrip().startswith('```'):
            in_code = not in_code
        if not in_code and line.startswith('## '):
            sections.append((heading, lines))
            heading, lines = line[3:].strip(), []
        else:
            lines.append(line)
    sections.append((heading, lines))

    for heading, lines in sections:
        text = clean('\n'.join(lines))
        if len(text) < 50:
            continue
        if heading:
            label = re.sub(r'\s*\{#[\w-]+\}$', '', heading)  # without the custom anchor
            title, source = f'{page_title} - {label}', f'{url}#{slugify(heading)}'
        else:
            title, source = page_title, url
        rows.append({'title': title, 'source': source, 'text': text})

output.parent.mkdir(parents=True, exist_ok=True)
with output.open('w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['title', 'source', 'text'], delimiter='|', quotechar='"')
    writer.writeheader()
    writer.writerows(rows)
print(f'{len(rows)} sections written to {output}')
```

Lancez-le sur la documentation en anglais. Le fichier CSV est écrit là où l'outil d'indexation l'attend :
`<dossier de base>/<namespace>-<nom de l'application>/input/`.

```shell
python3 docs_to_csv.py ../tock/docs/docs/en ingestion/app-new_assistant/input/tock-doc.csv
```

```
405 sections written to ingestion/app-new_assistant/input/tock-doc.csv
```

> Pour indexer la documentation en français, lancez le script sur `../tock/docs/docs/fr` avec l'URL de base
> `https://doc.tock.ai/tock/master/fr/` en troisième argument, et choisissez _French_ comme langue de l'application.

## Indexer la documentation

Créez le fichier de configuration de l'outil d'indexation, `ingestion/config.json` :

```json
{
  "bot": {
    "namespace": "app",
    "bot_id": "new_assistant",
    "file_location": "/ingestion"
  },
  "em_setting": {
    "provider": "Ollama",
    "model": "mxbai-embed-large",
    "base_url": "http://host.docker.internal:11434"
  },
  "vector_store_setting": {
    "provider": "PGVector",
    "host": "pgvector_postgres",
    "port": 5432,
    "username": "postgres",
    "password": {
      "type": "Raw",
      "secret": "ChangeMe"
    },
    "database": "postgres"
  },
  "data_csv_file": "tock-doc.csv",
  "document_index_name": null,
  "chunk_size": 1000,
  "embedding_bulk_size": 20,
  "embedding_max_chunks": null,
  "ignore_source": false,
  "append_doc_title_and_chunk": true
}
```

* `bot` : le namespace et le nom d'application du bot. `file_location` est le dossier contenant les fichiers CSV,
  vu depuis le conteneur.
* `em_setting` : le modèle d'embedding. Le même modèle doit être choisi dans les réglages RAG du bot.
* `chunk_size` : la taille maximale d'un morceau, en caractères. `mxbai-embed-large` lit au plus 512 tokens :
  des morceaux plus grands font échouer l'indexation avec _"the input length exceeds the context length"_.
* `vector_store_setting` : la base PostgreSQL démarrée par Docker Compose.
* `document_index_name` : laissez-le à `null`, pour que l'index soit nommé d'après le namespace, le bot et la session
  d'indexation, comme l'attend _Tock Studio_.
* `append_doc_title_and_chunk` : ajoute le titre (page et section) au début de chaque morceau, ce qui améliore la recherche.

Lancez l'outil d'indexation, disponible sous forme d'image Docker `tock/llm-indexing-tools`, sur le réseau de la plateforme.
L'option `-v` affiche la progression et le résumé de l'indexation :

```shell
docker run --rm \
  --network tock-docker_default \
  --add-host host.docker.internal:host-gateway \
  -v "$PWD/ingestion:/ingestion" \
  tock/llm-indexing-tools:{{ tock_version }} \
  python tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py \
  --json-config-file=/ingestion/config.json -v
```

> Le nom du réseau est `<dossier du fichier Docker Compose>_default` : `tock-docker_default` si vous avez cloné
> le dépôt avec son nom par défaut. `docker network ls` liste les réseaux.

Le calcul des embeddings de toute la documentation prend moins d'une minute avec un GPU ou un Mac Apple Silicon,
et quelques minutes sur CPU. À la fin, l'outil affiche un résumé :

```
------------------------------ RUN VECTORISATION OUTPUT ------------------------------
Index name             : ns_app_bot_new_assistant_session_7fab9630_3a85_403d_ad43_233ec15fd7e7
Index session ID       : 7fab9630-3a85-403d-ad43-233ec15fd7e7
Documents extracted    : 405 (Docs)
Documents chunked      : 737 (Chunks)
Duration               : 44.01 seconds
...
Status                 : COMPLETED
```

Notez l'**Index session ID** : il indique au bot quels documents utiliser.

## Configurer le RAG

Dans _Tock Studio_, allez dans _Gen AI_ > _Rag settings_ :

* **Question condensing** et **Question answering**, _Configuration_ :
    * Provider : _Ollama_
    * BaseUrl : `http://host.docker.internal:11434`
    * Model : `qwen2.5-16k`
    * Temperature : `0` (un petit modèle local respecte plus sûrement le format de réponse attendu)
* **Embedding**, _Configuration_ :
    * Provider : _Ollama_
    * BaseUrl : `http://host.docker.internal:11434`
    * Model : `mxbai-embed-large`
* **Indexing session** :
    * Indexing session id : l'**Index session ID** affiché par l'outil d'indexation
    * Search type : _Hybrid search_ (combine la recherche vectorielle et une recherche par mots-clés)
* Activez **Rag activated** et **Dialogs debug**
* _Save_

Une fois les réglages enregistrés, rechargez la page : le champ _Vector database index name_ affiche le nom de l'index créé par l'outil d'indexation.

> Inutile de configurer la base vectorielle dans _Gen AI_ > _Vector DB settings_ : par défaut,
> l'orchestrateur utilise la base PostgreSQL de la plateforme (définie par des variables d'environnement dans le fichier Docker Compose).

## Tester le bot

Allez dans _Test_ > _Test_ et posez une question sur Tock, par exemple :

* _Which vector stores are supported?_
* _How do I deploy Tock with Docker?_
* _How can I connect my bot to WhatsApp?_

Le bot n'a encore aucune intention : chaque question est qualifiée _unknown_ et traitée par le RAG.
La réponse est accompagnée des liens vers les sections de la documentation sur lesquelles elle s'appuie, par exemple :

> The vector stores supported by Tock include PGVector (PostgreSQL) and OpenSearch.
>
> Sources : _Vector store providers_

Le bot peut aussi être appelé en dehors de _Tock Studio_, avec le connecteur web créé par l'assistant :

```shell
curl -X POST http://localhost:8080/io/app/new_assistant/web \
  -H 'Content-Type: application/json' \
  -d '{"query": "Which vector stores are supported?", "userId": "my-user", "locale": "en"}'
```

Avec _Dialogs debug_ activé, _Analytics_ > _Dialogs_ affiche, pour chaque réponse, la question reformulée
et les morceaux de documentation envoyés au LLM.

## Utilisation sur CPU

Sans GPU, une réponse du modèle 7B prend deux à trois minutes, alors que la plateforme abandonne au bout d'une minute
(le bot répond alors _"Technical error"_ ou _"technical error :( timeout"_). Allongez les délais d'attente avec un fichier
`docker-compose-slow-llm.yml`, dans le dossier `tock-docker` :

```yaml
services:
  bot_api:
    environment:
      # Délai d'attente des appels à l'orchestrateur Gen AI
      - tock_gen_ai_orchestrator_client_request_timeout_ms=600000
      # Délai avant que le bot ne ferme le canal de réponse
      - tock_cleanup_delay_seconds=600
  admin_web:
    environment:
      # Délai d'attente de l'écran de test de Tock Studio
      - tock_bot_rest_client_request_timeout_ms=600000
```

Puis redémarrez la plateforme avec les deux fichiers :

```shell
docker compose -f docker-compose-rag-pgvector.yml -f docker-compose-slow-llm.yml up -d
```

Les modèles plus petits (3B) sont plus rapides, mais ne respectent pas de façon fiable le format de réponse JSON
exigé par le prompt RAG.

## Utiliser OpenAI au lieu d'Ollama

Pour utiliser OpenAI, choisissez le fournisseur _OpenAI_ avec votre clé d'API dans les réglages RAG, par exemple avec le LLM
`gpt-4o-mini` et le modèle d'embedding `text-embedding-3-small` (qui accepte des morceaux plus grands, par exemple `"chunk_size": 2000`), et remplacez le `em_setting` de la configuration
d'indexation par :

```json
"em_setting": {
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "secret": "sk-..."
  },
  "model": "text-embedding-3-small",
  "base_url": "https://api.openai.com/v1"
}
```

La documentation doit alors être indexée à nouveau avec ce modèle, et le nouvel identifiant de session d'indexation saisi
dans les réglages RAG : le modèle d'embedding des réglages RAG doit toujours être celui utilisé pour l'indexation.

## Aller plus loin

* Améliorez les réponses avec le [prompt RAG](../gen-ai/rag-prompt.md) et le [_Rag prompt context_](../gen-ai/rag-prompt-context.md)
  (sujets couverts et exclus, lexique métier).
* Comprenez pourquoi une section de la documentation est (ou n'est pas) utilisée avec le [diagnostic de recherche](../gen-ai/vector-store-inspection.md).
* Mesurez la qualité des réponses avec les [jeux de données et évaluations](../gen-ai/answers-quality.md).
* Tracez les appels aux LLM avec un [fournisseur d'observabilité](../gen-ai/observability.md).
* Intégrez le bot dans une page web avec le [connecteur Web](../channels/web.md).
