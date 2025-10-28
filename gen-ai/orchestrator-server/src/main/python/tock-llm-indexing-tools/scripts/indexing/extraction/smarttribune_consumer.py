#   Copyright (C) 2023-2025 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""Smart Tribune import data and formatter for send in opensearch.

Usage:
    smarttribune_consumer.py [-v]  <base_url> <output_csv> [options]

Arguments:

    base_url    the base URL to prefix every FAQ entry's query parameter to
                create a full URL
    output_csv  path to the output, ready-to-index CSV file

Options:
    --knowledge_base=<value>...    name of the target knowledge base, ex: name1 name2 name3

    --tag_title=<value>
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Import and Format a Smart Tribune data by API  into a ready-to-index CSV file
(one 'title'|'source'|'text' line per filtered entry).
"""
import asyncio
import logging
import os
import sys
from functools import partial
from pathlib import Path
from time import time
from urllib.parse import urlparse

import aiohttp
import aiometer
import pandas as pd
from docopt import docopt
from dotenv import load_dotenv


async def _get_number_page(row, token):
    # request numberPage of question with length page's equal to 200
    url_base_api, headers = await prep_call(token)

    async with aiohttp.ClientSession(trust_env=True) as session:
        url = f'{url_base_api}knowledge-bases/{row[1]}/search'

        body = dict(knowledgeType=['question'], channel='faq')
        if cli_args.get('--tag_title') is not None:
            body['filters'] = [{'title': cli_args.get('--tag_title'), 'type': 'tag'}]
        params = dict(limit=200)

        async with session.post(
                url=url, json=body, headers=headers, params=params
        ) as response:
            if response.status != 200:
                logging.error(await response.text(), response.status)
                return 0
            response_json = await response.json()
            total_items = response_json.get('meta').get('totalItems')
            if total_items != 0:
                number_page = response_json.get('meta').get('numberPage')
                logging.info(f'request all question, number of page {number_page}')
                return number_page
            return 0


async def _get_question(token, row, current_page):
    # request documentId and question by page with length page's equal to 200
    url_base_api, headers = await prep_call(token)

    async with aiohttp.ClientSession(trust_env=True) as session:
        url = f'{url_base_api}knowledge-bases/{row.iloc[0]}/search'
        body = dict(knowledgeType=['question'], channel='faq')
        if cli_args.get('--tag_title') is not None:
            body['filters'] = [{'title': cli_args.get('--tag_title'), 'type': 'tag'}]
        params = dict(limit=200, page=current_page)

        async with session.post(
                url, json=body, headers=headers, params=params
        ) as response:
            if response.status != 200:
                logging.error(await response.text(), response.status)
                return row.iloc[0], row.iloc[1], None, None, None

            response_get_questions_json = await response.json()
            df_all_questions = pd.DataFrame(response_get_questions_json.get('data'))
            df_all_questions = df_all_questions.rename(
                columns={'title': 'title', 'slug': 'source'}
            )
            df_all_questions['knowledge_base_id'] = row.iloc[0]
            df_all_questions['channel_id'] = row.iloc[1]
            return df_all_questions.get(
                ['knowledge_base_id', 'channel_id', 'documentId', 'title', 'source']
            )


async def _get_answer(token, row):
    url_base_api, headers = await prep_call(token)
    if cli_args.get('--tag_title') is not None:
        headers['customResponses'] = cli_args.get('--tag_title')
    # Définir l'URL de la requête
    url = f"{url_base_api}knowledge-bases/{row.get('knowledge_base_id')}/questions/{row.get('documentId')}/channels/{row.get('channel_id')}/responses"

    async with aiohttp.ClientSession(trust_env=True) as session:
        async with session.get(url, headers=headers) as response:
            if response.status != 200:
                row['text'] = None
                logging.error(await response.text(), response.status)
                return row.get(
                    [
                        'knowledge_base_id',
                        'channel_id',
                        'documentId',
                        'title',
                        'source',
                        'text',
                    ]
                )

            response_json = await response.json()
            if response_json.get('data'):
                row['text'] = response_json.get('data')[0].get('content').get('body')
            else:
                row['text'] = None
            return row.get(
                [
                    'knowledge_base_id',
                    'channel_id',
                    'documentId',
                    'title',
                    'source',
                    'text',
                ]
            )


def receipt_id_from_allowed_desired_knowledge_base(allowed_knowledge_bases):
    filtered_data = filter(
        lambda item: item.get('name') in cli_args.get('--knowledge_base')
                     and any(channel.get('systemName') == 'faq' for channel in item.get('channels')),
        allowed_knowledge_bases,
    )
    knowledge_bases_id_list = [
        (
            item.get('id'),
            next(
                channel.get('id')
                for channel in item.get('channels')
                if channel.get('systemName') == 'faq'
            ),
        )
        for item in filtered_data
    ]
    df_knowledge_bases = pd.DataFrame(
        knowledge_bases_id_list, columns=['knowledge_base_id', 'channel_id']
    )

    return df_knowledge_bases


async def prep_call(token=None):
    url_base_api = 'https://api-gateway.app.smart-tribune.com/v1/'
    headers = {'Content-Type': 'application/json', 'Accept-Language': 'fr'}
    if token:
        headers['Authorization'] = f'Bearer {token}'

    return url_base_api, headers


async def fetch_auth_token(session, url, headers, json=None):
    async with session.post(url, json=json, headers=headers) as response:
        data = await response.json()
        return data

async def fetch_allowed_knowledge_bases(session, url, headers):
    async with session.get(url, headers=headers) as response:
        data = await response.json()
        return data


async def _main(args, body_credentials):
    """
       import data from Smart Tribune API then format it into a ready-to-index CSV file.
    python smarttribune_consumer.py -v  https://www.cmb.fr/reseau-bancaire-cooperatif/web/aide/faq results_test.csv
    -- [Arkéa - base de connaissances] cmb-pub-part-143

           Parameters:
           args (dict): A dictionary containing command-line arguments.
                       Expecting keys:     '--knowledge_base'
                                           '--tag_title'
                                           '<base_url>'
                                           '<output_csv>'
           body_credentials (dict): A dictionary containing api credentials
                       Expecting keys:     'apiKey'
                                           'apiSecret'
    """

    # receipt auth token
    _start = time()
    url_base_api, headers = await prep_call()

    logging.debug('request token with apiKey and apiSecret')
    url = f'{url_base_api}auth'
    headers = {'Content-Type': 'application/json'}

    async with aiohttp.ClientSession(trust_env=True) as session:
        response_auth = await fetch_auth_token(session=session, json=body_credentials, url=url, headers=headers)
    if not response_auth:
        logging.error(response_auth.get('text'), response_auth.get('status_code'))
        sys.exit(1)

    # save token
    token = response_auth.get('token')
    # request knowledge bases accessible with this token
    logging.debug('request allowed knowledge bases list and associated channels')
    url = f'{url_base_api}knowledge-bases?limit=200'
    headers['Authorization'] = f'Bearer {token}'
    async with aiohttp.ClientSession(trust_env=True) as session:
        response_allowed_knowledge_bases = await fetch_allowed_knowledge_bases(session=session, url=url, headers=headers)
    if not response_allowed_knowledge_bases.get('data'):
        logging.error(
            response_allowed_knowledge_bases.get('text'),
            response_allowed_knowledge_bases.get('status_code'),
        )
        sys.exit(1)

    # filter knowledge base id and faq channel id associated
    logging.debug(
        'filtering knowledge base allowed for take knowledge_base_id and channel_id associated'
    )
    results_allowed_knowledge_bases = response_allowed_knowledge_bases.get(
        'data'
    )
    df_knowledge_bases = receipt_id_from_allowed_desired_knowledge_base(
        results_allowed_knowledge_bases
    )
    # receipt number_page by knowledge_bases
    logging.debug('request number page of question by knowledge base')
    coroutines = [
        _get_number_page(row, token) for row in df_knowledge_bases.itertuples()
    ]
    number_pages = await asyncio.gather(*coroutines, return_exceptions=False)
    df_knowledge_bases['number_page'] = pd.Series(number_pages)

    # receipt question by knowledge_base_id
    logging.debug('request questions by page')

    coroutines = [
        _get_question(token, row, current_page)
        for _, row in df_knowledge_bases.iterrows()
        for current_page in range(1, row.iloc[2] + 1)
    ]
    rawdata = await asyncio.gather(*coroutines, return_exceptions=False)
    df_all_questions = pd.concat([pd.DataFrame(page) for page in rawdata])

    # receipt answer by documentId
    logging.debug('request answer by question')
    rawdata = await aiometer.run_all(
        [partial(_get_answer, token, row[1]) for row in df_all_questions.iterrows()],
        max_at_once=20,
        max_per_second=20,
    )

    df_all_questions = pd.DataFrame(rawdata)
    print(df_all_questions.get('source'))
    # format data
    logging.debug('format data')
    df_all_questions[
        'source'
    ] = f"{cli_args.get('<base_url>')}?question=" + df_all_questions.get('source')

    # export data
    logging.debug(f"Export to output CSV file {args.get('<output_csv>')}")
    logging.info(
        f'finished {len(df_all_questions)} questions in {time() - _start:.2f} seconds'
    )
    df_all_questions.get(['title', 'source', 'text']).to_csv(
        args.get('<output_csv>'), sep='|', index=False
    )


if __name__ == '__main__':
    load_dotenv()
    cli_args = docopt(__doc__, version='Smart Tribune consumer 0.1.0')

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args.get('-v') else logging.INFO,
        format=log_format,
    )

    # Check args:
    # - base url must be valid
    result = urlparse(cli_args.get('<base_url>'))
    if not result.scheme or not result.netloc:
        logging.error(
            f"Cannot proceed: '{cli_args.get('<base_url>')}' is not a valid URL"
        )
        sys.exit(1)

    # - output file path
    target_dir = Path(cli_args.get('<output_csv>')).parent
    if not target_dir.exists():
        logging.error(f'Cannot proceed: directory {target_dir} does not exist')
        sys.exit(1)

    # check credentials
    if not os.getenv('APIKEY'):
        logging.error('Cannot proceed: APIKEY  does not configured in .env ')
        sys.exit(1)
    if not os.getenv('APISECRET'):
        logging.error('Cannot proceed: APISECRET  does not configured in .env ')
        sys.exit(1)

    body_credentials = dict(
        apiKey=os.getenv('APIKEY'), apiSecret=os.getenv('APISECRET')
    )

    # Main func
    asyncio.run(_main(cli_args, body_credentials))
