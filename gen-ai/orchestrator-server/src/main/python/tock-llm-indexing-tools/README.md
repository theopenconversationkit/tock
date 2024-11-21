# TOCK indexing and testing tools

A collection of tools to:

- ingest data into a Vector DB
- test specific RAG settings against a dataset.

## Installing toolset

Install tools using Poetry from package directory base:

`poetry install --no-root`

Install pre-commit for format code before commit:

`pre-commit install`

Then run the scripts by passing them to a Python interpreter (>= 3.9):

`python <script> <args>`

## Data ingestion

![Data processing and indexing tools](docs/processing_and_indexing.png "Data processing and indexing tools")

### Data processing

#### smarttribune_formatter.py

```
Smart Tribune export file formatter.

Usage:
smarttribune_formatter.py [-v] <input_csv> <tag_title> <base_url> <output_csv>

Arguments:
input_csv   path to the Smart Tribune CSV export file
tag_title   tag title to discrimate FAQ source ('Tag (ID system)' column will be filtered for lines containing this tag)
base_url    the base URL to prefix every FAQ entry's query parameter to create a full URL
output_csv  path to the output, ready-to-index CSV file

Options:
-h --help   Show this screen
--version   Show version
-v          Verbose output for debugging (without this option, script will be silent but for errors)
```

Turns a Smart Tribune CSV export file into a ready-to-index CSV file (one 'title'|'source'|'text' line per filtered entry):


| title      | source                | text                  |
|------------|--------------------|-----------------------|
| Some title | http://example.com | This is example text. |
| ...        | ...                | ...                   |


### smarttribune_consumer.py

```
Smart Tribune import data and formatter for send in opensearch.

Usage:
    smarttribune_consumer.py [-v]  <knowledge_base>  <base_url> <output_csv> [options]

Arguments:
    knowledge_base  name of the target knowledge base, ex: "name1 | name2 | name3"
    base_url    the base URL to prefix every FAQ entry's query parameter to
                create a full URL
    output_csv  path to the output, ready-to-index CSV file

Options:
    --tag_title=<value>
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Import and Format a Smart Tribune data by API  into a ready-to-index CSV file
(one 'title'|'source'|'text' line per filtered entry).
```
Set in a .env your APIKEY and your APISECRET

Import data from smart tribune API and return a ready-to-index CSV file (one 'title'|'source'|'text' line per filtered entry):


| title      | source                | text                  |
|------------|--------------------|-----------------------|
| Some title | http://example.com | This is example text. |
| ...        | ...                | ...                   |


#### webscraper.py

```
Simple recursive webscraper based on a list of BeautifulSoup filters.

Usage:
webscraping.py [-v] <input_urls> <soup_filters> <output_csv>
webscraping.py -h | --help
webscraping.py --version

Arguments:
input_urls      a comma-separated list of base URLs the scraper will browse recursively to find scrapable contents
soup_filters    a comma-separated list of filters to get pages contents (texts to be indexed will be concatenated in this order)
                Example: id='notes',class_='container',id='test'
output_csv      path to the output, ready-to-index CSV file (this file will be created at execution time, along with a <base URL netloc>/ sub-dir in the same directory,    containing debug info)

Options:
-h --help   Show this screen
--version   Show version
-v          Verbose output for debugging (without this option, script will be silent but for errors)
```

Recursively browse web URLs (follow links from these base URLs), then scrape links' contents based on a list of BeautifulSoup filters, then export these contents into a ready-to-index CSV file (one 'title'|'source'|'text' line per URL with scraped contents):


| Title      | URL                | Text                  |
|------------|--------------------|-----------------------|
| Some title | http://example.com | This is example text. |
| ...        | ...                | ...                   |

### Documents indexing

#### index_documents.py

```
Index a CSV file (line format: 'title'|'source'|'text') into a vector database.

Usage:
  index_documents.py --input-csv=<path> --namespace=<ns> --bot-id=<id> \
                     --embeddings-json-config=<emb_cfg> --vector-store-json-config=<vs_cfg> \
                     --chunks-size=<size> [--ignore-source=<is>] [--embedding-bulk-size=<em_bs>] \
                     [--env-file=<env>] [-v]
  index_documents.py (-h | --help)
  index_documents.py --version

Options:
  -h --help                           Show this help message.
  --version                           Show the version.
  --input-csv=<path>                  Path to the CSV file to be indexed.
  --namespace=<ns>                    TOCK bot namespace to which the index belongs.
  --bot-id=<id>                       TOCK bot ID to which the index belongs.
  --embeddings-json-config=<emb_cfg>  Path to embeddings configuration JSON file.
                                       (Describes settings for embeddings models supported by TOCK.)
  --vector-store-json-config=<vs_cfg> Path to vector store configuration JSON file.
                                       (Describes settings for vector stores supported by TOCK.)
  --chunks-size=<size>                Size of the embedded document chunks.
  --ignore-source=<is>                Ignore source validation. Useful if sources aren't valid URLs.
                                       [default: false]
  --embedding-bulk-size=<em_bs>       Number of chunks sent in each embedding request.
                                       [default: 100]
  --env-file=<env>                    Path to an optional environment configuration file.
  -v                                  Verbose output for debugging.

Description:
  This script indexes the contents of a CSV file into a vector database.
  The CSV must contain 'title', 'source', and 'text' columns. The 'text' will be chunked
  according to the specified chunk size and embedded using settings described in the
  embeddings JSON configuration file. Documents will then be indexed into a vector store
  using the vector store JSON configuration.

  The index name is automatically generated based on the namespace, bot ID, and a unique identifier
  (UUID). For example, in OpenSearch: ns-{namespace}-bot-{bot_id}-session-{uuid4}.
  Indexing details will be displayed on the console at the end of the operation,
  and saved in a specific log file in ./logs
```
CSV columns are 'title'|'source'|'text'. 

'text' will be chunked according to chunks_size, and embedded using configuration described in embeddings_cfg (it uses the embeddings constructor from the orchestrator module, so JSON file shall follow corresponding format - See [Embedding Settings](../server/src/gen_ai_orchestrator/models/em/em_types.py)).

Documents will be indexed in OpenSearch DB under index_name index (index_name shall follow OpenSearch naming restrictions) with the following metadata:


| Metadata tag     | Description                                                                                   |
|------------------|-----------------------------------------------------------------------------------------------|
| index_session_id | a uuid for the indexing session (running this script)                                         |
| index_datetime   | the date of the indexing session                                                              |
| id               | a uuid for each document (one per line in the input file)                                     |
| chunk            | the nb of the chunk if the original document was splitted: 'n/N'                              |
| title            | the 'title' column from original input CSV                                                    |
| source           | the 'source' column from original input CSV. This can be ignored using --ignore-source option |
| reference        | the document 'reference' that save the 'source' column whether the source is ignored or not   |

#### Minimal invocation
  index_documents.py --input-csv=data.csv --namespace=my_namespace \
    --bot-id=1234 --embeddings-json-config=embeddings.json \
    --vector-store-json-config=vector_store.json --chunks-size=50

#### Include optional parameters
  index_documents.py --input-csv=data.csv --namespace=my_namespace \
    --bot-id=1234 --embeddings-json-config=embeddings.json \
    --vector-store-json-config=vector_store.json --chunks-size=50 \
    --ignore-source=true --embedding-bulk-size=200 --env-file=.env -v

#### Sample result: 
<pre>
--------------------------------------- Indexing details --------------------------------------

Index name          : ns-mystore-bot-istore-session-2cd0cdeb-bb02-4c4e-90cd-76b42371b1c2
Index session ID    : 2cd0cdeb-bb02-4c4e-90cd-76b42371b1c2
Documents extracted : 4094 (Docs)
Documents chunked   : 12158 (Chunks)
Chunk size          : 1000 (Characters)
Input csv           : 0-bot-istore/full-extract-istore.csv
Embeddings          : AzureOpenAIService
Vector Store        : OpenSearch
Ignoring sources    : False
Duration            : 11 minutes and 45.52 seconds
Date                : 2024-11-21 14:14:42

-----------------------------------------------------------------------------------------------
</pre>

#### Logs
Each time a script is executed, one or more log files are generated and time-stamped in this format: 
`logs/index_documents_%Y%m%d_%H%M%S.log`

## Default Vector Store Configuration

To configure the default vector store, you can use the following environment variables:

| Variables                                                        | Description                                                       | Default                       | List of values                              |
|------------------------------------------------------------------|-------------------------------------------------------------------|-------------------------------|---------------------------------------------|
| `tock_gen_ai_orchestrator_application_environment`               | Application environment                                           | `DEV`                         | `DEV`, `PROD`                               |
| `tock_gen_ai_orchestrator_em_provider_timeout`                   | Embeddings request timeout (in seconds)                           | 120                           | Integer                                     |
| `tock_gen_ai_orchestrator_vector_store_provider`                 | Vector Store Provider                                             | `OpenSearch`                  | `OpenSearch`, `PGVector`                    |
| `tock_gen_ai_orchestrator_vector_store_host`                     | Vector Store host                                                 | 'localhost'                   | String                                      |
| `tock_gen_ai_orchestrator_vector_store_port`                     | Vector Store port                                                 | '9200'                        | Integer                                     |
| `tock_gen_ai_orchestrator_vector_store_user`                     | Vector Store user                                                 | 'admin'                       | String                                      |
| `tock_gen_ai_orchestrator_vector_store_pwd`                      | Vector Store password                                             | 'admin'                       | String                                      |
| `tock_gen_ai_orchestrator_vector_store_database`                 | Vector Store database name (Only if necessary. Example: PGVector) | Null                          | String                                      |
| `tock_gen_ai_orchestrator_vector_store_test_query`               | Query used to test the Vector Store                               | 'What knowledge do you have?' | String                                      |
| `tock_gen_ai_orchestrator_vector_store_secret_manager_provider`  | Secret Manager Provider                                           | `AWS_SECRETS_MANAGER`         | `AWS_SECRETS_MANAGER`, `GCP_SECRET_MANAGER` |
| `tock_gen_ai_orchestrator_vector_store_credentials_secret_name`  | Secret name storing credentials                                   | Null                          | String                                      |


## Testing RAG settings on dataset

![RAG settings testing tools](docs/rag_testing_tools.png "RAG settings testing tools")

### generate_dataset.py

Generates a testing dataset based on an input file. The input file should have the correct format (see generate_datset_input.xlsx for sample). The generated dataset can be saved on filesystem, using the --csv-output option, on langsmith, using the --langsmith-dataset-name option, or both.

```
Usage:
    generate_dataset.py [-v] <input_excel> --range=<s> [--csv-output=<path>] [ --langsmith-dataset-name=<name> ] [--locale=<locale>] [--no-answer=<na>]
    generate_dataset.py [-v] <input_excel> --sheet=<n>... [--csv-output=<path>] [ --langsmith-dataset-name=<name> ] [--locale=<locale>] [--no-answer=<na>]

Arguments:
    input_excel path to the input excel file

Options:
    --range=<s>                     Range of sheet to be parsed. The expected format is X,Y where X is the first sheet to be included, and Y is the last. Indices are 0-indexed.
    --sheet=<n>                     Sheet numbers to be parsed. Indices are 0-indexed.
    --csv-output=<path>             Output path of csv file to be generated.
    --langsmith-dataset-name=<name> Name of the dataset to be saved on langsmith.
    --locale=<locale>               Locale to be included in de dataset. [default: French]
    --no-answer=<na>                Label of no_answer to be included in the dataset. [default: NO_RAG_SENTENCE]
    -h --help                       Show this screen
    --version                       Show version
    -v                              Verbose output for debugging (without this option, script will be silent but for errors)

Generates a testing dataset based on an input file. The input file should have the correct format (see generate_datset_input.xlsx for sample). The generated dataset can be saved on filesystem, using the --csv-output option, on langsmith, using the --langsmith-dataset-name option, or both.
```

### rag_testing_tool.py

Retrieval-Augmented Generation (RAG) endpoint settings testing tool based on LangSmith's SDK: runs a specific RAG Settings configuration against a reference dataset.

```
Usage:
    rag_testing_tool.py [-v] <rag_query> <dataset_name> <test_name> [<delay>]
    rag_testing_tool.py -h | --help
    rag_testing_tool.py --version

Arguments:
    rag_query       path to a JSON 'RAGQuery' JSON file containing RAG settings
                    to be tested: llm model, embedding model, vector database
                    provider, indexation session's unique id, and 'k', i.e. nb
                    of retrieved docs (question and chat history are ignored,
                    as they will come from the dataset)
    dataset_name    the reference dataset name
    test_name       name of the test run

Options:
    delay       Delay between two calls to the inference method in ms
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)
```

Build a RAG (Lang)chain from the RAG Query and runs it against the provided LangSmith dataset. The chain is created anew for each entry of the dataset, and if a delay is provided each chain creation will be delayed accordingly.
### export_run_results.py

Export a LangSmith dataset run results, in csv format.

```
Usage:
    export_run_results.py [-v] <dataset_id> <session_ids>...
    export_run_results.py -h | --help
    export_run_results.py --version

Arguments:
    dataset_id      dataset id
    session_ids     list of session ids

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging

The exported CSV file will have these columns :
'Reference input'|'Reference output'|'Response 1'|'Sources 1'|...|'Response N'|'Sources N'
NB: There will be as many responses as run sessions
```
