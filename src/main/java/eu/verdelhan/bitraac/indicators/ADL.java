package eu.verdelhan.bitraac.indicators;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Accumulation distribution line (ADL) indicator.
 */
public class ADL implements Indicator<BigDecimal> {

    private ArrayList<Period> periods;

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     */
    public ADL(final List<Period> periods, int lastPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - lastPeriods, nbPeriods));
    }
    
    /**
     * @return the ADL indicator
     */
    @Override
    public BigDecimal execute() {
        BigDecimal adl = BigDecimal.ZERO;
        for (Period period : periods) {

            // Getting high, low and close prices
            BigDecimal highPrice = period.getHigh().getPrice().getAmount();
            BigDecimal lowPrice = period.getLow().getPrice().getAmount();
            BigDecimal closePrice = period.getLast().getPrice().getAmount();

            // Calculating the money flow multiplier
            BigDecimal moneyFlowMultiplier = closePrice.subtract(lowPrice).subtract(highPrice.subtract(closePrice)).divide(highPrice.subtract(lowPrice), RoundingMode.HALF_UP);

            // Calculating the money flow volume
            BigDecimal moneyFlowVolume = moneyFlowMultiplier.multiply(new Volume(period).execute());

            // Calculating the ADL
            adl = adl.add(moneyFlowVolume);
        }

        return adl;
    }
}