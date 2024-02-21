# TOCK indexing tools

A collection of tools to ingest data into a Vector DB.

## Installing toolset

Install tools using Poetry from package directory base:

`poetry install`

Then run the scripts by passing them to a Python interpreter (>= 3.9):

`python <script> <args>`

## Data processing

### smarttribune_formatter.py

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

Turns a Smart Tribune CSV export file into a ready-to-index CSV file (one 'title'|'url'|'text' line per filtered entry):


| Title      | URL                | Text                  |
| ------------ | -------------------- | ----------------------- |
| Some title | http://example.com | This is example text. |
| ...        | ...                | ...                   |

### webscraper.py

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

Recursively browse web URLs (follow links from these base URLs), then scrape links' contents based on a list of BeautifulSoup filters, then export these contents into a ready-to-index CSV file (one 'title'|'url'|'text' line per URL with scraped contents):


| Title      | URL                | Text                  |
| ------------ | -------------------- | ----------------------- |
| Some title | http://example.com | This is example text. |
| ...        | ...                | ...                   |

## Documents indexing

### index_documents.py

Index a ready-to-index CSV file ('title'|'url'|'text' lines) file contents into an OpenSearch vector database.

```
Usage:
    index_documents.py [-v] <input_csv> <index_name> <embeddings_cfg> <chunks_size> [<env_file>]
    index_documents.py -h | --help
    index_documents.py --version

Arguments:
    input_csv       path to the ready-to-index file
    index_name      name of the OpenSearch index (shall follow indexes naming rules)
    embeddings_cfg  path to an embeddings configuration file (JSON format) (shall describe settings for one of OpenAI or AzureOpenAI embeddings model)
    chunks_size     size of the embedded chunks of documents

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will be silent but for errors and the unique indexing session id)
```

Index a ready-to-index CSV file contents into an OpenSearch vector database.

CSV columns are 'title'|'url'|'text'. 'text' will be chunked according to chunks_size, and embedded using configuration described in embeddings_cfg (it uses the embeddings constructor from the orchestrator module, so JSON file shall follow corresponding format).

Documents will be indexed in OpenSearch DB under index_name index (index_name shall follow OpenSearch naming restrictions) with the following metadata:


| Metadata tag     | Description                                                      |
| ------------------ | ------------------------------------------------------------------ |
| index_session_id | a uuid for the indexing session (running this script)            |
| index_datetime   | the date of the indexing session                                 |
| id               | a uuid for each document (one per line in the input file)        |
| chunk            | the nb of the chunk if the original document was splitted: 'n/N' |
| title            | the 'title' column from original input CSV                       |
| url              | the 'url' column from original input CSV                         |

A unique indexing session id is produced and printed to the console (will be the last line printed if the '-v' option is used).
