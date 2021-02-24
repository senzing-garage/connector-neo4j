# connector-neo4j

## Overview

The Neo4j connector is an application, written in Java, which gathers information from Senzing and maps it into Neo4j graph database.  The connector reads messages containing Senzing information from a message queue (RabbitMQ or AWS SQS), derives from that data what entities in the Senzing repository are affected, gets the entity data, using the Senzing API, finds how the entities relate to other entities and inserts that data into a Neo4j database.  Note that this connector does not load source records into the Neo4j database.  It loads the Senzing entity information and each entity can be constructed from multiple source records.  If the source record data is desired, and how it relates to the Senzing entities, it will need to be loaded into the database prior to loading the Senzing entities.  In that case the records need to contain DATA_SOURCE and RECORD_ID fields, matching those used in Senzing repository for linking the Senzing entities back to source system records.

The messages read from the message queue are in json format and an example looks like this:
```console
{"DATA_SOURCE":"TEST","RECORD_ID":"RECORD3","AFFECTED_ENTITIES":[{"ENTITY_ID":1,"LENS_CODE":"DEFAULT"}]}
```

This project gives the framework for mapping Senzing data to Neo4j database but can be modified to fit the user's specific solutions.

### Contents

1. [Demonstrate using Command Line](#demonstrate-using-command-line)
    1. [Dependencies](#dependencies)
    1. [Building](#building)
    1. [Preparation for running](#preparation-for-running)
    1. [Running](#running)
1. [Demonstrate using Docker](#demonstrate-using-docker)
    1. [Expectations for docker](#expectations-for-docker)
    1. [Initialize Senzing](#initialize-senzing)
    1. [Configuration](#configuration)
    1. [Develop](#develop)
    1. [Run docker container](#run-docker-container)
	
### Legend

1. :thinking: - A "thinker" icon means that a little extra thinking may be required.
   Perhaps you'll need to make some choices.
   Perhaps it's an optional step.
1. :pencil2: - A "pencil" icon means that the instructions may need modification before performing.
1. :warning: - A "warning" icon means that something tricky is happening, so pay attention.

## Demonstrate using Command Line

### Dependencies

To build the Neo4j Connector you will need [Apache Maven](https://maven.apache.org/install.html) (recommend version 3.6.1 or later)
as well as [OpenJDK](http://openjdk.java.net/) version 11.0.x (recommend version 11.0.6+10 or later).

This application interacts with Senzing API so it needs to be installed beforehand.  Information on how to install it can be found here: [Senzing API quick start](https://docs.senzing.com/quickstart/)

You will also need the Senzing `g2.jar` file installed in your Maven repository.  This file is included in the Senzing API installation above.  In order to install `g2.jar` you must:

1. Locate your
   [SENZING_G2_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_g2_dir)
   directory.
   The default locations are:
    1. [Linux](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/install-senzing-api.md#centos): `/opt/senzing/g2`
    1. Windows MSI Installer: `C:\Program Files\Senzing\`

1. Determine your `SENZING_G2_JAR_VERSION` version number:
    1. Locate your `g2BuildVersion.json` file:
        1. Linux: `${SENZING_G2_DIR}/g2BuildVersion.json`
        1. Windows: `${SENZING_G2_DIR}\data\g2BuildVersion.json`
    1. Find the value for the `"VERSION"` property in the JSON contents.
       Example:

        ```console
        {
            "PLATFORM": "Linux",
            "VERSION": "1.14.20060",
            "API_VERSION": "1.14.3",
            "BUILD_NUMBER": "2020_02_29__02_00"
        }
        ```

1. Install the `g2.jar` file in your local Maven repository, replacing the
   `${SENZING_G2_DIR}` and `${SENZING_G2_JAR_VERSION}` variables as determined above:

    1. Linux:

        ```console
        export SENZING_G2_DIR=/opt/senzing/g2
        export SENZING_G2_JAR_VERSION=1.14.3

        mvn install:install-file \
            -Dfile=${SENZING_G2_DIR}/lib/g2.jar \
            -DgroupId=com.senzing \
            -DartifactId=g2 \
            -Dversion=${SENZING_G2_JAR_VERSION} \
            -Dpackaging=jar
        ```

    1. Windows:

        ```console
        set SENZING_G2_DIR="C:\Program Files\Senzing\g2"
        set SENZING_G2_JAR_VERSION=1.14.3

        mvn install:install-file \
            -Dfile="%SENZING_G2_DIR%\lib\g2.jar" \
            -DgroupId=com.senzing \
            -DartifactId=g2 \
            -Dversion="%SENZING_G2_JAR_VERSION%" \
            -Dpackaging=jar
        ```

1. Setup your environment.  The Connector relies on native libraries and the
   environment must be properly setup to find those libraries:

    1. Linux

        ```console
        export SENZING_G2_DIR=/opt/senzing/g2

        export LD_LIBRARY_PATH=${SENZING_G2_DIR}/lib:${SENZING_G2_DIR}/lib/debian:$LD_LIBRARY_PATH
        ```

    1. Windows

        ```console
        set SENZING_G2_DIR="C:\Program Files\Senzing\g2"

        set Path=%SENZING_G2_DIR%\lib;%Path%
        ```

### Building

The Neo4j connector is built on [Senzing listener](https://github.com/Senzing/senzing-listener) framework.  This framework needs to be downloaded and built before the Neo4j connector is built.  The steps for building the listener are:

```console
git clone git@github.com:Senzing/senzing-listener.git
cd senzing-listener
mvn clean install
```

To build connector-neo4j simply execute:

```console
git clone git@github.com:Senzing/connector-neo4j.git
cd connector-neo4j
mvn install
```

The JAR file will be contained in the `target` directory under the name `neo4j-connector-[version].jar`.

Where `[version]` is the version number from the `pom.xml` file.

In addition `target/libs` will contain all the depending jar files needed by the application
and `target/conf/neo4jconnector.properties` holds the configuration needed by the application and 
it will require modifications to match the installation of g2 and other applications the Connector depends on.  SEE BELOW.

### Preparation for running

The Connector requires installations of Senzing API (see above), RabbitMQ and Neo4j for its operation.

Note: if docker containers are used it is best to use a docker network to facilitate communication between the containers.
An example for setting up a network:
```console
sudo docker network create -d bridge ncn
```
This network "ncn" will be used when dealing with containers in this write-up.

1. Installing G2

    If not done already.  See [Dependencies](#dependencies) above.

1. Install Neo4j

     An easy way to install and run Neo4j is to run it as a docker container
        
    ```console
        sudo sudo docker run --detach \
            --publish=7474:7474 \
            --publish=7687:7687 \
            --volume=$HOME/neo4j/data:/data \
            --volume=$HOME/neo4j/logs:/logs \
            --network ncn \
            neo4j:latest
    ```

    Other ways to install and run Neo4j can be found here:
    https://neo4j.com/docs/operations-manual/current/installation/

    Once the installation is done go to `http://<server name>:7474`, using a browser.
    If the installation is local that would be `http://locahlost:7474`.
    Log in using default user name and password, which are neo4j/neo4j.  You will be asked to change your password. Do so and remember the password since you will need it for the `Edit configuration` section below.

1. Install RabbitMQ

    Again, run it as a docker container is a simple option
    ```console
       sudo docker run -it --rm --name rabbitmq \
           --publish 5672:5672 \
           --publish 15672:15672 \
            --network ncn \
           rabbitmq:3-management
    ```
    If using an installer is preferred please see
        https://www.rabbitmq.com/download.html
            
1. :thinking: **Optional:**  Create a queue in RabbitMQ

    The Connector will create the queue specified in configuration if it doesn't exist already.  If having a queue created beforehand is desired, here are the steps:

    1. Open up a browser and enter `http://<host name>:15672` into the address bar.  If you install locally this will be `http://localhost:14562`
    1. Log in. Default is guest/guest on a fresh install.
    1. Select `Queues` tab at the top.
    1. Click `Add a new queue` below the grid.
    1. Enter `senzing` in the `Name` box.
    1. For the `Durability` option, click the pull-down and selet `Transient`.
    1. Click `Add Queue` button at the bottom.

1. :pencil2: Edit configuration

    There are two ways to pass configuration to the connector.  Through a configuration file and with command line parameters.
    
    Lets first look at the configuration file.  The configuration file is found at `target/conf/neo4jconnector.properties`.  The steps to set it up follow.
    
    1. Locate the G2 ini file.  It can generally be found in the project path as `/home/<user>/senzing/etc/G2Module.ini` where `user` is the user account. See the [Quick Start Guide](https://senzing.zendesk.com/hc/en-us/articles/115002408867-Quickstart-Guide) for further information.
    1. Open `target/conf/neo4jconnector.properties` in an editor.
    1. Change the value of `neo4jconnector.g2.inifile` to what was found in step 1. above.
    1. Change the `neo4jPassword` for `neo4jconnector.neo4j.uri` to the password you created in `Install Neo4j` section above.
    1. Make any other changes needed. For example if RabbitMQ was set up with user security then user name and password need to be set in the file.
    
    The command line takes following options:
```console
    -iniFile 
        path to the G2 ini file
    -neo4jConnection
        connection string for neo4j, the format is `bolt://<user>:<password>@<hostname>:<port>`
    -mqHost
        host name or ip address for RabbitMQ server
    -mqUser
        RabbitMQ user name
    -mqPassword
        Password for RabbitMQ
    -mqQueue
        The name of the RabbitMQ queue used for receiving messages
```

    If both configuration file and command line options are used the command line options take precedence.

### Running

To execute the server you will use `java -jar`.  It is assumed that your environment
is properly configured as described in the "Dependencies" and "Preparation for running" sections above.

Type
```console
java -jar neo4j-connector-[version].jar
```

Where `[version]` is the version number from the `pom.xml` file.

If command line options are used it could look like this:
```console
java -jar neo4j-connector-[version].jar \
    -iniFile /home/user/senzing/etc/G2Module.ini \
    -neo4jConnection bolt://neo4j:neo4jPassword@localhost:7687 \
    -mqHost localhost \
    -mqQueue senzing
```


## Demonstrate using Docker

### Expectations for docker

#### Space for docker

This repository and demonstration require 6 GB free disk space.

#### Time for docker

Budget 40 minutes to get the demonstration up-and-running, depending on CPU and network speeds.

#### Background knowledge for docker

This repository assumes a working knowledge of:

1. [Docker](https://github.com/Senzing/knowledge-base/blob/master/WHATIS/docker.md)


### Initialize Senzing

1. If Senzing has not been initialized, visit
   "[How to initialize Senzing with Docker](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/initialize-senzing-with-docker.md)".

### Configuration

Configuration values specified by environment variable or command line parameter.

- **[SENZING_G2_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_g2_dir)**

### Develop

#### Prerequisite software

The following software programs need to be installed:

1. [git](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/install-git.md)
1. [make](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/install-make.md)
1. [jq](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/install-jq.md)
1. [docker](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/install-docker.md)

#### Clone repository

For more information on environment variables,
see [Environment Variables](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md).

1. Set these environment variable values:

    ```console
    export GIT_ACCOUNT=senzing
    export GIT_REPOSITORY=connector-neo4j
    export GIT_ACCOUNT_DIR=~/${GIT_ACCOUNT}.git
    export GIT_REPOSITORY_DIR="${GIT_ACCOUNT_DIR}/${GIT_REPOSITORY}"
    ```

1. Follow steps in [clone-repository](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/clone-repository.md) to install the Git repository.

#### Build docker image for development

1. :pencil2: Set environment variables.
   Example:

    ```console
    export SENZING_G2_DIR=/opt/senzing/g2
    ```

1. Build docker image.

    ```console
    cd ${GIT_REPOSITORY_DIR}

    sudo make \
        SENZING_G2_JAR_PATHNAME=${SENZING_G2_DIR}/lib/g2.jar \
        SENZING_G2_JAR_VERSION=$(cat ${SENZING_G2_DIR}/g2BuildVersion.json | jq --raw-output '.VERSION') \
        docker-build
    ```

    Note: `sudo make docker-build-development-cache` can be used to create cached docker layers.


### Run docker container

1. :pencil2: Set environment variables.

    - **[SENZING_DATA_VERSION_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_data_version_dir)**
    - **[SENZING_ETC_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_etc_dir)**
    - **[SENZING_G2_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_g2_dir)**
    - **[SENZING_VAR_DIR](https://github.com/Senzing/knowledge-base/blob/master/lists/environment-variables.md#senzing_var_dir)**

    In addition the path to the project directory must be set.  The project directory is where the G2 project was created.  See the [Quick Start Guide](https://senzing.zendesk.com/hc/en-us/articles/115002408867-Quickstart-Guide).

    As an example:

    ```console
    export PROJECT_DIR=/home/<USER>/senzing
    ```
    Where USER is the name of the user account.
 
1. Prepare for running.

    Ensure the steps in [Preparation for running](preparation-for-running) have been executed before running the docker container. 

1. Run docker container.

    1. :warning:
       **macOS** - [File sharing](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/share-directories-with-docker.md#macos)
       must be enabled for the volumes.
    1. :warning:
       **Windows** - [File sharing](https://github.com/Senzing/knowledge-base/blob/master/HOWTO/share-directories-with-docker.md#windows)
       must be enabled for the volumes.

   When running the docker container the command line options need to be used.

   Example:

    ```console
    sudo docker run \
      --volume ${SENZING_DATA_VERSION_DIR}:/opt/senzing/data \
      --volume ${SENZING_ETC_DIR}:/etc/opt/senzing \
      --volume ${SENZING_G2_DIR}:/opt/senzing/g2 \
      --volume ${SENZING_VAR_DIR}:/var/opt/senzing \
      --volume ${PROJECT_DIR}:${PROJECT_DIR} \
      --network ncn \
      senzing/connector-neo4j \
          -iniFile /home/user/senzing/etc/G2Module.ini \
          -neo4jConnection bolt://neo4j:neo4jPassword@localhost:7687 \
          -mqHost localhost \
          -mqQueue senzing
    ```

