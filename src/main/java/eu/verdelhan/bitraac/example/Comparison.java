package eu.verdelhan.bitraac.example;

import eu.verdelhan.bitraac.AlgorithmComparator;

public class Comparison {

    public static void main(String[] args) {

        AlgorithmComparator ac = new AlgorithmComparator();
        ac.compare(new NoAlgorithmAlgorithm(1000, 0.5),
                new BinaryAlgorithm(1000, 0.5),
                new SimpleMovingAveragesAlgorithm(1000, 0.5),
                new PPOAlgorithm(1000, 0.5),
                new MultiIndicatorsAlgorithm(1000, 0.5));

    }
}
