FROM ubuntu:20.10

RUN apt update && apt -y upgrade
RUN apt install -y libopencv-dev python3-opencv openjdk-11-jre

COPY ./target/scala-2.12/AutoHermod-assembly-0.1.jar /opt/AutoHermod/

COPY resources /resources
COPY .secrets/* /resources/
COPY tokens /tokens

ENTRYPOINT ["java","-jar","/opt/AutoHermod/AutoHermod-assembly-0.1.jar","resources/application.json"]