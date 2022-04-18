FROM ubuntu:20.10

RUN apt update && apt -y upgrade
RUN apt install -y libopencv-dev python3-opencv openjdk-11-jre

COPY ./target/scala-2.12/alys-assembly-0.3.jar /opt/alys/

COPY resources /resources
COPY .secrets/* /.secrets/
COPY tokens /tokens

ENTRYPOINT ["java","-jar","/opt/alys/alys-assembly-0.2.jar",".secrets/application.json"]