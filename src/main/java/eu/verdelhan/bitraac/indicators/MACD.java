package eu.verdelhan.bitraac.indicators;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Moving average convergence divergence (MACD) indicator.
 */
public class MACD implements Indicator<BigDecimal> {

    private ArrayList<Period> periods;

    private int shortTermEmaNbPeriods;

    /**
     * @param periods the list of periods
     * @param shortTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for short term EMA computation (e.g. 12)
     * @param longTermEmaNbPeriods the number of periods to use (i.e. the n last periods) for long term EMA computation (e.g. 26)
     */
    public MACD(final List<Period> periods, int shortTermEmaNbPeriods, int longTermEmaNbPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (longTermEmaNbPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        if (shortTermEmaNbPeriods > longTermEmaNbPeriods) {
            throw new IllegalArgumentException("Long term period count must be greater than short term period count");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - longTermEmaNbPeriods, nbPeriods));
        this.shortTermEmaNbPeriods = shortTermEmaNbPeriods;
    }
    
    /**
     * @return the MACD indicator
     */
    @Override
    public BigDecimal execute() {
        // Computing exponential moving averages
        BigDecimal shortTermEma = new EMA(periods, shortTermEmaNbPeriods).execute();
        BigDecimal longTermEma = new EMA(periods, periods.size()).execute();

        return shortTermEma.subtract(longTermEma);
    }
}