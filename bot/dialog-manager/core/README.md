# Local installation

Below these instructions :
## 1. Python installation
### a) Without python env 
Be sure to have installed :
- python3
- `apt install python3-pip`
- `apt -y install graphviz`
- Be sure to have a JAVA_HOME initialized in your profile, .bahsrc for example.
- `pip install -r requirements.txt` in bot/dialog-manager/core/src/main/resources/python/install
#### Jep and rights, be careful :
- Be sure to have jep in your path
- launch : `which jep` to find were it is installed
- launch `jep` to see if your user has the rights to launch it
- Jep must have the same rights as the JAVA_HOME, so it must be available to your user.
#### Workaround
- In your IDE in botApi you can add as VM arguments for example the path where jep is located :  
  `-Djava.library.path=/usr/local/lib/python3.8/dist-packages/jep`

### b) With python env
- If you use a virtual env :
  - `python3 -m venv venv` 
  - `source venv/bin/activate`
- `pip install -r requirements.txt` in bot/dialog-manager/core/src/main/resources/python/install
#### Workaround
Make sure to set the `PATH` environment variable for example `PATH=$PROJECT_DIR$/venv/bin`
in your IDE run configuration setting of botApi.
