package eu.verdelhan.bitraac.indicators;

import com.xeiam.xchange.dto.marketdata.Trade;
import eu.verdelhan.bitraac.data.Period;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.joda.money.BigMoney;

/**
 * Aroon down indicator.
 */
public class AroonDown implements Indicator<Double> {

    private ArrayList<Period> periods;

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     */
    public AroonDown(final List<Period> periods, int lastPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - lastPeriods, nbPeriods));
    }
    
    /**
     * @return the Aroon Down indicator (in percentage terms, i.e. between 0 and 100)
     */
    @Override
    public Double execute() {
        // Getting the number of periods since the low price
        Period lowPeriod = null;
        int nbPeriodsSinceLow = 0;
        for (Period period : periods) {
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

        return ((periods.size() - nbPeriodsSinceLow) / periods.size()) * 100.0;
    }
}