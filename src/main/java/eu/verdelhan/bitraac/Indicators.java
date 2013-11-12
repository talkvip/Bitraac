package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * See http://bitcoincharts.com/charts/
 */
public class Indicators {

    private static final ArrayList<Trade> TRADES = AlgorithmComparator.getLocalTrades();

	/**
	 * @param lastValues the number of values to use (i.e. the n last values)
	 * @return the moving average price of trades
	 */
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
