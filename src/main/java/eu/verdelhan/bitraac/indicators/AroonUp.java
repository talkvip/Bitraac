package eu.verdelhan.bitraac.indicators;

import com.xeiam.xchange.dto.marketdata.Trade;
import eu.verdelhan.bitraac.data.Period;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.joda.money.BigMoney;

/**
 * Aroon up indicator.
 */
public class AroonUp implements Indicator<Double> {

    private ArrayList<Period> periods;

    /**
     * @param periods the list of periods
     * @param lastPeriods the number of periods to use (i.e. the n last periods)
     */
    public AroonUp(final List<Period> periods, int lastPeriods) {
        Validate.noNullElements(periods, "List of periods is null or contains null periods");
        final int nbPeriods = periods.size();
        if (lastPeriods > nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }
        this.periods = new ArrayList<Period>(periods.subList(nbPeriods - lastPeriods, nbPeriods));
    }
    
    /**
     * @return the Aroon Up indicator (in percentage terms, i.e. between 0 and 100)
     */
    @Override
    public Double execute() {
        // Getting the number of periods since the high price
        Period highPeriod = null;
        int nbPeriodsSinceHigh = 0;
        for (Period period : periods) {
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

        return ((periods.size() - nbPeriodsSinceHigh) / periods.size()) * 100.0;
    }
}