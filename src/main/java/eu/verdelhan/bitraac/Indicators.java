package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
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

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     * @return the ADL indicator
     */
    public static BigDecimal getAccumulationDistributionLine(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        BigDecimal adl = BigDecimal.ZERO;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
            Period period  = periods.get(i);

            // Getting high, low and close prices
            BigDecimal highPrice = period.getHigh().getPrice().getAmount();
            BigDecimal lowPrice = period.getLow().getPrice().getAmount();
            BigDecimal closePrice = period.getLast().getPrice().getAmount();

            // Calculating the money flow multiplier
            BigDecimal moneyFlowMultiplier = closePrice.subtract(lowPrice).subtract(highPrice.subtract(closePrice)).divide(highPrice.subtract(lowPrice), RoundingMode.HALF_UP);

            // Calculating the money flow volume
            BigDecimal moneyFlowVolume = moneyFlowMultiplier.multiply(getVolume(period));

            // Calculating the ADL
            adl = adl.add(moneyFlowVolume);
        }
        
        return adl;
    }

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
            Trade currentHighTrade = period.getHigh();
            if (currentHighTrade != null) {
                if (highPeriod == null) {
                    highPeriod = period;
                } else {
                    BigMoney highPrice = highPeriod.getHigh().getPrice();
                    if (currentHighTrade.getPrice().isGreaterThan(highPrice)
                            || currentHighTrade.getPrice().isEqual(highPrice)) {
                        // New high price
                        highPeriod = period;
                        nbPeriodsSinceHigh = 0;
                    }
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
            Trade currentLowTrade = period.getLow();
            if (currentLowTrade != null) {
                if (lowPeriod == null) {
                    lowPeriod = period;
                } else {
                    BigMoney lowPrice = lowPeriod.getLow().getPrice();
                    if (currentLowTrade.getPrice().isLessThan(lowPrice)
                            || currentLowTrade.getPrice().isEqual(lowPrice)) {
                        // New low price
                        lowPeriod = period;
                        nbPeriodsSinceLow = 0;
                    }
                }
            }
            nbPeriodsSinceLow++;
        }

        return ((lastPeriods - nbPeriodsSinceLow) / lastPeriods) * 100;
    }

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 14)
     * @return the average true range
     */
    public static double getAverageTrueRange(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
            Period period = periods.get(i);
            double trueRange;
            if (i == firstPeriodIdx) {
                // First period
                trueRange = getTrueRange(null, period);
            } else {
                // Subsequent periods
                Period previousPeriod = periods.get(i - 1);
                trueRange = getTrueRange(previousPeriod, period);
                // TO DO
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }


        return 0;
    }

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     * @return the exponential moving average price of trades
     */
    public static BigDecimal getExponentialMovingAverage(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        lastPeriods = (nbPeriods - lastPeriods) > 0 ? lastPeriods : nbPeriods;

        BigDecimal multiplier = new BigDecimal((double) (2 / ((double) lastPeriods + 1))); // Weighting multiplier

        // Computing EMAs
        int firstPeriodIdx = nbPeriods - lastPeriods;
        // An exponential moving average (EMA) has to start somewhere
        // so a simple moving average is used as the previous period's EMA in the first calculation.
        BigDecimal ema = getSimpleMovingAverage(periods, lastPeriods);
        for (int i = firstPeriodIdx + 1; i < lastPeriods; i++) {
            Trade periodLastTrade = periods.get(i).getLast();
            if (periodLastTrade != null) {
                BigDecimal closePrice = periodLastTrade.getPrice().getAmount();
                ema = closePrice.subtract(ema).multiply(multiplier).add(ema);
            }
        }
        return ema;
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
        BigDecimal shortTermEma = getExponentialMovingAverage(periods, shortTermEmaNbPeriods);
        BigDecimal longTermEma = getExponentialMovingAverage(periods, longTermEmaNbPeriods);

        return shortTermEma.subtract(longTermEma);
    }

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 20)
     * @return the mean deviation
     */
    public static BigDecimal getMeanDeviation(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        // TO DO
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 14)
     * @return the money flow index (MFI)
     */
    public static BigDecimal getMoneyFlowIndex(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        
        // TO DO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @param periods the list of periods
     * @param shortTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for short term EMA computation (e.g. 12)
     * @param longTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for long term EMA computation (e.g. 26)
     * @return the PPO indicator (in percentage terms, i.e. between 0 and 100)
     */
    public static BigDecimal getPercentagePriceOscillator(ArrayList<Period> periods, int shortTermEmaNbPeriods, int longTermEmaNbPeriods) {
        int nbPeriods = periods.size();
        if (longTermEmaNbPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        // Computing exponential moving averages
        BigDecimal shortTermEma = getExponentialMovingAverage(periods, shortTermEmaNbPeriods);
        BigDecimal longTermEma = getExponentialMovingAverage(periods, longTermEmaNbPeriods);

        return shortTermEma.subtract(longTermEma).divide(longTermEma, RoundingMode.HALF_UP).multiply(HUNDRED);
    }

    /**
     * Aka. Momentum
     * The ROC calculation compares the current price with the price "n" periods ago.
     * @param periods the list of periods
     * @param n the current price will be compared with the price "n" (e.g. 12) periods ago
     * @return the rate of change (ROC)
     */
    public static double getRateOfChange(ArrayList<Period> periods, int n) {
        int nbPeriods = periods.size();
        if (n > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        try {
            BigDecimal nPeriodsAgoClosePrice = periods.get(nbPeriods - n).getLast().getPrice().getAmount();
            BigDecimal currentClosePrice = periods.get(nbPeriods - 1).getLast().getPrice().getAmount();

            return currentClosePrice.subtract(nPeriodsAgoClosePrice).divide(nPeriodsAgoClosePrice, RoundingMode.HALF_UP).multiply(HUNDRED).doubleValue();
        } catch (RuntimeException re) {
            return Double.NaN;
        }
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
        BigDecimal rsi = HUNDRED.subtract(HUNDRED.divide(relativeStrength.add(BigDecimal.ONE), RoundingMode.HALF_UP));
        return rsi;
    }

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     * @return the simple moving average price of trades
     */
    public static BigDecimal getSimpleMovingAverage(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        BigDecimal average = BigDecimal.ZERO;
        int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
            Trade periodLastTrade = periods.get(i).getLast();
            if (periodLastTrade == null) {
                // No trade in the period
                lastPeriods--;
            } else {
                average = average.add(periodLastTrade.getPrice().getAmount());
            }
        }
        return average.divide(new BigDecimal(lastPeriods), RoundingMode.HALF_UP);
    }

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 10)
     * @return the standard deviation (volatility)
     */
    public static double getStandardDeviation(ArrayList<Period> periods, int lastPeriods) {
        int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

        // Getting the average close price
        BigDecimal averageClosePrice = getSimpleMovingAverage(periods, lastPeriods);

        int firstPeriodIdx = (nbPeriods - lastPeriods) > 0 ? nbPeriods - lastPeriods : 0;
        double sumOfSquaredDeviations = 0;
        for (int i = firstPeriodIdx; i < nbPeriods; i++) {
            Period period = periods.get(i);
            BigDecimal closePrice = period.getLast().getPrice().getAmount();
            sumOfSquaredDeviations += closePrice.subtract(averageClosePrice).pow(2).doubleValue();
        }

        return Math.sqrt(sumOfSquaredDeviations / (nbPeriods - firstPeriodIdx));
    }

    /**
     * @param previousPeriod the previous period
     * @param currentPeriod the current period
     * @return the true range for the current period
     */
    public static double getTrueRange(Period previousPeriod, Period currentPeriod) {
        if (currentPeriod == null) {
            throw new IllegalArgumentException("Current period should not be null");
        }

        //  Current extrema prices
        BigDecimal currentHighPrice = currentPeriod.getHigh().getPrice().getAmount();
        BigDecimal currentLowPrice = currentPeriod.getLow().getPrice().getAmount();

        double trueRange;
        if (previousPeriod == null) {
            // No previous period
            trueRange = currentHighPrice.subtract(currentLowPrice).doubleValue();
        } else {
            // Using the previous close price
            BigDecimal previousClosePrice = previousPeriod.getLast().getPrice().getAmount();
            BigDecimal trueRangeMethod1 = currentHighPrice.subtract(currentLowPrice);
            BigDecimal trueRangeMethod2 = currentHighPrice.subtract(previousClosePrice).abs();
            BigDecimal trueRangeMethod3 = currentLowPrice.subtract(previousClosePrice).abs();
            trueRange = trueRangeMethod1.max(trueRangeMethod2).max(trueRangeMethod3).doubleValue();
        }
        return trueRange;
    }

    /**
     * @param period the period for which we want the trade volume
     * @return the trade volume (always positive) during the period
     */
    public static BigDecimal getVolume(Period period) {
        BigDecimal volume = BigDecimal.ZERO;
        for (Trade trade : period.getTrades()) {
            volume = volume.add(trade.getTradableAmount().multiply(trade.getPrice().getAmount()));
        }
        return volume;
    }
}
