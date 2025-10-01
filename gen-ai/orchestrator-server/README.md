
# Tock Gen AI Orchestrator Server

### Maven build : `POETRY`

If you encounter the problem below, following a maven build on the '**tock-gen-ai-orchestrator-server**' project :

```log
[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:3.1.0:exec (poetry-to-requirements.txt) on project tock-gen-ai-orchestrator-server: 
Command execution failed.: Cannot run program "poetry" (in directory "path/to/tock/gen-ai/orchestrator-server/src/main/python/server"): error=2
```

Please follow these instructions:

- For Linux, the path to this "poetry" script must be declared :
  + in the PATH defined in the ~/.profile configuration file.
  + or in a script that launches the maven build

Tested with python 3.12.2 and poetry 1.8.3