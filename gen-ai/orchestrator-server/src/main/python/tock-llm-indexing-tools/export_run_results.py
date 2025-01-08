"""
Export a LangSmith or LangFuse dataset run results.
Usage:
        export_run_results_both.py [-v] <dataset_provider> <dataset_id_or_name> <session_or_run_ids>...
        export_run_results_both.py -h | --help
        export_run_results_both.py --version

Arguments:
    dataset_provider       specify either 'langfuse' or 'langsmith'
    dataset_id_or_name     dataset id if langsmith or name if langfuse
    session_or_run_ids     list of session or run ids

Options:
    -v          Verbose output
    -h --help   Show this screen
    --version   Show version


The exported CSV file will have these columns :
'Reference input'|'Reference output'|'Response 1'|'Sources 1'|...|'Response N'|'Sources N'
The CSV file will be saved in the same location as the script.
NB: There will be as many responses as run sessions

Note that you need to set the LANGFUSE_SECRET_KEY and LANGFUSE_PUBLIC_KEY environment variables in order to use Langfuse.
The LANGFUSE_SECRET_KEY and LANGFUSE_PUBLIC_KEY are the secret and public keys provided by Langfuse

And you need to set the LANGCHAIN_API_KEY to use Langsmith.
"""

import csv
import json
import logging
import os
import sys
import time

import requests
from docopt import docopt
from dotenv import load_dotenv

from generate_dataset import init_langfuse


# Function to create the CSV header, works for both providers
def create_csv_header(_runs_names, provider, dataset_id):
    """
    Create the CSV file header.
    This function handles both LangFuse and LangSmith providers.

    Args:
        _runs_names: List of run/session IDs.
        provider: The provider being used (either 'langfuse' or 'langsmith').
        dataset_id : dataset id for langsmith or dataset name for langfuse

    Returns:
        The CSV file header as a list.
    """
    header_topic = 'Thématique de la question'
    header_reference_input = 'Entrée de référence'
    header_reference_output = 'Sortie de référence'
    header_answer = 'Réponse'
    header_sources = 'Sources'

    csv_header = [header_topic, header_reference_input, header_reference_output]

    if provider == 'langfuse':
        counter = 1
        for run_name in _runs_names:
            csv_header.append(f"{header_answer} {counter} ({run_name})")
            csv_header.append(f"{header_sources} {counter} ({run_name})")
            counter += 1

    elif provider == 'langsmith':
        sessions_content = get_sessions(dataset_id)
        counter = 1
        for session_id in _runs_names:
            session_name = get_session_name(session_id, sessions_content)
            csv_header.append(f"{header_answer} {counter} ({session_name})")
            csv_header.append(f"{header_sources} {counter} ({session_name})")
            counter += 1

    return csv_header


# LangFuse-specific functions
def fetch_trace_by_item_and_dataset_run(dataset_run, item):
    """
    Fetches the trace for a dataset item from a LangFuse dataset run.

    Args:
        dataset_run: The dataset run with items.
        item: The dataset item.

    Returns:
        Trace data if found, otherwise None.
    """
    for item_run in dataset_run:
        if item.id == item_run.dataset_item_id:
            trace = client.get_trace(item_run.trace_id)
            return trace
    return None


def append_runs_langfuse(dataset_item, _runs_names):
    """
    Append LangFuse run data to the CSV.

    Args:
        dataset_item: The dataset item.
        _runs_names: List of run names.

    Returns:
        A list representing a line in the CSV.
    """
    csv_line = [
        dataset_item.metadata["topic"] if dataset_item.metadata else "",
        dataset_item.input["question"],
        dataset_item.expected_output["answer"]
    ]

    for _run_name in _runs_names:
        dataset_run = client.get_dataset_run(dataset_name=dataset_name, dataset_run_name=_run_name)
        trace = fetch_trace_by_item_and_dataset_run(dataset_run.dataset_run_items, dataset_item)
        if trace is None or trace.output is None or not isinstance(trace.output, dict):
            csv_line.append('')  # Empty if no trace is found
            csv_line.append('')  # Empty for sources as well
        else:
            csv_line.append(trace.output["answer"])  # Append the answer
            csv_line.append('\n\n'.join(
                [f'[{doc["metadata"]["title"]}]({doc["metadata"]["source"]}) : {doc["page_content"]}'
                 f'\n################################################################################'
                 f'################################################################################' for doc in
                 trace.output["source_documents"]]))

    return csv_line


# LangSmith-specific functions (restored)
def get_sessions(_dataset_id):
    """
    Fetches the dataset run sessions for LangSmith.

    Args:
        _dataset_id: The dataset ID.

    Returns:
        The sessions as a list.
    """
    logging.info(f'Call the LangSmith API to get run sessions for dataset_id={_dataset_id}.')
    sessions_response = requests.get(
        f'{base_url}/sessions?reference_dataset={_dataset_id}',
        headers={"x-api-key": _LANGSMITH_API_KEY},
    )
    if sessions_response.status_code == 200:
        sessions_content = json.loads(sessions_response.content)
        logging.debug(f"Number of example runs obtained = {len(sessions_content)}")
        return sessions_content
    else:
        logging.error(
            f"Failed to get example runs. \nHttp code: {sessions_response.status_code} \nContent: {sessions_response.content}")
        raise RuntimeError


def get_example_runs_content(example_id):
    """
    Fetch runs of an example
    Args:
        example_id: the example id

    Returns: the runs
    """

    logging.info(f'Call the LangSmith API to get dataset runs for the example_id={example_id}.')
    example_runs_response = requests.post(
        f'{base_url}/runs/query',
        json={
            "reference_example": [
                example_id
            ],
            "is_root": "true",
            "filter": "eq(is_root, true)",
            "select": [
                "status",
                # "inputs",
                "outputs",
                "end_time",
                "total_cost",
                # "extra",
                "feedback_stats",
                "error"
            ],
            "limit": 15
        },
        headers={"x-api-key": _LANGSMITH_API_KEY},
    )
    if example_runs_response.status_code == 200:
        example_runs_content = json.loads(example_runs_response.content)
        logging.debug(f"Number of example runs obtained = {len(example_runs_content)}")
        return example_runs_content
    else:
        logging.error(f"Failed to get example runs. \n"
                      f"Http code : {example_runs_response.status_code} \n"
                      f"Content   : {example_runs_response.content}")
        raise RuntimeError


# Restoring get_dataset_examples function for LangSmith
def get_dataset_examples(nb_example, dataset_id):
    """
    Fetch the dataset examples from LangSmith.

    Args:
        nb_example: The number of examples to fetch.
        dataset_id: The dataset ID to fetch examples for.

    Returns:
        The dataset examples.
    """
    examples = []
    counter = nb_example
    offset = 0
    limit = 100  # Less than or equal to 100

    while counter > 0:
        logging.info(f'Call the LangSmith API to get {limit} dataset examples, starting from {offset}.')
        dataset_examples_response = requests.get(
            f'{base_url}/examples?dataset={dataset_id}&offset={offset}&limit={limit}',
            headers={"x-api-key": _LANGSMITH_API_KEY},
        )

        if dataset_examples_response.status_code == 200:
            dataset_examples_content = json.loads(dataset_examples_response.content)
            examples.extend(dataset_examples_content)
            logging.debug(f"Number of examples obtained = {len(dataset_examples_content)}")
            logging.debug(f"Number of examples collected = {len(examples)}/{nb_example}")
        else:
            logging.error(
                f"Failed to get dataset examples. \nHttp code: {dataset_examples_response.status_code} \nContent: {dataset_examples_response.content}")
            raise RuntimeError

        counter -= limit
        offset += limit

    logging.info(f"Fetched dataset examples = {len(examples)}/{nb_example}")
    return examples


def append_runs_langsmith(dataset_example, _session_ids):
    """
    Append LangSmith run data to the CSV.

    Args:
        dataset_example: The dataset example.
        _session_ids: List of session IDs.

    Returns:
        A list representing a line in the CSV.
    """
    csv_line = [
        dataset_example["inputs"]["metadata"]["topic"],
        dataset_example["inputs"]["question"],
        dataset_example["outputs"]["answer"]
    ]

    example_runs_content = get_example_runs_content(dataset_example['id'])

    for _id in _session_ids:
        run = first_true(example_runs_content['runs'], lambda x: x['session_id'] == _id)
        if run is None or run["outputs"] is None:
            csv_line.append('')
            csv_line.append('')
        elif run["error"]:
            csv_line.append(run["error"])
            csv_line.append('')
        else:
            csv_line.append(run["outputs"]["answer"])
            csv_line.append(','.join([doc["metadata"]["url"] for doc in run["outputs"]["source_documents"]]))

    return csv_line


def get_session_name(_id: str, sessions) -> str:
    """
    Get session name.
    Args:
        _id: the session id
        sessions: the sessions

    Returns: the session name
    """

    return first_true(sessions, predicate=lambda x: x['id'] == _id)['name']


def get_example_runs_content(example_id):
    """
    Fetch runs of an example
    Args:
        example_id: the example id

    Returns: the runs
    """

    logging.info(f'Call the LangSmith API to get dataset runs for the example_id={example_id}.')
    example_runs_response = requests.post(
        f'{base_url}/runs/query',
        json={
            "reference_example": [
                example_id
            ],
            "is_root": "true",
            "filter": "eq(is_root, true)",
            "select": [
                "status",
                # "input",
                "outputs",
                "end_time",
                "total_cost",
                # "extra",
                "feedback_stats",
                "error"
            ],
            "limit": 15
        },
        headers={"x-api-key": _LANGSMITH_API_KEY},
    )
    if example_runs_response.status_code == 200:
        example_runs_content = json.loads(example_runs_response.content)
        logging.debug(f"Number of example runs obtained = {len(example_runs_content)}")
        return example_runs_content
    else:
        logging.error(f"Failed to get example runs. \n"
                      f"Http code : {example_runs_response.status_code} \n"
                      f"Content   : {example_runs_response.content}")
        raise RuntimeError


def first_true(iterable, predicate=None):
    """
    Returns the first element in the iterable that satisfies the predicate.

    Args:
        iterable: The iterable to search.
        predicate: The condition to check for.

    Returns:
        The first element satisfying the predicate.
    """
    return next(filter(predicate, iterable), None)


# Check for environment variables from LangFuse and LangSmith
def check_environment_variables(provider):
    """
    Checks the required environment variables based on the provider.

    Args:
        provider: The provider being used ('langfuse' or 'langsmith').
    """
    if provider == 'langfuse':
        if not os.getenv('LANGFUSE_SECRET_KEY'):
            logging.error('Cannot proceed: LANGFUSE_SECRET_KEY is not defined.')
            sys.exit(1)
        if not os.getenv('LANGFUSE_HOST'):
            logging.error('Cannot proceed: LANGFUSE_HOST is not defined.')
            sys.exit(1)
    elif provider == 'langsmith':
        if not os.getenv('LANGCHAIN_API_KEY'):
            logging.error('Cannot proceed: LANGCHAIN_API_KEY is not defined.')
            sys.exit(1)


if __name__ == '__main__':
    start_time = time.time()
    load_dotenv()  # Load environment variables from .env file

    cli_args = docopt(__doc__, version='Export Run Results 0.1.0')

    provider = cli_args['<dataset_provider>']
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(level=logging.DEBUG if cli_args['-v'] else logging.INFO, format=log_format)

    check_environment_variables(provider)  # Check environment variables based on provider

    csv_lines = []
    if provider == 'langfuse':
        dataset_name = cli_args['<dataset_id_or_name>']
        runs_names = cli_args['<session_or_run_ids>']
        client = init_langfuse()
        dataset = client.get_dataset(name=dataset_name)
        csv_lines = [create_csv_header(runs_names, provider, dataset_name)]
        for item in dataset.items:
            csv_lines.append(append_runs_langfuse(item, runs_names))

    elif provider == 'langsmith':
        # The LangSmith API base url
        base_url = 'https://api.smith.langchain.com/api/v1'
        # Get LangSmith API key from environment
        _LANGSMITH_API_KEY = os.environ["LANGCHAIN_API_KEY"]
        dataset_id = cli_args['<dataset_id_or_name>']
        session_ids = cli_args['<session_or_run_ids>']
        dataset_info = get_sessions(dataset_id)
        examples = get_dataset_examples(len(dataset_info), dataset_id)
        csv_lines = [create_csv_header(session_ids, provider, dataset_id)]
        for example in examples:
            csv_lines.append(append_runs_langsmith(example, session_ids))

    output_csv_file = f"export_run_result_{provider}_{int(time.time())}.csv"
    with open(output_csv_file, 'w', newline='') as csv_file:
        writer = csv.writer(csv_file, delimiter='|')
        writer.writerows(csv_lines)

    logging.info(f"CSV file successfully generated: {output_csv_file}")
    logging.info(f"Total execution time: {time.time() - start_time:.2f} seconds")
