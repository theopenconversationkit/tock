##### Using pyenv && poetry (recommended)

It's recommended to use pyenv, see install and usage guide here:
https://gist.github.com/trongnghia203/9cc8157acb1a9faad2de95c3175aa875

Basic usage to create a venv with a specific version of Python for this project :

```sh
# In gen-ai/orchestrator-server/src/main/python/server
pyenv install 3.13.0
pyenv local 3.13.0  # Activate Python 3.9 for the current
which python # Check that you use the python version installed by pyenv
python --version # Check your python version
python -m venv .venv # Create a virtual env based on this python version
source .venv/bin/activate # Activate your virtual env
poetry install # Install dependencies for this project in the virtual env
```

To enable the git-committers plugin in MkDocs, configure the MKDOCS_GIT_COMMITTERS_APIKEY environment variable in your .bashrc file as follows:

export MKDOCS_GIT_COMMITTERS_APIKEY=[your_github_token].

Ensure the GitHub token you provide has the necessary repo permissions to access repository data. After adding this line, reload your shell configuration with source ~/.bashrc to apply the changes.

# TODO : Doc dev à rédiger

source .venv/bin/activate
mkdocs serve --dev-addr 127.0.0.1:8182
