"""
Run an evaluation on LangFuse dataset experiment using Ragas.

Usage:
    run_evaluation.py [-v] --json-config-file=<jcf>

Description:
    This script is used to run an evaluation on Langfuse dataset experiment based on a json configuration file.
    The configuration file specifies the Ragas metrics to be calculated.

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.

Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python run_evaluation.py --json-config-file=path/to/config-file.json
"""
from datetime import datetime
from typing import Optional, List

from docopt import docopt
from gen_ai_orchestrator.services.security.security_service import fetch_secret_key_value
from langfuse import Langfuse
from langfuse.api import TraceWithFullDetails, DatasetRunItem
from langfuse.client import DatasetItemClient

from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.dataset.evaluation.models import DatasetExperiment, DatasetExperimentItemScores, RunEvaluationInput, \
    RunEvaluationOutput
from scripts.dataset.evaluation.ragas_evaluator import RagasEvaluator


def get_trace_if_exists(logger, client, dataset_name, experiment_name, _dataset_run,
                        item) -> [Optional[DatasetRunItem], Optional[TraceWithFullDetails]]:
    item_run = next((r for r in _dataset_run if r.dataset_item_id == item.id), None)

    if item_run:
        return item_run, client.get_trace(item_run.trace_id)

    logger.info(f"No trace found for '{item.id}' of dataset '{dataset_name}' in experiment '{experiment_name}!")
    return None, None

def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Evaluation 1.0.0')
    logger = configure_logging(cli_args)

    dataset_experiment = DatasetExperiment()
    experiment_scores: List[DatasetExperimentItemScores] = []
    dataset_items: List[DatasetItemClient] = []

    try:
        logger.info("Loading input data...")
        input_config = RunEvaluationInput.from_json_file(cli_args['--json-config-file'])
        logger.debug(f"\n{input_config.format()}")

        client = Langfuse(
            host=str(input_config.observability_setting.url),
            public_key=input_config.observability_setting.public_key,
            secret_key=fetch_secret_key_value(input_config.observability_setting.secret_key),
        )
        ragas_evaluator = RagasEvaluator(langfuse_client=client, evaluation_input=input_config)

        dataset_experiment=input_config.dataset_experiment
        dataset_name=dataset_experiment.dataset_name
        experiment_name=dataset_experiment.experiment_name
        dataset = client.get_dataset(name=dataset_name)
        dataset_items = dataset.items

        for item in dataset_items:
            dataset_run = client.get_dataset_run(
                dataset_name=dataset_name,
                dataset_run_name=experiment_name
            )
            run_item, run_trace_details = get_trace_if_exists(logger, client, dataset_name, experiment_name, dataset_run.dataset_run_items, item)
            if run_trace_details and run_trace_details.output and isinstance(run_trace_details.output, dict):
                metric_scores = ragas_evaluator.score_with_ragas(
                    item=item,
                    run_trace_details=run_trace_details,
                    experiment_name=experiment_name
                )
                experiment_scores.append(
                    DatasetExperimentItemScores(
                        run_item_id=run_item.id,
                        run_trace_id=run_trace_details.id,
                        metric_scores=metric_scores
                    )
                )
            else:
                logger.warn(f"Impossible to evaluate item '{item.id}' of dataset '{dataset_name}' in experiment '{experiment_name}'!")

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)

    len_dataset_items = len(dataset_items)
    output = RunEvaluationOutput(
        status = activity_status,
        dataset_experiment=dataset_experiment,
        dataset_experiment_scores=experiment_scores,
        duration = datetime.now() - start_time,
        items_count=len(dataset_items),
        success_rate=100 * (len(experiment_scores) / len_dataset_items) if len_dataset_items > 0 else 0
    )
    logger.debug(f"\n{output.format()}")

if __name__ == '__main__':
    main()
