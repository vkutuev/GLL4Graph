package iguana.utils.input;

import org.neo4j.graphdb.*;

import java.io.Closeable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Neo4jBenchmarkInput extends Neo4jGraphInput
//        implements Closeable
{
    private final GraphDatabaseService graphDb;
    private final BiFunction<Relationship, Direction, String> toLabel;
    private final int start;
    Transaction tx;

    public Neo4jBenchmarkInput(GraphDatabaseService graphDb, BiFunction<Relationship, Direction, String> toLabel, int start) {
        super(graphDb);
        this.graphDb = graphDb;
        this.toLabel = toLabel;
        this.start = start;
        this.tx = graphDb.beginTx();
    }

    public long nVertices() {
        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.getAllNodes().stream().count();
        }
    }

    @Override
    public List<Integer> nextSymbols(int index) {
        List<Integer> result = new ArrayList<>();

        if (isFinal(index)) {
            result.add(EOF);
        }
        Iterable<Relationship> relationships = graphDb.getNodeById(index).getRelationships();
        for (Relationship rel : relationships) {
            String tmp = toLabel.apply(
                    rel,
                    rel.getStartNodeId() == index ? Direction.OUTGOING : Direction.INCOMING
            );
            if (tmp != null) {
                result.add((int) tmp.charAt(0));
            }
        }
//        tx.getNodeById(index).getRelationships()
//                .forEach(rel -> {
//                    String tmp = toLabel.apply(
//                            rel,
//                            rel.getStartNodeId() == index ? Direction.OUTGOING : Direction.INCOMING
//                    );
//                    if (tmp != null) {
//                        result.add((int) tmp.charAt(0));
//                    }
//                });

        return result;
    }

    @Override
    public boolean isFinal(int index) {
        return true;
    }

    @Override
    public List<Integer> getStartVertices() {
        List<Integer> result = new ArrayList<>();
//        Iterable<Node> nodes = tx.getAllNodes();
        for (Node node: graphDb.getAllNodes()) {
            long id = node.getId();
            if (id == start) {
                result.add((int) id);
            }
        }
        return result;
//        return tx.getAllNodes().stream()
//                .filter(node -> node.getId() == start)
//                .map(node -> (int) node.getId())
//                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getFinalVertices() {
        List<Integer> result = new ArrayList<>();
        graphDb.getAllNodes().forEach(node -> {
            result.add((int) node.getId());
        });
        return result;
//        return tx.getAllNodes().stream()
//                .map(node -> (int) node.getId())
//                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getDestVertex(int v, String t) {
        return StreamSupport.stream(graphDb.getNodeById(v).getRelationships().spliterator(), false)
                .map(rel -> {
                    final Direction direction = rel.getStartNodeId() == v
                            ? Direction.OUTGOING
                            : Direction.INCOMING;

                    String tmp = toLabel.apply(rel, direction);
                    if (tmp != null && tmp.equals(t)) {
                        return direction == Direction.INCOMING
                                ? (int) rel.getStartNode().getId()
                                : (int) rel.getEndNode().getId();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        tx.close();
    }
}
