In order to install in dev mode, add these symbolic links :

> For unix users, in the current directory (bot/admin/web):

```
ln -s ../../../../nlp/admin/web/src/app/applications src/app/applications
ln -s ../../../../nlp/admin/web/src/app/archive src/app/archive
ln -s ../../../../nlp/admin/web/src/app/build src/app/build
ln -s ../../../../nlp/admin/web/src/app/core-nlp src/app/core-nlp
ln -s ../../../../nlp/admin/web/src/app/entities src/app/entities
ln -s ../../../../nlp/admin/web/src/app/inbox src/app/inbox
ln -s ../../../../nlp/admin/web/src/app/intents src/app/intents
ln -s ../../../../nlp/admin/web/src/app/logs src/app/logs
ln -s ../../../../nlp/admin/web/src/app/model src/app/model
ln -s ../../../../nlp/admin/web/src/app/nlp-tabs src/app/nlp-tabs
ln -s ../../../../nlp/admin/web/src/app/quality-nlp src/app/quality-nlp
ln -s ../../../../nlp/admin/web/src/app/scroll src/app/scroll
ln -s ../../../../nlp/admin/web/src/app/search src/app/search
ln -s ../../../../nlp/admin/web/src/app/sentence-analysis src/app/sentence-analysis
ln -s ../../../../nlp/admin/web/src/app/sentences-scroll src/app/sentences-scroll
ln -s ../../../../nlp/admin/web/src/app/shared-nlp src/app/shared-nlp
ln -s ../../../../nlp/admin/web/src/app/test-nlp src/app/test-nlp
ln -s ../../../../nlp/admin/web/src/app/try src/app/try
```

Alternatively, you can run ```mvn validate``` to create these symbolic links.
 

Then run these npm commands in order to setup and launch [Angular CLI](https://cli.angular.io/).

```
npm install
npm install -g @angular/cli
ng serve
```

WARNING : Those commands require [Python 2.7](https://www.python.org/downloads/release/python-272/). So, if you have different Python versions, specify it to the npm command like this :
```
//python.exe for windows users
npm install --python=D:/devhome/opt/Python27/python.exe
npm install --python=D:/devhome/opt/Python27/python.exe -g @angular/cli
ng serve
```


Of course in the end you need also to start the [Bot Admin server](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml).
