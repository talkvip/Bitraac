package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

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

	/**
	 * @param timestamp the date for which we want the trade volume
	 * @return the trade volume at timestamp
	 */
	public static BigDecimal getVolume(Date timestamp) {
		BigDecimal volume = BigDecimal.ZERO;
		for (Trade trade : TRADES) {
			if (timestamp.equals(trade.getTimestamp())) {
				volume = volume.add(trade.getPrice().getAmount());
			}
		}
		return volume;
	}

}
