package iguana.utils.input;

import org.neo4j.graphdb.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class Neo4jGraphInput extends GraphInput {
    private final GraphDatabaseService graphDb;
    private static final Label START_STATUS = Label.label("start");
    private static final Label FINAL_STATUS = Label.label("final");
    private static final String TAG = "tag";


    public Neo4jGraphInput(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public List<Integer> nextSymbols(int index) {
        try (Transaction tx = graphDb.beginTx()) {
            List<Integer> nextSymbols = StreamSupport.stream(graphDb.getNodeById(index).getRelationships(Direction.OUTGOING).spliterator(), false)
                    .map(rel -> (int) ((String) rel.getProperty(TAG)).charAt(0))
                    .collect(Collectors.toList());

            if (isFinal(index)) {
                nextSymbols.add(EOF);
            }
            return nextSymbols;
        }
    }

//    public boolean nVertices() {
//        try (Transaction tx = graphDb.beginTx()) {
//            return tx.getAllNodes().stream().map(Entity::getId).allMatch(i -> (i >= 0 && i < 225135));
//        }
//    }

    @Override
    public boolean isFinal(int index) {
        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.getNodeById(index).hasLabel(FINAL_STATUS);
        }
    }

    @Override
    public List<Integer> getStartVertices() {
        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.getAllNodes().stream()
                    .filter(node -> node.hasLabel(START_STATUS))
                    .map(node -> (int)node.getId())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Integer> getFinalVertices() {
        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.getAllNodes().stream()
                    .filter(node -> node.hasLabel(FINAL_STATUS))
                    .map(node -> (int)node.getId())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Integer> getDestVertex(int v, String t) {
        try (Transaction tx = graphDb.beginTx()) {
            return StreamSupport.stream(graphDb.getNodeById(v).getRelationships(Direction.OUTGOING).spliterator(), false)
                    .filter(edge -> edge.getProperty(TAG).equals(t))
                    .map(edge -> (int)edge.getEndNode().getId())
                    .collect(Collectors.toList());
        }
    }

    public abstract void close();
}
