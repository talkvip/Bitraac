package eu.verdelhan.bitraac.indicators;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Standard deviation indicator.
 */
public class StandardDeviation implements Indicator<Double> {

    private ArrayList<Period> periods;

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods) (e.g. 10)
     */
    public StandardDeviation(final List<Period> periods, int lastPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - lastPeriods, nbPeriods));
    }
    
    /**
     * @return the standard deviation (volatility)
     */
    @Override
    public Double execute() {
        // Getting the average close price
        int nbPeriods = periods.size();
        BigDecimal averageClosePrice = new SMA(periods, nbPeriods).execute();

        double sumOfSquaredDeviations = 0;
        for (Period period : periods) {
            BigDecimal closePrice = period.getLast().getPrice().getAmount();
            sumOfSquaredDeviations += closePrice.subtract(averageClosePrice).pow(2).doubleValue();
        }

        return Math.sqrt(sumOfSquaredDeviations / nbPeriods);
    }
}