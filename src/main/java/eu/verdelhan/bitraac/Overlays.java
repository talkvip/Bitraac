package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

/**
 * See:
 *   - http://bitcoincharts.com/charts/
 *   - http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators
 */
public class Overlays {

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

        BigDecimal average = new BigDecimal(0);
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
            BigDecimal closePrice = periods.get(i).getLast().getPrice().getAmount();
            ema = closePrice.subtract(ema).multiply(multiplier).add(ema);
        }
        return ema;
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
