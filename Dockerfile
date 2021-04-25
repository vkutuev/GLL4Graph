FROM maven:3-openjdk-8

COPY . /iguana

WORKDIR /iguana

RUN mvn clean install -DskipTests
RUN mvn exec:java -Dexec.mainClass="benchmark.Neo4jBenchmark" -Dexec.args="bt 1122 1 2 enzyme.txt test/resources/grammars/graph/Test2/grammar.json enzyme" -Dexec.cleanupDaemonThreads=false
