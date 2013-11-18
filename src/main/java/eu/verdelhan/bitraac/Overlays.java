package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * See:
 *   - http://bitcoincharts.com/charts/
 *   - http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators
 */
public class Overlays {

	/**
	 * @param lastValues the number of values to use (i.e. the n last values)
	 * @return the moving average price of trades
	 */
    public static BigDecimal getSimpleMovingAverage(int lastValues) {
        int nbValues = ExchangeMarket.getAllTrades().size();
        if (lastValues >= nbValues) {
            throw new IllegalArgumentException("Not enough values");
        }

        BigDecimal average = new BigDecimal(0);
        int firstValueIndex = (nbValues - lastValues) > 0 ? nbValues - lastValues : 0;
        for (int i = firstValueIndex; i < nbValues; i++) {
            average = average.add(ExchangeMarket.getAllTrades().get(i).getPrice().getAmount());
        }
        return average.divide(new BigDecimal(lastValues), RoundingMode.HALF_UP);
    }

	/**
	 * @param period the period for which we want the trade volume
	 * @return the trade volume during the period
	 */
	public static BigDecimal getVolume(Period period) {
		BigDecimal volume = BigDecimal.ZERO;
		for (Trade trade : period.getTrades()) {
			volume = volume.add(trade.getPrice().getAmount());
		}
		return volume;
	}

	/**
	 * @param timestamp the date for which we want the trade volume
	 * @return the trade volume at timestamp
	 */
	public static BigDecimal getVolume(Date timestamp) {
		BigDecimal volume = BigDecimal.ZERO;
		for (Trade trade : ExchangeMarket.getAllTrades()) {
			if (timestamp.equals(trade.getTimestamp())) {
				volume = volume.add(trade.getPrice().getAmount());
			}
		}
		return volume;
	}
}
