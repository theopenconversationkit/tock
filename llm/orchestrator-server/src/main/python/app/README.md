<a name="readme-top"></a>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

LLM Orchestrator is the server that handle all LLMs operations : Retrieval Augmented Generation, synthetic sentences generatio. This server is called by Bot API RAG story.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


* [![Python][Python]][Python-url]
* [![FastApi][FastApi]][FastApi-url]
* [![Azure][Azure]][Azure-url]
* [![AWS][AWS]][AWS-url]
* [![LangChain][LangChain]][LangChain-url]
* [![OpenAI][OpenAI]][OpenAI-url]
* [![AzureOpenAI][AzureOpenAI]][AzureOpenAI-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- GETTING STARTED -->
## Getting Started with Tock LLM Orchestrator

### Prerequisites

#### Python & Peotry

##### Using pyenv && poetry (recommended)
  It's recommended to use pyenv, see install and usage guide here :
  https://gist.github.com/trongnghia203/9cc8157acb1a9faad2de95c3175aa875

  Basic usage to create a venv with a specific version of Python for this project :
```sh
# In llm/orchestrator-server/src/main/python/app
pyenv install 3.9.18
pyenv local 3.9.18  # Activate Python 3.9 for the current
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
  apt install python3.9 poetry
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
gunicorn llm_orchestrator.main:app --reload --bind :8000 --workers=2 --worker-class=uvicorn.workers.UvicornWorker --log-config=./src/llm_orchestrator/configurations/logging/config.ini
```

### Dev

See the installation guide, in your virtual env install extra dependencies and pre-commit hooks :
```bash
poetry install --with dev
pre-commit install
uvicorn llm_orchestrator.main:app --host 0.0.0.0 --port 8000 --reload --log-level trace
```

<!-- USAGE EXAMPLES -->
## Usage

Uvicorn Fast API : Go to
   ```sh
    http://localhost:8000/
   ```

_For more information, please refer to the [Swagger](http://localhost:8000/docs)_

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
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
