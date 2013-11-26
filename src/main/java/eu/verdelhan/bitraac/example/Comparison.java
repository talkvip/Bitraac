package eu.verdelhan.bitraac.example;

import eu.verdelhan.bitraac.AlgorithmComparator;

public class Comparison {

    public static void main(String[] args) {

        AlgorithmComparator ac = new AlgorithmComparator(1000, 0.5);
        ac.compare(new NoAlgorithmAlgorithm(),
                new DummyBinaryAlgorithm(),
                new EnhancedBinaryAlgorithm(),
                new SimpleMovingAveragesAlgorithm(),
                new ExponentialMovingAveragesAlgorithm());

    }
}
