FROM alpine:latest

RUN apk --no-cache add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN apk add maven
RUN mkdir AtlasSQL

COPY . .

RUN mvn clean compile
