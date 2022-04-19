# tock-sample-cas-auth-provider

Use this module as an example to create CAS authentication dedicated to your organization.

## How it works

Customer may want to provide a CAS based authentication to their Tock ecosystem.

The intended way to do this is to ship a separated JAR containing a custom CAS authentication module.

This way, customer specific security details could be committed in a separated repository

## How to build

In your `ẁorkspace/` directory:

```shell
# Creates directory
mkdir my-company-cas

# Copy tock sample code
git clone https://github.com/theopenconversationkit/tock.git
cp -r tock/samples/tock-sample-cas-auth-provider/. my-company-cas/

# Now using version control
cd my-company-cas
git init
```

Edit following line in `my-company-cas/pom.xml` according to `tock/` location
```
 <relativePath>../../pom.xml</relativePath>
```

Using JDK 8+
```shell
mvn clean install
```

>> **Warning**
>>
>> By default, CAS login page is configured to https://casserver.herokuapp.com/cas/login Which is only meant for test purposes



## How to run locally (Intellij)

### Add CAS module to classpath


In Intellij open Tock project, then
```
File -> Project structure -> Modules -> tock-bot-admin-server -> Dependencies
```

Then

```
Plus Button -> JARs or Directories 
```

Select from this project `target/XX-shaded.jar`, and configure it as `Runtime` dependency



### Configure BotAdmin runner

As an advice, you can install [envfile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin (available in Intellij)


#### How to use sample env (configuration preset) file

In Intellij open BotAdmin runner -> Envfile tab

Plug then activate  [conf/dev/tock-bot-admin-server.env](conf/dev/tock-bot-admin-server.env)

> Sample .env file is configured to work behind corporate proxy.
> Equivalent to `export https_proxy=http://127.0.0.1:3128`

### Test result in Chrome

- Launch BotAdmin (default port is `7999`)
- Open http://localhost:7999 

By default you would be prompted for user/password by demo CAS

Demo CAS Credentials: `casuser` / `Mellon`.



