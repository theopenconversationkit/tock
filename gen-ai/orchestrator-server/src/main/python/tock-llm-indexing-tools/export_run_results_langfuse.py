"""
Export a LangFuse dataset run results.

Usage:
    export_run_results_langfuse.py [-v] <dataset_name> <runs_names>...
    export_run_results_langfuse.py -h | --help
    export_run_results_langfuse.py --version

Arguments:
    dataset_name      dataset id
    runs_names     list of session ids

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging

The exported CSV file will have these columns :
'Reference input'|'Reference output'|'Response 1'|'Sources 1'|...|'Response N'|'Sources N'
NB: There will be as many responses as run sessions
"""
import csv
import logging
import os
import sys
import time

from docopt import docopt
from dotenv import load_dotenv
from langfuse import Langfuse


def fetch_trace_by_item_and_dataset_run(dataset_run, item):
    """
    Returns the trace of the dataset_run_item of this dataset_run and this dataset_item
    Args:
        dataset_run: the dataset run with items
        item: the item

    Returns: trace or None if no dataset_run_item of this item and this dataset_run
    """
    for item_run in dataset_run:
        if item.id == item_run.dataset_item_id:
            trace = client.fetch_trace(item_run.trace_id)
            return trace.data
    return None


def create_csv_header(_runs_names: list[str]):
    """
    Create the CSV file header
    Args:
        _runs_names: the session ids

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

    # Complete csv header with sessions name
    counter = 1
    for run_name in _runs_names:
        csv_header.append(f"{header_answer} {counter} ({run_name})")
        csv_header.append(f"{header_sources} {counter} ({run_name})")
        counter += 1

    return csv_header


def append_runs(dataset_item, _runs_names):
    """
    Append in a CSV line, the fetched runs for the given dataset run
    Args:
        dataset_item: the dataset item
        _runs_names: the runs names list

    Returns: The CSV line
    """

    # Init csv line
    csv_line = [
        dataset_item.metadata["topic"] if dataset_item.metadata else "",
        dataset_item.input["question"],
        dataset_item.expected_output["answer"]
    ]

    # Complete csv line with example run result
    for _run_name in _runs_names:
        dataset_run = client.get_dataset_run(dataset_name=dataset_name, dataset_run_name=_run_name)
        trace = fetch_trace_by_item_and_dataset_run(dataset_run.dataset_run_items, dataset_item)
        if trace is None or trace.output is None or not isinstance(trace.output, dict):
            csv_line.append('')
            csv_line.append('')
        else:
            csv_line.append(trace.output["answer"])
            csv_line.append(
                ','.join([doc["metadata"]["url"] for doc in trace.output["source_documents"]])
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
    langfuse_secret_key = os.getenv('LANGFUSE_SECRET_KEY')
    if not langfuse_secret_key:
        logging.error(
            'Cannot proceed: LANGFUSE_SECRET_KEY env variable is not defined (define it in a .env file)'
        )
        sys.exit(1)
    langchain_host = os.getenv('LANGFUSE_HOST')
    if not langchain_host:
        logging.error(
            'Cannot proceed: LANGFUSE_HOST env variable is not defined (define it in a .env file)'
        )
        sys.exit(1)
    langfuse_public_key = os.getenv('LANGFUSE_PUBLIC_KEY')
    if not langfuse_public_key:
        logging.error(
            'Cannot proceed: LANGFUSE_PUBLIC_KEY env variable is not defined (define it in a .env file)'
        )
        sys.exit(1)
    client = Langfuse()

    # The script arguments
    dataset_name = cli_args['<dataset_name>']
    runs_names = cli_args['<runs_names>']

    # Get dataset
    logging.info(f'Call the LangFuse API to get the dataset information for dataset_name={dataset_name}.')
    dataset = client.get_dataset(name=dataset_name)
    logging.info(f"Number of items in dataset = {len(dataset.items)}")
    output_directory = "export_run_results/" + dataset_name
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)
        logging.debug(f"Répertoire de sortie créé : {output_directory}")
    # CSV filename
    output_csv_file = f"export_run_result_langfuse_{dataset_name}_{int(time.time())}.csv"
    # Chemin du fichier CSV de sortie
    csv_file_path = os.path.join(output_directory, output_csv_file)
    # CSV header line
    csv_lines = [create_csv_header(runs_names)]

    # CSV data lines
    index = 1
    for item in dataset.items:
        csv_lines.append(append_runs(item, runs_names))
        progress = index / len(dataset.items) * 100
        logging.info(f"item processed : {index}/{len(dataset.items)} - Progression : {progress:.2f}%")
        index += 1

    # Creation of CSV file
    with open(csv_file_path, 'w', newline='') as csv_file:
        writer = csv.writer(csv_file, delimiter='|')
        writer.writerows(csv_lines)
    logging.info(f"Successful csv generation. File path : {csv_file_path}")
    logging.info(
        'End of execution. (Duration : %.2f seconds)',
        time.time() - start_time
    )
