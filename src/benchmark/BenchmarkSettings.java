package benchmark;

public class BenchmarkSettings {
    private final int warmUpIterations;
    private final int measurementsIterations;
    private final String datasetName;

    public BenchmarkSettings(int warmUpIterations, int measurementsIterations, String datasetName) {
        this.warmUpIterations = warmUpIterations;
        this.measurementsIterations = measurementsIterations;
        this.datasetName = datasetName;
    }

    public int getWarmUpIterations() { return warmUpIterations; }

    public int getMeasurementsIterations() {
        return measurementsIterations;
    }

    public String getDatasetName() {
        return datasetName;
    }
}
