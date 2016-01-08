# <%= props.appName %>


<%= props.projectName %> Dockerfile
=====================================

| | |
| - | - |
| Based On | [`docker.movio.co/java-8`](https://bitbucket.org/moviohq/docker-java-8/overview) |
| Description | Used to build docker container for the <%= props.projectName %> Play App |

## Volumes

-v /path/to/server.conf:/var/<%= props.appName %>/conf/server.conf -v /path/to/logback.xml:/var/<%= props.appName %>/conf/logback.xml -p 9000:9000
