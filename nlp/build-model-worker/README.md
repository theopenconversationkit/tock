# Tock - Worker

This module provides classes used to launch a tock worker

Tock worker supports several build modes :

 - COMMAND_LINE
 - VERTICLE
 - ON_DEMAND

Select a mode with this property (default "COMMAND_LINE"):

    - tock_build_worker_mode 

## Worker as a command line program (COMMAND_LINE) 

With COMMAND_LINE mode you can define one of this build type.

Several types of build are possible :

 - BUILD_DIFF   > Build is started only if at least one sentence status has changed.
 - CLEANUP      > Cleanup orphan builds
 - REBUILD_ALL  > Full rebuild
 - TEST         > Test build

## Worker on dedicated server (VERTICLE)

With VERTICLE mode, worker start "successively" a BUILD_DIFF / TEST and CLEANUP build in a dedicated environment 

## Worker on demand (ON DEMAND)

This module provides classes used to launch ON_DEMAND build type on compute platform like AWS BATCH

To enable a compute environment you have to add an implementation of WorkerOnDemand in your classpath.

### Worker on AWS BATCH

This module provides classes used to launch a tock worker on AWS BATCH (https://aws.amazon.com/batch/)

#### Usage 

First create on AWS Batch :

  - 1 compute environment
  - 1 job definition
  - 1 job queue
  
You can configure your job definition with this container image
    
    tock/build_worker:latest
    
In your worker build dependencies add this 

    <dependency>
        <groupId>ai.tock</groupId>
        <artifactId>tock-nlp-build-model-worker-on-aws-batch</artifactId>
    </dependency>

Configure your job definition name and job queue name with these properties :    

    tock_worker_aws_batch_job_definition_name   =   "tock-worker-job-definition"
    tock_worker_aws_batch_job_queue_name        =   "tock-worker-job-queue"

Start your worker with these properties:

    tock_build_worker_mode                      =   "ON_DEMAND"
    tock_build_worker_on_demand_type            =   "AWS_BATCH"
    
You can pass properties on your AWS BATCH job by prefixing property name with "tock_worker_ondemand"

Examples:

    tock_worker_ondemand_JAVA_ARGS"       =   "-Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=4 -Xmx12888m -XX:MaxMetaspaceSize=256m" />
    tock_worker_ondemand_mongo_url        =   "mongodb://xxxx"
    tock_worker_ondemand_bot_mongo_db     =   "DBNAME"
    tock_worker_ondemand_front_mongo_db   =   "DBNAME"
    tock_worker_ondemand_model_mongo_db   =   "DBNAME"
    tock_worker_ondemand_cache_mongo_db   =   "DBNAME"
    tock_worker_ondemand_env              =   "integ"
    tock_worker_ondemand_default_locale   =   "fr"
     