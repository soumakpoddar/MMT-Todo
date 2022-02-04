# Docker build using fatjar
# - docker build -t example/mmt_induction .
# - docker run -t -i -p 8888:8888 example/mmt_induction

FROM openjdk:17-oracle

ENV FAT_JAR MMT-Induction-1.0.0-SNAPSHOT-fat.jar
ENV APP_HOME /usr/app

EXPOSE 8888

COPY target/$FAT_JAR $APP_HOME/

WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $FAT_JAR"]
