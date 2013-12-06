package eu.verdelhan.bitraac.indicators;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.joda.money.BigMoney;

/**
 * Relative strength index (RSI) indicator.
 */
public class RSI implements Indicator<BigDecimal> {

    private ArrayList<Period> periods;

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 14)
     */
    public RSI(final List<Period> periods, int lastPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - lastPeriods, nbPeriods));
    }
    
    /**
     * @return the relative strength index
     */
    @Override
    public BigDecimal execute() {
        int nbPeriods = periods.size();

        // Computing gains and losses
        ArrayList<BigDecimal> gains = new ArrayList<BigDecimal>();
        gains.add(BigDecimal.ZERO);
        ArrayList<BigDecimal> losses = new ArrayList<BigDecimal>();
        losses.add(BigDecimal.ZERO);
        for (int i = 1; i < nbPeriods; i++) {
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
        BigDecimal nbPeriodsDivider = new BigDecimal(nbPeriods);
        BigDecimal nbPeriodsMinusOne = nbPeriodsDivider.subtract(BigDecimal.ONE);

        // First average gain and first average loss
        averageGains.add(sumOfGains.divide(nbPeriodsDivider, RoundingMode.HALF_UP));
        averageLosses.add(sumOfLosses.divide(nbPeriodsDivider, RoundingMode.HALF_UP));
        // Subsequent "average gain" and "average loss" values
        for (int i = 1; i < nbPeriods; i++) {
            BigDecimal previousAverageGain = averageGains.get(i - 1);
            BigDecimal previousAverageLoss = averageLosses.get(i - 1);
            averageGains.add(previousAverageGain.multiply(nbPeriodsMinusOne).add(gains.get(i)).divide(nbPeriodsDivider, RoundingMode.HALF_UP));
            averageLosses.add(previousAverageLoss.multiply(nbPeriodsMinusOne).add(losses.get(i)).divide(nbPeriodsDivider, RoundingMode.HALF_UP));
        }

        // Relative strength
        BigDecimal relativeStrength = averageGains.get(nbPeriods - 1).divide(averageLosses.get(nbPeriods - 1), RoundingMode.HALF_UP);

        // Relative strength index
        BigDecimal rsi = IndicatorUtils.HUNDRED.subtract(IndicatorUtils.HUNDRED.divide(relativeStrength.add(BigDecimal.ONE), RoundingMode.HALF_UP));
        return rsi;
    }
}