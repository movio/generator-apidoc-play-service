FROM docker.movio.co/java-8:6.0.0

EXPOSE 9000

ENV JMX_PORT <%= props.jmxPort %>
ENV SVC_MAX_MEMORY <%= props.maxMemory %>
ENV RMI_HOSTNAME <%= props.rmiHostname %>

# Move up once finialised
RUN mkdir /var/<%= props.appName %> \
  && groupadd -g 1012 atm \
  && useradd -u 1012 -g atm atm -d /var/<%= props.appName %> -s /bin/false \
  && chown -R atm:atm /var/<%= props.appName %>

WORKDIR /var/<%= props.appName %>

COPY <%= props.appName %>/lib /var/<%= props.appName %>/lib
COPY <%= props.appName %>/bin /var/<%= props.appName %>/bin

COPY src/main/docker/bin /usr/local/bin/
COPY src/main/docker/etc /etc/

COPY conf /var/<%= props.appName %>/conf
