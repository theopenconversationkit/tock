"""Export a LangSmith dataset run results.

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
"""
import csv
import json
import logging
import os
import time

import requests
from docopt import docopt
from dotenv import load_dotenv


def first_true(iterable, predicate=None):
    """
    Returns the first element of the iterator that satisfies the given predicate
    Args:
        iterable: the iterator
        predicate: the predicate

    Returns: element of the iterator
    """
    return next(filter(predicate, iterable), None)


def get_session_name(_id: str, sessions) -> str:
    """
    Get session name.
    Args:
        _id: the session id
        sessions: the sessions

    Returns: the session name
    """
    return first_true(sessions, predicate=lambda x: x['id'] == _id)['name']


def create_csv_header(_session_ids: list[str]):
    """
    Create the CSV file header
    Args:
        _session_ids: the session ids

    Returns: the CSV file header
    """

    # CSV Headers
    header_topic = 'Thématique de la question'
    header_reference_input = 'Entrée de référence'
    header_reference_output = 'Sortie de référence'
    header_answer = 'Réponse'
    header_sources = 'Sources'

    # Init csv header
    csv_header = [header_topic, header_reference_input, header_reference_output]

    # Get sessions info
    sessions_content = get_sessions(dataset_id)

    # Complete csv header with sessions name
    counter = 1
    for session_id in _session_ids:
        session_name = get_session_name(session_id, sessions_content)
        csv_header.append(f"{header_answer} {counter} ({session_name})")
        csv_header.append(f"{header_sources} {counter} ({session_name})")
        counter += 1

    return csv_header


def get_sessions(_dataset_id):
    """
    Fetch the dataset run sessions
    Args:
        _dataset_id: the dataset id

    Returns: the dataset run sessions
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
        logging.error(f"Failed to get example runs. \n"
                      f"Http code : {sessions_response.status_code} \n"
                      f"Content   : {sessions_response.content}")
        raise RuntimeError


def get_dataset_examples(nb_example):
    """
    Fetch the dataset examples
    Args:
        nb_example: number of examples to fetch

    Returns: the dataset examples
    """

    examples = []
    counter = nb_example
    offset = 0
    limit = 100  # less_than_equal should be less than or equal to 100

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
            logging.error(f"Failed to get dataset examples. \n"
                          f"Http code : {dataset_examples_response.status_code} \n"
                          f"Content   : {dataset_examples_response.content}")
            raise RuntimeError

        counter -= limit
        offset += limit

    logging.info(f"Fetched dataset examples = {len(examples)}/{nb_example} -> {len(examples) / nb_example * 100:.2f}%")
    return examples


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


def append_example_runs(dataset_example, _session_ids):
    """
    Append in a CSV line, the fetched runs for the given dataset example
    Args:
        dataset_example: the dataset example
        _session_ids: the session ids

    Returns: The CSV line
    """

    # Init csv line
    csv_line = [
        dataset_example["inputs"]["metadata"]["topic"],
        dataset_example["inputs"]["question"],
        dataset_example["outputs"]["answer"]
    ]

    # Get example runs
    example_runs_content = get_example_runs_content(dataset_example['id'])

    # Complete csv line with example run result
    for _id in _session_ids:
        run = first_true(example_runs_content['runs'], predicate=lambda x: x['session_id'] == _id)
        if run is None or run["outputs"] is None:
            csv_line.append('')
            csv_line.append('')
        elif run["error"]:
            csv_line.append(run["error"])
            csv_line.append('')
        else:
            csv_line.append(run["outputs"]["answer"])
            csv_line.append(
                ','.join([doc["metadata"]["url"] for doc in run["outputs"]["source_documents"]])
            )

    return csv_line


if __name__ == '__main__':
    start_time = time.time()
    load_dotenv()
    cli_args = docopt(__doc__, version='Webscraper 0.1.0')
    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.INFO, format=log_format
    )
    # Get LangSmith API key from environment
    _LANGSMITH_API_KEY = os.environ["LANGCHAIN_API_KEY"]
    # The LangSmith API base url
    base_url = 'https://api.smith.langchain.com/api/v1'
    # The script arguments
    dataset_id = cli_args['<dataset_id>']
    session_ids = cli_args['<session_ids>']

    try:
        logging.info(f'Call the LangSmith API to get the dataset information for dataset_id={dataset_id}.')
        dataset_info_response = requests.get(
            f'{base_url}/datasets?id={dataset_id}',
            headers={"x-api-key": _LANGSMITH_API_KEY},
        )

        # Exit the programme if an error occurs
        if dataset_info_response.status_code != 200:
            logging.error(f"Failed to get dataset information. \n"
                          f"Http code : {dataset_info_response.status_code} \n"
                          f"Content   : {dataset_info_response.content}")
            exit(1)

        # No error occurred, continue loading content
        dataset_info_content = json.loads(dataset_info_response.content)
        example_counter = dataset_info_content[0]['example_count']
        logging.info(f"Number of examples in dataset = {example_counter}")

        # Get dataset examples
        dataset_examples = get_dataset_examples(example_counter)

        # Exit the programme if no runs is found
        if len(dataset_examples) == 0:
            logging.error("No runs found !")
            exit(1)

        # Get the runs of all examples, then create a csv file
        # CSV filename
        output_csv_file = f"export_run_result_{dataset_id}_{int(time.time())}.csv"
        # CSV header line
        csv_lines = [create_csv_header(session_ids)]

        # CSV data lines
        index = 1
        for example in dataset_examples:
            csv_lines.append(append_example_runs(example, session_ids))
            progress = index / example_counter * 100
            logging.info(f"Example processed : {index}/{example_counter} - Progression : {progress:.2f}%")
            index += 1

        # Creation of CSV file
        with open(output_csv_file, 'w', newline='') as csv_file:
            writer = csv.writer(csv_file, delimiter='|')
            writer.writerows(csv_lines)
        logging.info(f"Successful csv generation. Filename : {output_csv_file}")
    except requests.exceptions.RequestException as e:
        logging.error("A connection error has occurred : %s", e)

    logging.info(
        'End of execution. (Duration : %.2f seconds)',
        time.time() - start_time
    )
