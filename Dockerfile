FROM eclipse-temurin:21-alpine

RUN mkdir -p /para/lib

WORKDIR /para

ENV PARA_PLUGIN_ID="para-cache-hazelcast" \
	PARA_PLUGIN_VER="1.41.3"

ADD https://oss.sonatype.org/service/local/repositories/releases/content/com/erudika/$PARA_PLUGIN_ID/$PARA_PLUGIN_VER/$PARA_PLUGIN_ID-$PARA_PLUGIN_VER-shaded.jar /para/lib/
