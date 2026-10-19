---
title: RAG on the Tock documentation
description: "Build a bot that answers questions about Tock from its documentation with RAG, in about 30 minutes."
---

# Build a RAG bot on the Tock documentation

In this tutorial, you will build a bot that answers questions about Tock, using the Tock documentation itself
as its knowledge base. The bot relies on [RAG](../gen-ai/rag.md) (Retrieval-Augmented Generation):
for each question, Tock searches the documentation for the most relevant passages,
then asks an LLM to write an answer from them, with links to the sources.

Everything runs on your machine: the Tock platform with Docker, and the models with [Ollama](https://ollama.com/).

## What you will create

* A local Tock platform with the Gen AI orchestrator and a [PGVector](https://github.com/pgvector/pgvector) vector store
* A vector index of the Tock documentation
* A bot that answers questions such as _"Which vector stores are supported?"_, with links to the documentation pages

## Prerequisites

* About 30 minutes
* [Git](https://git-scm.com/), [Python 3](https://www.python.org/) (no extra package is needed),
  and recent versions of [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)
* [Ollama](https://ollama.com/download), and about 16 GB of RAM to run the platform and a 7B model
* Tock **26.9.0** or later: earlier versions of the indexing tool fail at startup, and their PostgreSQL schema
  does not support the hybrid search used below

> **GPU or not?** With a GPU (or an Apple Silicon Mac), an answer takes from a few seconds to about thirty seconds
> (20 to 35 seconds on an M4 Mac with 16 GB of RAM). On a CPU only, a 7B model needs **several minutes** per answer:
> the platform must then be started with longer timeouts, see [Running on a CPU](#running-on-a-cpu).
> You can also use a hosted LLM, see [Using OpenAI instead of Ollama](#using-openai-instead-of-ollama).

### On macOS

* Install the [Ollama application](https://ollama.com/download/mac), not a Docker image:
  Docker containers cannot use the GPU of the Mac.
* Docker can be provided by [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  or by [Colima](https://github.com/abiosoft/colima). With Colima:
    * the virtual machine only has 2 GB of memory by default, whereas the platform uses almost 3 GB:
      start it with more resources, for instance `colima start --cpu 4 --memory 6`
      (the new values are kept for the next starts);
    * the `docker compose` command needs the Docker Compose plugin: `brew install docker-compose`,
      then add its folder to `cliPluginsExtraDirs` in `~/.docker/config.json`, as explained at the end of the installation;
    * clone the repositories in your home folder: by default, Colima only shares this one with the virtual machine.
      Elsewhere (for instance in `/tmp`), the files mounted by Docker Compose are empty in the containers:
      neither the MongoDB replica set nor the PostgreSQL schema is initialized.

## Prepare the models

The bot uses two models:

* an **embedding model**, `mxbai-embed-large`, which turns texts into vectors to search the documentation,
* an **LLM**, `qwen2.5:7b`, which writes the answers.

```shell
ollama pull mxbai-embed-large
ollama pull qwen2.5:7b
```

The RAG prompt of Tock, with the retrieved documents, is longer than the default context window of Ollama
(4096 tokens): the prompt would be truncated, and the LLM would not answer in the expected JSON format.
Create a variant of the model with a larger context window:

```shell
printf 'FROM qwen2.5:7b\nPARAMETER num_ctx 16384\n' > Modelfile
ollama create qwen2.5-16k -f Modelfile
```

The Tock containers reach Ollama through the `host.docker.internal` address
(the Docker Compose files of Tock map it to the host, also on Linux).
On Linux, Ollama only listens on `127.0.0.1` by default: make it listen on all interfaces, for instance
when it runs as a systemd service:

```shell
sudo systemctl edit ollama
# add the following lines, then save:
#   [Service]
#   Environment="OLLAMA_HOST=0.0.0.0"
sudo systemctl restart ollama
```

> On macOS (Docker Desktop or Colima) and Windows, `host.docker.internal` reaches Ollama without any configuration:
> this step is not needed.

## Start the platform

The [Tock Docker](https://github.com/theopenconversationkit/tock-docker) repository provides a Docker Compose file
with everything needed for RAG: _Tock Studio_, _Bot API_, the Gen AI orchestrator, and a PostgreSQL database
with the PGVector extension.

```shell
git clone https://github.com/theopenconversationkit/tock-docker.git && cd tock-docker
chmod +x scripts/setup.sh
docker compose -f docker-compose-rag-pgvector.yml up -d
```

> The Tock version is set by the `TAG` variable of the `.env` file.

At its first start, the PostgreSQL database is initialized with the schema of the Gen AI orchestrator,
including the full-text search column used by the hybrid search.

> If the `pgvector-postgres-vl` volume was created by an earlier version of Tock Docker, this schema is missing
> and the hybrid search fails (the bot answers _"Technical error"_): remove the volume with
> `docker compose -f docker-compose-rag-pgvector.yml down -v` (this also deletes the Tock data), or apply
> `scripts/pgvector/init.sql` to the database.

After a minute or so, _Tock Studio_ is available at [http://localhost](http://localhost).
Log in with `admin@app.com` / `password`.

## Create the application

When you first log in, a wizard invites you to create an application:

* _Choose your language_: select _English_, then _Next_
* _Select a first Channel_: select _web_, then _Next_
* _Create your Assistant_: _Create_

The wizard creates an application named `new_assistant`, in the `app` namespace (the namespace of the default users
of the Docker platform). The documents are indexed for a given bot: the indexing tool needs this namespace
and this application name.

## Convert the documentation into a CSV file

The [indexing tool](../gen-ai/indexing.md) reads documents from a CSV file with the columns `title`, `source` and `text`.
The Tock documentation is a set of Markdown files, in the `docs/docs` folder of the Tock repository:
the following script turns each section of each page into a row of the CSV file, with the URL of the section
on [doc.tock.ai](https://doc.tock.ai/) as its source.

Get the documentation, next to the `tock-docker` folder, and create a working folder:

```shell
cd ..
git clone --depth 1 https://github.com/theopenconversationkit/tock.git
mkdir -p tock-rag && cd tock-rag
```

Create a `docs_to_csv.py` file in the `tock-rag` folder:

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

Run it on the English documentation. The CSV file is written where the indexing tool expects it:
`<base folder>/<namespace>-<application name>/input/`.

```shell
python3 docs_to_csv.py ../tock/docs/docs/en ingestion/app-new_assistant/input/tock-doc.csv
```

```
405 sections written to ingestion/app-new_assistant/input/tock-doc.csv
```

## Index the documentation

Create the configuration file of the indexing tool, `ingestion/config.json`:

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

* `bot`: the namespace and the application name of the bot. `file_location` is the folder containing the CSV files,
  as seen from the container.
* `em_setting`: the embedding model. The same model must be selected in the RAG settings of the bot.
* `chunk_size`: the maximum size of a chunk, in characters. `mxbai-embed-large` reads at most 512 tokens:
  larger chunks make the indexing fail with _"the input length exceeds the context length"_.
* `vector_store_setting`: the PostgreSQL database started by Docker Compose.
* `document_index_name`: leave it `null`, so that the index is named after the namespace, the bot and the indexing session,
  as _Tock Studio_ expects.
* `append_doc_title_and_chunk`: prepends the title (page and section) to each chunk, which improves the search.

Run the indexing tool, available as the `tock/llm-indexing-tools` Docker image, on the network of the platform.
The `-v` option displays the progress and the summary of the indexing:

```shell
docker run --rm \
  --network tock-docker_default \
  --add-host host.docker.internal:host-gateway \
  -v "$PWD/ingestion:/ingestion" \
  tock/llm-indexing-tools:{{ tock_version }} \
  python tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py \
  --json-config-file=/ingestion/config.json -v
```

> The network name is `<folder of the Docker Compose file>_default`: `tock-docker_default` if you cloned
> the repository with its default name. `docker network ls` lists the networks.

Embedding the whole documentation takes less than a minute with a GPU or an Apple Silicon Mac,
and a few minutes on a CPU. At the end, the tool displays a summary:

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

Write down the **Index session ID**: it tells the bot which documents to use.

## Configure the RAG

In _Tock Studio_, go to _Gen AI_ > _Rag settings_:

* **Question condensing** and **Question answering**, _Configuration_:
    * Provider: _Ollama_
    * BaseUrl: `http://host.docker.internal:11434`
    * Model: `qwen2.5-16k`
    * Temperature: `0` (a small local model follows the expected answer format more reliably)
* **Embedding**, _Configuration_:
    * Provider: _Ollama_
    * BaseUrl: `http://host.docker.internal:11434`
    * Model: `mxbai-embed-large`
* **Indexing session**:
    * Indexing session id: the **Index session ID** displayed by the indexing tool
    * Search type: _Hybrid search_ (combines the vector search with a keyword search)
* Enable **Rag activated** and **Dialogs debug**
* _Save_

Once saved, reload the page: the _Vector database index name_ field shows the name of the index created by the indexing tool.

> There is no need to configure the vector store in _Gen AI_ > _Vector DB settings_: by default, the
> orchestrator uses the PostgreSQL database of the platform (set by environment variables in the Docker Compose file).

## Test the bot

Go to _Test_ > _Test_ and ask a question about Tock, for instance:

* _Which vector stores are supported?_
* _How do I deploy Tock with Docker?_
* _How can I connect my bot to WhatsApp?_

The bot has no intent yet: every question is qualified as _unknown_ and handled by the RAG.
The answer comes with the links to the documentation sections it is based on, for instance:

> The vector stores supported by Tock include PGVector (PostgreSQL) and OpenSearch.
>
> Sources: _Vector store providers_

The bot can also be called from outside _Tock Studio_, with the web connector created by the wizard:

```shell
curl -X POST http://localhost:8080/io/app/new_assistant/web \
  -H 'Content-Type: application/json' \
  -d '{"query": "Which vector stores are supported?", "userId": "my-user", "locale": "en"}'
```

With _Dialogs debug_ enabled, _Analytics_ > _Dialogs_ shows, for each answer, the condensed question
and the documentation chunks sent to the LLM.

## Running on a CPU

Without a GPU, an answer of the 7B model takes two to three minutes, whereas the platform gives up after one minute
(the bot then answers _"Technical error"_ or _"technical error :( timeout"_). Extend the timeouts with a
`docker-compose-slow-llm.yml` file, in the `tock-docker` folder:

```yaml
services:
  bot_api:
    environment:
      # Timeout of the calls to the Gen AI orchestrator
      - tock_gen_ai_orchestrator_client_request_timeout_ms=600000
      # Delay before the bot closes the answer channel
      - tock_cleanup_delay_seconds=600
  admin_web:
    environment:
      # Timeout of the Test screen of Tock Studio
      - tock_bot_rest_client_request_timeout_ms=600000
```

Then restart the platform with both files:

```shell
docker compose -f docker-compose-rag-pgvector.yml -f docker-compose-slow-llm.yml up -d
```

Smaller models (3B) are faster, but they do not follow the JSON answer format required by the RAG prompt reliably.

## Using OpenAI instead of Ollama

To use OpenAI, choose the _OpenAI_ provider with your API key in the RAG settings, for instance with the `gpt-4o-mini` LLM
and the `text-embedding-3-small` embedding model (which accepts larger chunks, for instance `"chunk_size": 2000`), and replace the `em_setting` of the indexing configuration with:

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

The documentation must be indexed again with this model, and the new indexing session ID entered in the RAG settings:
the embedding model of the RAG settings must always be the one used for the indexing.

## Going further

* Handle the journeys that must not be left to the LLM with stories: [Create your first bot with Tock Studio](first-bot-studio.md).
* Improve the answers with the [RAG prompt](../gen-ai/rag-prompt.md) and the [_Rag prompt context_](../gen-ai/rag-prompt-context.md)
  (covered and excluded topics, business lexicon).
* Understand why a documentation section is (or is not) used with the [retrieval diagnostic](../gen-ai/vector-store-inspection.md).
* Measure the quality of the answers with [datasets and evaluations](../gen-ai/answers-quality.md).
* Trace the LLM calls with an [observability provider](../gen-ai/observability.md).
* Embed the bot in a web page with the [Web connector](../channels/web.md).
