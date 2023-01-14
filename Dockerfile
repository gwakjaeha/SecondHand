ARG ELK_VERSION
FROM docker.elastic.co/elasticsearch/elasticsearch:7.17.8
RUN elasticsearch-plugin install analysis-nori
