<a name="readme-top"></a>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started-with-gen-ai-orchestrator">Getting Started with Gen AI Orchestrator</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#dev">Dev</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#prompts-tooling">Prompts tooling</a></li>
  </ol>
</details>

## About The Project

Gen AI Orchestrator is the server that handle all LLMs operations : Retrieval Augmented Generation, synthetic sentences generation. This server is called by Bot API RAG story.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

* [![Python]][Python-url]
* [![FastApi]][FastApi-url]
* [![Azure]][Azure-url]
* [![AWS]][AWS-url]
* [![LangChain]][LangChain-url]
* [![OpenAI]][OpenAI-url]
* [![AzureOpenAI]][AzureOpenAI-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started with Gen AI Orchestrator

### Prerequisites

#### Python & Peotry

##### Using pyenv && poetry (recommended)

It's recommended to use pyenv, see install and usage guide here :
https://gist.github.com/trongnghia203/9cc8157acb1a9faad2de95c3175aa875

Basic usage to create a venv with a specific version of Python for this project :

```sh
# In gen-ai/orchestrator-server/src/main/python/server
pyenv install 3.10.6
pyenv local 3.10.6  # Activate Python 3.9 for the current
which python # Check that you use the python version installed by pyenv
python --version # Check your python version
python -m venv .venv # Create a virtual env based on this python version
source .venv/bin/activate # Activate your virtual env
poetry install # Install dependencies for this project in the virtual env
```

##### Using apt (not recommended version may vary)

*Skip this part if you have followed the install using pyenv*

Install python3.9 and poetry :

```sh
apt install python3.10 poetry
```

Create a virtual env then install dependencies :

```bash
python3.9 -m venv .venv
source .venv/bin/activate # Activate your virtual env
poetry install # Install dependencies for this project in the virtual env
```

#### Install dependencies

```bash
poetry install # Install dependencies for this project in the virtual env
```

#### Open Search vector DB

To run Open Search vector database see [tock-docker](https://github.com/theopenconversationkit/tock-docker)
([see our fork](https://github.com/CreditMutuelArkea/tock-docker/blob/feature/rag/develop/docker-compose-opensearch-only.yml) until it's merge to the upstream)

Start the Open Search cluster using :

```
docker compose -f docker-compose-opensearch-only.yml up -d
```

#### Start the API

```bash
uvicorn gen_ai_orchestrator.main:app --reload --host 0.0.0.0 --port 8000 --log-config=./src/gen_ai_orchestrator/configurations/logging/config.ini
```

### Dev

See the installation guide, in your virtual env install extra dependencies and pre-commit hooks :

```bash
poetry install --with dev
pre-commit install
uvicorn gen_ai_orchestrator.main:app --reload --host 0.0.0.0 --port 8000 --log-config=./src/gen_ai_orchestrator/configurations/logging/config.ini
```

#### Unit tests

When dev dependencies are installed, run unit tests for the orchestrator using Tox:

```
tox run
```

This will also produce a Coverage.py code coverage report in coverage.xml.

### Dependencies analysis

#### Using pip audit

Dev extra dependencies contain the pip-audit package dependencies vulnerability analysis tool (see [pip-audit](https://pypi.org/project/pip-audit)).

Corresponding pre-commit hook will fail if poetry.lock contains any vulnerable dependencies.

To run pip-audit manually, setup in your Poetry env with dev dependencies installed, then:

```bash
pip-audit
```

If you happen to use multiple .pre-commit.yaml files (like by developing in both the 'app' and 'tock-llm-indexing-tools' projects), you may encounter a "No <some path>/.pre-commit-config.yaml file was found".
If so, edit the .git/hooks/pre-commit file (this is the default pre-commit installation path, yours may be elsewhere) and correct <some path> in the ARGS to point to the correct config files.

#### Generate SBOM file using cyclonedx-bom for Dependency Track

If you use [Dependency Track](https://dependencytrack.org/) for dependencies vulnerability management, follow those instruction to generate the SBOM file :
```bash
cyclonedx-py poetry . --no-dev -o tock-gen-ai-orchestrator-sbom-$(git rev-parse --short HEAD).json
```

## Usage

Uvicorn Fast API: go to

```sh
 http://localhost:8000/
```

*For more information, please refer to the [Swagger](http://localhost:8000/docs)*

## Prompts tooling

When a bot is configured to use the Gen AI Orchestrator (through the RAG/RAG settings screen in the Studio), a default prompt is proposed to the bot admin, to carry the user query to the LLM inference point.
Bot admins can customize this prompt to their specific conversational needs.

The size of this prompt (in tokens) can strongly impact your RAG system's performance if it is too long. *tiktoken* is referenced as a project dependency, to provide a convenience tokens counter tool for this prompt.

Once the project's dependencies are installed in a venv, the prompt tokens can be counted using the corresponding Python interpreter (see [here](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb) for more details):

```python
prompt = "my new prompt contents" # put your prompt's text there
encoding = tiktoken.encoding_for_model("gpt-4") # change according to RAG settings
num_tokens = len(encoding.encode(prompt))
print(f"Nb of tokens: {num_tokens}")
```

If your prompt contents can be made public, you can also use [OpenAI's tokenizer](https://platform.openai.com/tokenizer) as a more convenient method.

## RAG - Reranking (Compressor), Guardrail and embedding - Bloomz based models

The Gen AI Orchestrator server RAG chain supports embedding, reranking and guardrails for Bloomz based models.
Curently this implementation is based on a **custom inference server also Open Sourced at https://github.com/creditMutuelArkea/llm-inference/** 

### Embedding configuration

See [embeddings_bloomz_settings.json](./../tock-llm-indexing-tools/examples/embeddings_bloomz_settings.json) for an exemple of configuration.

### Reranking / Compressor settings

Compressor (aka reranker) will compute a similarity score between the user query and a document (context),
usually this score is linked to an output of the reranking model associated to a specific label. We may find the name of 
the label in the model card, for instance LABEL_1 contains this similarity score for *cmarkea/bloomz-3b-reranking* model [mentioned in the model
card here](https://huggingface.co/cmarkea/bloomz-3b-reranking#:~:text=lambda%20x%3A%20x%5B0%5D%5B%27label%27%5D%20%3D%3D%20%22LABEL_1%22%2C).

You can also specify a `min_score`, used to filter non relevant / similar documents.

Here is an exemple of compressor_settings that uses our llm-inference with *cmarkea/bloomz-3b-reranking* model :
```json
{
    "compressor_setting": {
      "endpoint": "http://localhost:8082",
      "min_score": 0.7,
      "label": "LABEL_1",
      "provider": "BloomzRerank"
  }
}
``` 

### Guardrail settings

Our guardrail models for instance [cmarkea/bloomz-560m-guardrail](https://huggingface.co/cmarkea/bloomz-560m-guardrail)
(see our organisation for other variants), returns the following scores thought llm-inference serveur :
```json
{
    "response": [
        [
            {
                "label": "insult",
                "score": 0.7866228222846985
            },
            {
                "label": "obscene",
                "score": 0.4258439540863037
            },
            {
                "label": "sexual_explicit",
                "score": 0.1550784707069397
            },
            {
                "label": "identity_attack",
                "score": 0.05749328061938286
            },
            {
                "label": "threat",
                "score": 0.022629201412200928
            }
        ]
    ]
}
```

When adding the guardrail setting to the RAG chain if any of the following evaluated toxicity score is higher that the
setting `max_score` the response will be rejected. Here is an exemple of guardrail setting :
```json
{
  "guardrail_setting": {
    "api_base": "http://localhost:8083",
    "provider": "BloomzGuardrail",
    "max_score": 0.5
  }
}
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[product-screenshot]: images/screenshot.png
[Python]: https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54
[Python-url]: https://www.langchain.com/
[FastApi]: https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=FastAPI&logoColor=white
[FastApi-url]: https://fastapi.tiangolo.com/
[LangChain]: https://img.shields.io/badge/LangChain-LIB-blue
[LangChain-url]: https://www.langchain.com/
[OpenAI]: https://img.shields.io/badge/OpenAI-LLM-blue
[OpenAI-url]: https://openai.com/
[AzureOpenAI]: https://img.shields.io/badge/AzureOpenAI-LLM-blue
[AzureOpenAI-url]: https://azure.microsoft.com/fr-fr/products/ai-services/openai-service
[OpenSearch]: https://img.shields.io/badge/OpenSearch-AWS-blue
[OpenSearch-url]: https://opensearch.org/
[Azure]: https://img.shields.io/badge/azure-%230072C6.svg?style=for-the-badge&logo=microsoftazure&logoColor=white
[Azure-url]: https://azure.microsoft.com/
[AWS]: https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white
[AWS-url]: https://aws.amazon.com/
