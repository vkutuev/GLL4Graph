package benchmark;

import apoc.ApocSettings;
import apoc.create.Create;
import apoc.export.csv.ImportCsv;
import apoc.help.Help;
import apoc.periodic.Periodic;
import org.iguana.grammar.Grammar;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.exceptions.KernelException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public abstract class Neo4jBenchmark {

    protected static String RESULTS_DIR = "results";

    // args[0] - benchmarkType
    // args[1] - nodeNumber
    // args[2] - number of warm up iteration
    // args[3] - total number of iterations
    // args[4] - path to dataset (graph)
    // args[5] - path to grammar
    // args[6] - dataset name = name of file with results
    public static void main(String[] args) throws IOException {
        Neo4jBenchmark benchmark = benchmarkByType(args[0], Integer.parseInt(args[1]));
        benchmark.loadGraph(args[4]);
        benchmark.loadGrammar(args[5]);
        benchmark.benchmark(parseSettings(new String[]{args[2], args[3], args[6]}));
        benchmark.removeData();
        benchmark.managementService.shutdown();
    }
    private static Neo4jBenchmark benchmarkByType(String type, int nodesCount) {
        return switch (type) {
            // AP - All Pairs; MS - Multiple Sources
            case "REACHABILITY_AP" -> new ReachabilityBenchmark(nodesCount);
//            case "REACHABILITY_MS" -> new ReachabilityBenchmark();
            case "PATH_AP" -> new PathBenchmark(nodesCount);
//            case "PATH_MS" -> new PathBenchmark();
            default -> throw new IllegalArgumentException("Illegal benchmark type");
        };
    }

    /**
     * @param args array contains string params:
     *             args[0] - warmUpIterations
     *             args[1] - measurementsIterations
     *             args[2] - datasetName
     * @return {@link BenchmarkSettings} instance
     */
    private static BenchmarkSettings parseSettings(String[] args) {
        return new BenchmarkSettings(
                Integer.parseInt(args[0]),
                Integer.parseInt(args[1]),
                args[2]);
    }

    Neo4jBenchmark(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    private final BiFunction<Relationship, Direction, String> relationship2Label =
            (Relationship relationship, Direction direction) ->
                    switch (direction) {
                        case INCOMING -> relationship.getType().name() + "_r";
                        case OUTGOING -> relationship.getType().name();
                        default -> throw new RuntimeException("Unexpected direction");
                    };
    private final File databaseDirectory = new File("target/neo4j-hello-db");
    private GraphDatabaseService graphDb;
    private DatabaseManagementService managementService;
    private Grammar grammar;
    private final int nodesCount;
    public void registerProcedure(GraphDatabaseService graphDb, List<Class<?>> procedures) {
        GlobalProcedures globalProcedures = ((GraphDatabaseAPI) graphDb).getDependencyResolver().resolveDependency(GlobalProcedures.class);
        for (Class<?> procedure : procedures) {
            try {
                globalProcedures.registerProcedure(procedure, true);
                globalProcedures.registerFunction(procedure, true);
                globalProcedures.registerAggregationFunction(procedure, true);
            } catch (KernelException e) {
                throw new RuntimeException("while registering " + procedure, e);
            }
        }
    }

    private void loadGraph(String pathToDataset) throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        managementService =
                new DatabaseManagementServiceBuilder(databaseDirectory)
                        //.setConfig(GraphDatabaseSettings.pagecache_memory, "100G")
                        .setConfig(GraphDatabaseSettings.tx_state_max_off_heap_memory, java.lang.Long.parseLong("24000000000"))
                        .setConfig(GraphDatabaseSettings.pagecache_warmup_enabled, true)
                        .setConfig(GraphDatabaseSettings.procedure_whitelist, List.of("gds.*", "apoc.*", "apoc.import.*"))
                        .setConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("gds.*", "apoc.*"))
                        .setConfig(GraphDatabaseSettings.default_allowed, "gds.*,apoc.*")
                        .setConfig(BoltConnector.enabled, true)
                        .setConfig(ApocSettings.apoc_import_file_enabled, true)
                        .setConfig(ApocSettings.apoc_import_file_use__neo4j__config, false)
                        .build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);

        registerProcedure(graphDb, asList(
                Create.class,
                Help.class,
                ImportCsv.class,
                Periodic.class
        ));

        try (Transaction tx = graphDb.beginTx()) {
            tx.execute("CALL apoc.import.csv(" +
                    "[{fileName: 'FILE:///" + pathToDataset + "nodes.csv', labels: ['Node']}], " +
                    "[{fileName: 'FILE:///" + pathToDataset + "edges.csv'}], " +
                    "{delimiter: ' ', stringIds: false}" +
                    ")");
            tx.commit();
        }
        System.out.println("Graph loaded");
    }

    private void loadGrammar(String pathToGrammar) {
        try {
            grammar = Grammar.load(pathToGrammar, "json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No grammar file is present");
        }
    }

    protected abstract void benchmark(BenchmarkSettings settings) throws IOException;

    private void removeData() {
        try (Transaction tx = graphDb.beginTx()) {
            tx.getAllNodes().forEach(node -> {
                node.getRelationships().forEach(Relationship::delete);
                node.delete();
            });
            tx.commit();
        }
    }

    protected BiFunction<Relationship, Direction, String> getRelationship2Label() {
        return relationship2Label;
    }

    protected GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    protected Grammar getGrammar() {
        return grammar;
    }

    protected int getNodesCount() {
        return nodesCount;
    }
}