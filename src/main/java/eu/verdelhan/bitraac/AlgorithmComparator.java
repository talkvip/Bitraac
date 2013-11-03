package eu.verdelhan.bitraac;

import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;

public class AlgorithmComparator {

    public void compare(TradingAlgorithm... algorithms) {
        for (TradingAlgorithm algorithm : algorithms) {
            System.out.println(algorithm);
        }
    }
}
