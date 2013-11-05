package eu.verdelhan.bitraac.example;

import eu.verdelhan.bitraac.AlgorithmComparator;
import java.math.BigDecimal;

public class Comparison {

    public static void main(String[] args) {

		BigDecimal initialBalance = new BigDecimal(100);
        AlgorithmComparator ac = new AlgorithmComparator(initialBalance);
		ac.getData();

    }
}
