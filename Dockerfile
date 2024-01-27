FROM ubuntu:23.10

LABEL author=dokcn
EXPOSE 9000/tcp
#SHELL ["/bin/bash"]

WORKDIR /root/docker

# set timezone and locale
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV LANG=C.utf8

# update apt repo and install utility
RUN sed -i 's@//.*archive.ubuntu.com@//mirrors.ustc.edu.cn@g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install --no-install-recommends curl wget unzip -y

# install jdk
ARG jdkVersion=17
RUN  apt-get install --no-install-recommends openjdk-${jdkVersion}-jdk -y && \
     java --version

# install maven
RUN  apt-get install --no-install-recommends maven -y && \
     mvn --version

# install chrome and chromedriver
ARG chromeDownloadUrl=https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN wget -nv $chromeDownloadUrl && \
    apt-get install --no-install-recommends ./google-chrome-stable_current_amd64.deb -y && \
    google-chrome --version && \
    rm google-chrome-stable_current_amd64.deb

RUN <<EOC
    set -ex
    fullChromeVersion=`google-chrome --version`
    chromeVersion=`echo $fullChromeVersion | cut -d ' ' -f 3 | cut -d '.' -f 1,2,3`
    chromedriverVersion=`curl https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${chromeVersion}`
    wget -nv https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/${chromedriverVersion}/linux64/chromedriver-linux64.zip
    unzip chromedriver-linux64.zip
    cp chromedriver-linux64/chromedriver /bin
    chromedriver --version
    rm -rf chromedriver-linux64
    rm chromedriver-linux64.zip
EOC

# remove apt cache
RUN rm -rf /var/lib/apt/lists/*

# config maven
COPY docker-related/settings.xml /root/.m2/

WORKDIR app
COPY pom.xml .

# download maven dependencies
RUN mvn dependency:resolve

# prefetch jars for maven exec plugin
RUN mvn exec:help
RUN mvn compiler:help

# copy java source files
COPY src src

# compile project
RUN mvn compile -DskipTests

# set jvm memory limit
#ENV MAVEN_OPTS="-Xms256m -Xmx256m"

# default behavior: run java application
ENTRYPOINT mvn -Dheadless -DwebDriverLogLevel=info -DdriverBinaryLocation=/bin/chromedriver -X exec:java
