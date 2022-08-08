package benchmark;

import iguana.utils.input.Neo4jBenchmarkInput;
import org.eclipse.collections.impl.list.Interval;
import org.iguana.grammar.Grammar;
import org.iguana.parser.IguanaParser;
import org.iguana.parser.Pair;
import org.iguana.sppf.NonterminalNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class PathBenchmark extends Neo4jBenchmark{

    private static final String RESULT_FILE_SUFFIX = "_all_path.csv";
    private static final String CSV_FORMAT = "%.3f,%.3f,%d%n";
    private static final String CSV_HEADER = "prepare_time,run_time,answer";

    public PathBenchmark(int nodesCount) {
        super(nodesCount);
    }

    @Override
    protected void benchmark(BenchmarkSettings settings) throws IOException {
        final Grammar grammar = getGrammar();

        File outFile = new File(RESULTS_DIR + File.separator + settings.getDatasetName() + RESULT_FILE_SUFFIX);
        boolean fileExists = !outFile.createNewFile();
        System.out.printf("Benchmarking reachability for %s graph%n", settings.getDatasetName());
        final int nodesCount = getNodesCount();
        try (PrintWriter outStatsTime = new PrintWriter(new FileOutputStream(outFile, true), true)) {
            if (!fileExists) {
                outStatsTime.printf("%s%n", CSV_HEADER);
            }

            final int warmUpIters = settings.getWarmUpIterations();
            final int maxIters = warmUpIters + settings.getMeasurementsIterations();
            for (int iter = 0; iter < maxIters; ++iter) {
                final long stepStartPrepareTime = System.nanoTime();
                IguanaParser parser = new IguanaParser(grammar);
                Neo4jBenchmarkInput input = new Neo4jBenchmarkInput(getGraphDb(),
                        getRelationship2Label(),
                        Interval.zeroTo(nodesCount - 1).stream(),
                        nodesCount);
                final long stepStartTime = System.nanoTime();
                final double stepPrepareTime = (double)(stepStartTime - stepStartPrepareTime) / 1_000_000_000.;
                Map<Pair, NonterminalNode> parseResults = parser.getSPPF(input);
                final long stepStopTime = System.nanoTime();
                final double stepRunTime = (double)(stepStopTime - stepStartTime) / 1_000_000_000.;

                input.close();

                if (iter >= warmUpIters && parseResults != null) {
                    outStatsTime.printf(CSV_FORMAT, stepPrepareTime, stepRunTime, parseResults.size());
                }
            }
        }
    }
}
