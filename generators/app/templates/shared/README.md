# <%= props.appName %>

##Pulling in apidoc

First install [apidoc-cli](https://github.com/mbryzek/apidoc-cli):

```bash
cd ~
git clone git@github.com:mbryzek/apidoc-cli.git```

Then pull down the generated files from apidoc:

```bash
~/apidoc-cli/bin/apidoc update```


##Building the Docker container


To build the docker container you will need to run:

```bash
sbt stage
./docker-build.sh <DOCKER_IMAGE_TAG>```

