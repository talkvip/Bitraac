package eu.verdelhan.bitraac;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
		int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
			Period period  = periods.get(i);
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
		int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
			Period period  = periods.get(i);
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
	 * @param shortTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for short term EMA computation (e.g. 12)
	 * @param longTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for long term EMA computation (e.g. 26)
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

	/**
	 * Aka. Momentum
	 * The ROC calculation compares the current price with the price "n" periods ago.
	 * @param periods the list of periods
	 * @param n the current price will be compared with the price "n" (e.g. 12) periods ago
	 * @return the rate of change (ROC)
	 */
	public static BigDecimal getRateOfChange(ArrayList<Period> periods, int n) {
		int nbPeriods = periods.size();
        if (n > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

		BigDecimal nPeriodsAgoClosePrice = periods.get(nbPeriods - 1 - n).getLast().getPrice().getAmount();
		BigDecimal currentClosePrice = periods.get(nbPeriods - 1).getLast().getPrice().getAmount();

		return currentClosePrice.subtract(nPeriodsAgoClosePrice).divide(nPeriodsAgoClosePrice, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
	}

	/**
	 * @param periods the list of periods
	 * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 14)
	 * @return the relative strength index (RSI)
	 */
	public static BigDecimal getRelativeStrengthIndex(ArrayList<Period> periods, int lastPeriods) {
		int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
		lastPeriods = (nbPeriods - lastPeriods) > 0 ? lastPeriods : nbPeriods;

		// Computing gains and losses
		ArrayList<BigDecimal> gains = new ArrayList<BigDecimal>();
		gains.add(BigDecimal.ZERO);
		ArrayList<BigDecimal> losses = new ArrayList<BigDecimal>();
		losses.add(BigDecimal.ZERO);
		int firstPeriodIdx = nbPeriods - lastPeriods;
        for (int i = firstPeriodIdx + 1; i < lastPeriods; i++) {
			Period previousPeriod  = periods.get(i - 1);
			Period currentPeriod  = periods.get(i);
			BigMoney previousClosePrice = previousPeriod.getLast().getPrice();
			BigMoney currentClosePrice = currentPeriod.getLast().getPrice();

			if (previousClosePrice.isLessThan(currentClosePrice)) {
				// Gain
				gains.add(currentClosePrice.getAmount().subtract(previousClosePrice.getAmount()));
				losses.add(BigDecimal.ZERO);
			} else if (previousClosePrice.isGreaterThan(currentClosePrice)) {
				// Loss
				gains.add(BigDecimal.ZERO);
				losses.add(previousClosePrice.getAmount().subtract(currentClosePrice.getAmount()));
			} else {
				// Neither gain nor loss
				gains.add(BigDecimal.ZERO);
				losses.add(BigDecimal.ZERO);
			}
		}

		// Sums of gains and losses
		BigDecimal sumOfGains = BigDecimal.ZERO;
		BigDecimal sumOfLosses = BigDecimal.ZERO;
		for (int i = 0; i < gains.size(); i++) {
			sumOfGains = sumOfGains.add(gains.get(i));
			sumOfLosses = sumOfLosses.add(losses.get(i));
		}

		// Computing average gains and average losses
		ArrayList<BigDecimal> averageGains = new ArrayList<BigDecimal>();
		ArrayList<BigDecimal> averageLosses = new ArrayList<BigDecimal>();
		BigDecimal nbPeriodsDivider = new BigDecimal(lastPeriods);
		BigDecimal nbPeriodsMinusOne = nbPeriodsDivider.subtract(BigDecimal.ONE);

		// First average gain and first average loss
		averageGains.add(sumOfGains.divide(nbPeriodsDivider, RoundingMode.HALF_UP));
		averageLosses.add(sumOfLosses.divide(nbPeriodsDivider, RoundingMode.HALF_UP));
		// Subsequent "average gain" and "average loss" values
		for (int i = firstPeriodIdx + 1; i < lastPeriods; i++) {
			BigDecimal previousAverageGain = averageGains.get(i - 1);
			BigDecimal previousAverageLoss = averageLosses.get(i - 1);
			averageGains.add(previousAverageGain.multiply(nbPeriodsMinusOne).add(gains.get(i)).divide(nbPeriodsDivider, RoundingMode.HALF_UP));
			averageLosses.add(previousAverageLoss.multiply(nbPeriodsMinusOne).add(losses.get(i)).divide(nbPeriodsDivider, RoundingMode.HALF_UP));
		}

		// Relative strength
		BigDecimal relativeStrength = averageGains.get(lastPeriods - 1).divide(averageLosses.get(lastPeriods - 1), RoundingMode.HALF_UP);

		// Relative strength index
		BigDecimal hundred = new BigDecimal(100);
		BigDecimal rsi = hundred.subtract(hundred.divide(relativeStrength.add(BigDecimal.ONE), RoundingMode.HALF_UP));
		return rsi;
	}
}
