FROM <%= props.dockerBaseImage %>

EXPOSE 9000

ENV JMX_PORT <%= props.jmxPort %>
ENV SVC_MAX_MEMORY <%= props.maxMemory %>

#Install supervisor and deps required for gosu
RUN apt-get update && apt-get install -y supervisor build-essential libsdl2-dev libsdl2-ttf-dev libpango1.0-dev \
                     libgl1-mesa-dev libfreeimage-dev libopenal-dev libsndfile-dev ruby-dev
RUN mkdir -p /var/log/supervisor

# disable CGO for ALL THE THINGS (to help ensure no libc)
ENV CGO_ENABLED 0

# install golang
ENV GOLANG_VERSION 1.4.2

RUN curl -sSL https://golang.org/dl/go$GOLANG_VERSION.src.tar.gz \
    | tar -vxz -C /usr/src \
    && cd /usr/src/go/src \
    && ./make.bash --no-clean 2>&1 \
    && mkdir -p /go/src /go/bin \
    && chmod -R 755 /go

ENV PATH /go/bin:/usr/src/go/bin:$PATH
ENV GOPATH /go
ENV GO_SRC_PATH /go/src
ENV GO_BIN_PATH /go/bin

# install gosu
ENV GOSU_VERSION 1.4
ENV GOPATH $GOPATH:/go/src/github.com/docker/libcontainer/vendor
ENV LIBCONTAINER_REPO_PATH github.com/docker/libcontainer
ENV LIBCONTAINER_VERSION b322073f27b0e9e60b2ab07eff7f4e96a24cb3f9
ENV GOSU_REPO_PATH github.com/tianon/gosu
RUN git clone https://$LIBCONTAINER_REPO_PATH.git $GO_SRC_PATH/$LIBCONTAINER_REPO_PATH \
    && cd $GO_SRC_PATH/$LIBCONTAINER_REPO_PATH \
    && git checkout --quiet $LIBCONTAINER_VERSION \
    && git clone https://$GOSU_REPO_PATH.git $GO_SRC_PATH/$GOSU_REPO_PATH \
    && cd $GO_SRC_PATH/$GOSU_REPO_PATH \
    && git checkout --quiet $GOSU_VERSION \
    && go build -a -ldflags '-d -s' -o $GO_BIN_PATH/gosu \
    && rm -rf $GO_SRC_PATH/$LIBCONTAINER_REPO_PATH $GO_SRC_PATH/$GOSU_REPO_PATH

RUN mkdir /opt/play-svc \
  && groupadd -g <%= props.dockerUserGroupId %> <%= props.dockerUserGroupName %> \
  && useradd -u <%= props.dockerUserId %> -g <%= props.dockerUserGroupName %> <%= props.dockerUserName %> -d /opt/play-svc -s /bin/false

WORKDIR /opt/play-svc

COPY <%= props.appName %>/lib /opt/play-svc/lib
COPY <%= props.appName %>/bin /opt/play-svc/bin
COPY <%= props.appName %>/conf /opt/play-svc/conf

RUN chown -R <%= props.dockerUserGroupName %>:<%= props.dockerUserName %> /opt/play-svc

COPY src/main/docker/bin /usr/local/bin/
COPY src/main/docker/etc /etc/

#Start supervisor which will run our play service
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]
