# Local installation

Below these instructions, we are not using python environments.
Be sure to have installed :
- python3
- `apt install python3-pip`
- `apt -y install graphviz`
- Be sure to have a JAVA_HOME initialized in your profile, .bahsrc for example.
- If you use a virtual env :
  - `python3 -m venv venv` 
  - `source venv/bin/activate`
- `pip install -r requirements.txt` in dialog-manager/core/src/main/resources/python/install


## Careful with rights
- be sure to have jep in your path
- launch : `wich jep` to find were it is installed
- launch `jep` to see if your user has the rights to launch it
- Jep must have the same rights as the JAVA_HOME, so it must be available to your user.

## Workaround
- In your IDE in botApi you can add as VM arguments for example the path where jep is located :  
`-Djava.library.path=/usr/local/lib/python3.8/dist-packages/jep`
- Also if you have installed the dependencies into a virtual env make sure to set the `PATH` environement variable
in your IDE run configuration setting of botApi.