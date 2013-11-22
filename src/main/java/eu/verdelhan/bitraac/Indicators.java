package eu.verdelhan.bitraac;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.joda.money.BigMoney;

/**
 * See:
 *   - http://bitcoincharts.com/charts/
 *   - http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators
 */
public class Indicators {

	/**
	 * @param periods the list of periods
	 * @param lastPeriods the number of periods to use (i.e. the n last periods)
	 * @return the Aroon Up indicator (in percentage terms, i.e. between 0 and 100)
	 */
	public static double getAroonUp(ArrayList<Period> periods, int lastPeriods) {
		int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

		// Getting the number of periods since the high price
		Period highPeriod = null;
		int nbPeriodsSinceHigh = 0;
		for (Period period : periods) {
			if (highPeriod == null) {
				highPeriod = period;
			} else {
				BigMoney highPrice = highPeriod.getHigh().getPrice();
				BigMoney currentPrice = period.getHigh().getPrice();
				if (currentPrice.isGreaterThan(highPrice)
						|| currentPrice.isEqual(highPrice)) {
					// New high price
					highPeriod = period;
					nbPeriodsSinceHigh = 0;
				}
			}
			nbPeriodsSinceHigh++;
		}

		return ((lastPeriods - nbPeriodsSinceHigh) / lastPeriods) * 100;
	}

	/**
	 * @param periods the list of periods
	 * @param lastPeriods the number of periods to use (i.e. the n last periods)
	 * @return the Aroon Down indicator (in percentage terms, i.e. between 0 and 100)
	 */
	public static double getAroonDown(ArrayList<Period> periods, int lastPeriods) {
		int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

		// Getting the number of periods since the low price
		Period lowPeriod = null;
		int nbPeriodsSinceLow = 0;
		for (Period period : periods) {
			if (lowPeriod == null) {
				lowPeriod = period;
			} else {
				BigMoney lowPrice = lowPeriod.getLow().getPrice();
				BigMoney currentPrice = period.getLow().getPrice();
				if (currentPrice.isLessThan(lowPrice)
						|| currentPrice.isEqual(lowPrice)) {
					// New low price
					lowPeriod = period;
					nbPeriodsSinceLow = 0;
				}
			}
			nbPeriodsSinceLow++;
		}

		return ((lastPeriods - nbPeriodsSinceLow) / lastPeriods) * 100;
	}

	/**
	 * @param periods the list of periods
	 * @param shortTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for short term EMA computation
	 * @param longTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for long term EMA computation
	 * @return the MACD indicator
	 */
	public static BigDecimal getMacd(ArrayList<Period> periods, int shortTermEmaNbPeriods, int longTermEmaNbPeriods) {
		int nbPeriods = periods.size();
        if (longTermEmaNbPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

		// Computing exponential moving averages
		BigDecimal shortTermEma = Overlays.getExponentialMovingAverage(periods, shortTermEmaNbPeriods);
		BigDecimal longTermEma = Overlays.getExponentialMovingAverage(periods, longTermEmaNbPeriods);

		return shortTermEma.subtract(longTermEma);
	}
}
