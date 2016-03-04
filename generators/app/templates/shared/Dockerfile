FROM <%= props.dockerBaseImage %>

ENV JMX_PORT <%= props.jmxPort %>
ENV SVC_MAX_MEMORY <%= props.maxMemory %>

RUN mkdir /opt/play-svc \
  && groupadd -g <%= props.dockerUserGroupId %> <%= props.dockerUserGroupName %> \
  && useradd -u <%= props.dockerUserId %> -g <%= props.dockerUserGroupName %> <%= props.dockerUserName %> -d /opt/play-svc -s /bin/false

WORKDIR /opt/play-svc

COPY target/universal/stage/lib /opt/play-svc/lib
COPY target/universal/stage/bin /opt/play-svc/bin
COPY target/universal/stage/conf /opt/play-svc/conf

RUN chown -R <%= props.dockerUserGroupName %>:<%= props.dockerUserName %> /opt/play-svc

COPY src/main/docker/bin /usr/local/bin/
COPY src/main/docker/etc /etc/
