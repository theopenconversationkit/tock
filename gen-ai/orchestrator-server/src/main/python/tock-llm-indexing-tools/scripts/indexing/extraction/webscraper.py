#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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
"""Simple recursive webscraper based on a list of BeautifulSoup filters.

Usage:
    webscraping.py [-v] <input_urls> <soup_filters> <output_csv>
    webscraping.py -h | --help
    webscraping.py --version

Arguments:
    input_urls      a comma-separated list of base URLs the scraper will
                    browse recursively to find scrapable contents
    soup_filters    a comma-separated list of filters to get pages contents
                    (texts to be indexed will be concatenated in this order)
                    Example: id='notes',class_='container',id='test'
    output_csv      path to the output, ready-to-index CSV file (this file will
                    be created at execution time, along with a
                    <base URL netloc>/ sub-dir in the same directory, containg
                    debug info)

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Recursively browse web URLs (follow links from these base URLs), then scrape
links' contents based on a list of BeautifulSoup filters, then export these
contents into a ready-to-index CSV file (one 'title'|'source'|'text' line per
URL with scraped contents).
"""
import logging
import mimetypes
import sys
from pathlib import Path
from urllib import request
from urllib.error import URLError
from urllib.parse import urljoin, urlparse

import magic
import pandas as pd
import requests
from bs4 import BeautifulSoup
from docopt import docopt


def browse_base_urls(
    base_urls: list[str], target_dir: str = '.', base_domain: str = 'domain'
):
    """
    Recursively browse URLs for sub-URLs. Creates the <base_domain>/urls.txt
    file along the way (the base URLs are listed in this file).

        Arguments:
            base_urls   a list of base URLs that will be browsed recursively
                        for sub-URLs (follow links)
            target_dir  the directory to store output files
            base_domain all sub-URLs will be checked to be in this base domain,
                        and all debug will go in a subdirectory with this name
    """
    # Create the debug sub-directory if it does not already exist
    path = Path(target_dir) / base_domain
    path.mkdir(parents=True, exist_ok=True)

    # Open results file
    path = path / 'scanned_urls.txt'
    with open(path, 'w') as output_file:
        # memorize visited URLs
        visited_urls = set()

        # Browse each base URL recursively
        while base_urls:
            base_url = base_urls.pop()

            # Loop recursively through the URLs to be visited, from the base URL
            urls_to_visit = {base_url}
            while urls_to_visit:
                current_url = urls_to_visit.pop()
                logging.debug(f'Visiting {current_url}')

                try:
                    # Check URL is valid
                    with request.urlopen(current_url) as response:
                        if response.status == 200:
                            # Scrape the HTML page with BeautifulSoup
                            soup = BeautifulSoup(response.read(), 'html.parser')

                            # Check base URL domain is in the same domain
                            base_url_href = soup.find(name='base').get('href')
                            base_url_domain = urlparse(base_url_href).netloc
                            if base_url_domain == base_domain:
                                # Find all links on the page
                                for link in soup.find_all('a', class_='btn_arrow'):
                                    # Get the URL from the link
                                    link_url = link.get('href')
                                    if link_url:
                                        # Normalize the link URL by joining it with the base URL
                                        link_url = urljoin(base_url_href, link_url)

                                        # Add the link URL to the set of URLs to be visited if it hasn't been visited yet
                                        if link_url not in visited_urls:
                                            logging.debug(
                                                f'Add to-be-visited link {link_url}'
                                            )
                                            urls_to_visit.add(link_url)

                                # Write row in output file
                                output_file.write(f'{current_url}\n')
                                # Add current url to the set of visited URLs
                                visited_urls.add(current_url)
                                logging.debug(f'Add to visited {current_url}')
                            else:
                                logging.warning(
                                    f"URL '{current_url}' is ignored because its base URL href ({base_url_href}) is not in the '{base_domain}' domain"
                                )
                        else:
                            logging.warning(
                                f"URL '{current_url}' is ignored because it answered GET with code {response.status}"
                            )

                except URLError as e:
                    logging.warning(
                        f"URL '{current_url}' is ignored because it failed to answer GET ({e})"
                    )


def scrape_urls(soup_filters, output_file, target_dir='.', base_domain='domain'):
    """
    Scrape all URLs listed in 'urls.txt' file with BeautifulSoup: create one
    txt file per scraped URL, then create the ready-to-index CSV file.

        Arguments:
            soup_filters:   a string containing comma-separated BeautifulSoup
                            filters
            output_file:    str path to the output file
            target_dir      the directory to store output files
            base_domain     all sub-URLs will be checked to be in this base
                            domain, and all debug will go in a subdirectory
                            with this name
    """
    results = []
    ignored_urls = []
    scraped_urls = []

    # fetch URLs file contents
    urls_file_path = Path(target_dir) / base_domain / 'scanned_urls.txt'
    with open(urls_file_path, 'r') as file:
        # Scrape contents for each line
        for line in file:
            line = line.strip()
            logging.debug(f'Scraping {line}')

            # GET contents
            with request.urlopen(line) as response:
                # Check if response object is not None
                if response is not None:
                    # Scrape the HTML page for tags corresponding to BeautifulSoup filters
                    soup = BeautifulSoup(response.read(), 'html.parser')
                    main_tags = [
                        soup.find(**soup_filter) for soup_filter in soup_filters
                    ]

                    if main_tags:
                        # Create a filename based on the url
                        contents_filename = (
                            line.replace('https://', '')
                            .replace('http://', '')
                            .replace('/', '_')
                            .replace(':', '')
                            + '.txt'
                        )
                        contents_file_path = (
                            Path(target_dir) / base_domain / contents_filename
                        )
                        # Scrape contents for each main Tag
                        scraped_texts = [
                            tag.get_text(separator='. ', strip=True).replace(
                                '.. ', '. '
                            )
                            for tag in main_tags
                            if tag is not None
                        ]
                        # Contatenate texts
                        if len(scraped_texts) == len(main_tags):
                            scraped_urls.append({'scraped_urls': line})
                            full_text = '. '.join(scraped_texts).replace('.. ', '. ')
                            # Write the processed text to the corresponding file
                            with open(contents_file_path, 'w') as contents_file:
                                contents_file.write(full_text)

                            # Scrape title (remove trailing e.g. ' | Crédit Mutuel de Bretagne')
                            title = (
                                soup.find(name='title')
                                .get_text(strip=True)
                                .split(sep=' | ')[0]
                            )

                            # Add URL with title and text to output file
                            results.append(
                                {'title': title, 'source': line, 'text': full_text}
                            )
                        else:
                            logging.debug(
                                f"URL '{line}' is ignored because scraped_texts={len(scraped_texts)}'"
                                f' is different from main_tags={len(main_tags)}'
                            )
                            ignored_urls.append({'ignored_urls': line})
                    else:
                        logging.debug(f'Line {line} is ignored (empty tags)')
                else:
                    logging.warning(
                        f"URL '{line}' is ignored because it failed to answer GET"
                    )

    # Save to output CSV file (use pandas to ensure 'ready-to-index' consistency)
    pd.DataFrame(results).to_csv(output_file, sep='|', index=False)
    pd.DataFrame(ignored_urls).to_csv(
        Path(target_dir) / base_domain / 'ignored_urls.txt',
        sep='|',
        index=False,
        header=False,
    )
    pd.DataFrame(scraped_urls).to_csv(
        Path(target_dir) / base_domain / 'scraped_urls.txt',
        sep='|',
        index=False,
        header=False,
    )

def get_mime_type(url):
    # 1. Essayer via Content-Type HTTP
    _mime_type = "Unknown"
    try:
        response = requests.head(url, allow_redirects=True)
        content_type = response.headers.get("Content-Type")
        if content_type:
            _mime_type =  content_type
    except requests.RequestException:
        pass

    # 2. Essayer via l'extension de fichier
    _mime_type = mimetypes.guess_type(url)[0]
    if _mime_type:
        return _mime_type

    # 3. Télécharger un petit morceau et détecter avec magic
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()
        sample = response.raw.read(2048)
        mime = magic.Magic(mime=True)
        _mime_type = mime.from_buffer(sample)
    except requests.RequestException:
        pass

    return _mime_type



if __name__ == '__main__':


    cli_args = docopt(__doc__, version='Webscraper 0.1.0')

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.WARNING, format=log_format
    )

    # Check args:
    # - input URLs
    base_urls = cli_args['<input_urls>'].split(',')

    print(get_mime_type(base_urls[0]))
    exit(0)

    if not base_urls[0]:
        logging.error(f"Cannot proceed: could not find a URL in list '{base_urls}'")
        sys.exit(1)
    base_domain = urlparse(base_urls[0]).netloc
    for base_url in base_urls:
        parsed_url = urlparse(base_url)
        if not parsed_url.scheme or not parsed_url.netloc:
            logging.error(f"Cannot proceed: '{base_url}' is not a valid URL")
            sys.exit(1)
        if base_domain != parsed_url.netloc:
            logging.error(
                f"Cannot proceed: '{base_url}' has a different base domain ('{parsed_url.netloc}') than previous URLs ('{base_domain}')"
            )
            sys.exit(1)

    # - BeautifulSoup filters
    filters = cli_args['<soup_filters>'].split(',')
    filters_as_dicts = []
    if filters:
        for filter in filters:
            key, value = filter.split('=')
            value = value.strip(
                "'"
            )  # Strip extra quotes (we want a dict of {str: str})
            if key and value:
                filters_as_dicts.append({key: value})
            else:
                logging.error(
                    f"Cannot proceed: BeautifulSoup filter '{filter}' could not be parsed into a key and its value"
                )
                sys.exit(1)
    else:
        logging.error(
            f"Cannot proceed: BeautifulSoup filters arg ({cli_args['<soup_filters>']}) could not be parsed for a list of filters"
        )
        sys.exit(1)

    # - output file path
    target_dir = Path(cli_args['<output_csv>']).parent
    if not target_dir.exists():
        logging.error(f'Cannot proceed: directory {target_dir} does not exist')
        sys.exit(1)

    # Two successive main funcs:
    # - Browse base URLs recursively to populate the urls.txt file listing all URLs to be scraped
    browse_base_urls(
        base_urls=base_urls, target_dir=target_dir, base_domain=base_domain
    )
    # - Scrape all URLs from urls.txt using BeautifulSoup filters
    scrape_urls(
        soup_filters=filters_as_dicts,
        output_file=cli_args['<output_csv>'],
        target_dir=target_dir,
        base_domain=base_domain,
    )
