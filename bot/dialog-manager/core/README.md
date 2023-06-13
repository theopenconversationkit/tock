# Local installation

Below these instructions :

## 1. Python installation

### a) Without python env

Be sure to have installed :

- python3
- `apt install python3-pip`
- `apt -y install graphviz`
- Be sure to have a JAVA_HOME initialized in your profile, .bahsrc for example.
- `pip install --user -r requirements.txt` in bot/dialog-manager/core/src/main/resources/python/install (be sure to be
  able to install with `--user` option)
- Some of your python packages will be installed in `home/$USER/.local/lib/python3.x` if you don't add an option to
  install else where.
- be sure to have jep in your path for instance add in your .bashrc : `export PATH="/home/$USER/.local/bin:$PATH"`

#### Jep and rights, be careful :

- Be sure to have jep in your path
- launch : `which jep` to find where is located installed its binary (the binary is in bin, so the library would be
  lib `../../lib/jep`)
- launch `jep` to see if your user has the rights to launch it
- It may be possible for you to see ```java.lang.ClassNotFoundException: jep.Run``` when you launch `jep` in a terminal,
  do not consider it as a trouble.
- Jep must have the same rights as the JAVA_HOME, so it must be available to your user.

#### Workaround about jep path

If you forget to add in your path, in your IDE in botApi you can add as VM arguments for example the path where jep
library is located :

- Please use the folder where the user is the same as the one who can launch BotApi run configuration in IntelliJ.
- if $USER  `-Djava.library.path=/usr/local/lib/python3.8/site-packages/jep`  
  (little information about : packages in python : https://stackoverflow.com/a/71456833)

#### Python admin issue

Be careful with admin / and current user access on libraries :  
If you installed python as root, when using `pip` some library can say "Permission denied" in your
/usr/local/lib/python3.X/dist-package,
Be sure to avoid to have any libraries in this folder.

### b) With python env

- If you use a virtual env :
    - `python3 -m venv venv`
    - `source venv/bin/activate`
- `pip install -r requirements.txt` in bot/dialog-manager/core/src/main/resources/python/install

#### Workaround

Make sure to set the `PATH` environment variable for example `PATH=$PROJECT_DIR$/venv/bin`
in your IDE run configuration setting of botApi.
