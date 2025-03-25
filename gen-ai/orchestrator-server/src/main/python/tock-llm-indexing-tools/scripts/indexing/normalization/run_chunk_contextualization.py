"""
Run an evaluation on LangFuse dataset experiment.
Usage:
        run_chunk_contextualization.py [-v] <chunk_contextualization_input_file>
        run_chunk_contextualization.py -h | --help
        run_chunk_contextualization.py --version

Options:
    -v          Verbose output
    -h --help   Show this screen
    --version   Show version
"""
import os
from langchain_openai import ChatOpenAI
from datetime import datetime
from typing import List

from docopt import docopt
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_llm_factory, \
    create_observability_callback_handler
from gen_ai_orchestrator.services.security.security_service import fetch_secret_key_value
from langchain_core.output_parsers import StrOutputParser
from langfuse import Langfuse

from scripts.common.logging_config import configure_logging
from langchain.prompts import ChatPromptTemplate, PromptTemplate, HumanMessagePromptTemplate

from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.indexing.normalization.models import RunChunkContextualizationInput, RunChunkContextualizationOutput


def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Chunk Contextualization 1.0.0')
    logger = configure_logging(cli_args)

    chunks: List[str] = []
    tested_chunks: List[str] = []
    try:
        logger.info("Loading input data...")
        chunk_contextualization_input = RunChunkContextualizationInput.from_json_file(cli_args["<chunk_contextualization_input_file>"])
        logger.debug(f"\n{chunk_contextualization_input.format()}")

        chunks = chunk_contextualization_input.chunks


        # Define the prompt for generating contextual information
        anthropic_contextual_retrieval_system_prompt = """<document>
                {WHOLE_DOCUMENT}
        </document>

        The following are key extracted chunks from the document:
        <chunks>
        {CHUNKS}
        </chunks>

        From the document content, determine the name of the financial product it describes.

        For each chunk, think through the following steps:
        1. **Identify references to data** (e.g., "Niveau Initial(2)", "Date d'Observation Finale(1)"). Think about what each reference could be referring to.
        2. **Locate the values** in the document that correspond to these references. Think about how the context in the document might help you find these values.
        3. **Replace the references** with their actual values. Think about the effect of this replacement on the clarity of the chunk.
        4. **Generate a succinct yet comprehensive context** for each chunk:
            - Clearly identify the financial product it belongs to. Think about how you can deduce the product name from the document.
            - Ensure that all relevant details needed to understand the chunk are included. Think about what is necessary to contextualize the chunk.
            - Resolve any references (e.g., dates, terms, or entities) explicitly by replacing them with their corresponding values from the document. Think about how to make these references clear for future searches.
            - Maintain clarity and conciseness while preserving critical information. Think about how to summarize the chunk without losing important details.
            - Ensure that the response is in the language of the original document. If the document is in French, respond in French. If it is in English, respond in English. Think about which language the document is written in before responding.
            - Use the correct chunk Id as provided. Think about how to ensure each chunk’s context is correctly mapped to its Id.

        Now, respond with the context for each chunk in the following JSON format. Be sure to use the same chunk Id as provided:

        ```json
        {{
            \"product_name\": \"DETECTED_PRODUCT_NAME\",
            \"contexts\": [
                {{\"id\": \"1\", \"context\": \"Generated context for chunk 1\"}},
                {{\"id\": \"2\", \"context\": \"Generated context for chunk 2\"}}
            ]
        }}
        """

        # Create a PromptTemplate for WHOLE_DOCUMENT and CHUNK_CONTENT
        anthropic_prompt_template = PromptTemplate(
            input_variables=['WHOLE_DOCUMENT', 'CHUNKS'],
            template=anthropic_contextual_retrieval_system_prompt
        )

        # Wrap the prompt in a HumanMessagePromptTemplate
        human_message_prompt = HumanMessagePromptTemplate(prompt=anthropic_prompt_template)
        # Create the final ChatPromptTemplate
        anthropic_contextual_retrieval_final_prompt = ChatPromptTemplate(
            input_variables=['WHOLE_DOCUMENT', 'CHUNKS'],
            messages=[human_message_prompt]
        )


        observability_handler = create_observability_callback_handler(
            observability_setting=chunk_contextualization_input.observability_setting,
            trace_name="Chunk contextualization"
        )
        llm_factory = get_llm_factory(setting=chunk_contextualization_input.llm_setting)
        llm_model_instance = llm_factory.get_language_model()

        # Chain the prompt with the model instance
        contextual_chunk_creation = anthropic_contextual_retrieval_final_prompt | llm_model_instance | StrOutputParser()

        # Process each chunk and generate contextual information
        # for test_chunk in chunks:
        formatted_chunks = "\n".join(
            [f"<chunk id='{chunk.id}'>\n{chunk.content}\n</chunk>" for chunk in chunks]
        )
        res = contextual_chunk_creation.invoke({
            "WHOLE_DOCUMENT": chunk_contextualization_input.document.content,
            "CHUNKS": formatted_chunks
        }, config={'callbacks': [observability_handler]})

        logger.info(f"Result = {res}")
        logger.info('--------------------')
        tested_chunks.append("")


        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)

    len_chunks = len(chunks)
    output = RunChunkContextualizationOutput(
        status = activity_status,
        duration = datetime.now() - start_time,
        items_count=len_chunks,
        success_rate=100 * (len(tested_chunks) / len_chunks) if len_chunks > 0 else 0
    )
    logger.debug(f"\n{output.format()}")

if __name__ == '__main__':
    main()
