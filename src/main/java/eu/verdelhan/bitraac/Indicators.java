package eu.verdelhan.bitraac;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.xeiam.xchange.dto.marketdata.Trade;

/**
 * See http://bitcoincharts.com/charts/
 */
public class Indicators {

    private static final ArrayList<Trade> TRADES = AlgorithmComparator.getLocalTrades();

    public static BigDecimal getMovingAverage(int lastValues) {
        int nbValues = TRADES.size();
        if (lastValues >= nbValues) {
            throw new IllegalArgumentException("Not enough values");
        }

        BigDecimal average = new BigDecimal(0);
        int firstValueIndex = (nbValues - lastValues) > 0 ? nbValues - lastValues : 0;
        for (int i = firstValueIndex; i < nbValues; i++) {
            average = average.add(TRADES.get(i).getPrice().getAmount());
        }
        return average.divide(new BigDecimal(lastValues), RoundingMode.HALF_UP);
    }

}
