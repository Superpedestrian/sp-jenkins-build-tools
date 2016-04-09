FROM java:8

RUN apt-get update &&\
    apt-get install shellcheck
RUN mkdir -p /app/build
# Depriv and do project install
COPY . /app
ARG USER_ID=1000
RUN groupadd sp && \
    adduser --disabled-password --gecos "" --uid ${USER_ID} --ingroup sp sp && \
    chown -R sp:sp /app
USER sp

WORKDIR /app
RUN ./gradlew tasks
CMD bash -c 'set -e; shellcheck -s bash shell/* && ./gradlew check'
