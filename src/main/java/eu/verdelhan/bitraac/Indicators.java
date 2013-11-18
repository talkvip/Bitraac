package eu.verdelhan.bitraac;

import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * See:
 *   - http://bitcoincharts.com/charts/
 *   - http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators
 */
public class Indicators {

	/**
	 * @param periods the list of periods
	 * @param lastPeriods the number of periods to use (i.e. the n last periods)
	 * @return the Aroon Up indicator
	 */
	public static BigDecimal getAroonUp(ArrayList<Period> periods, int lastPeriods) {
		int nbPeriods = periods.size();
        if (lastPeriods >= nbPeriods) {
            throw new IllegalArgumentException("Not enough periods");
        }

		// TO DO
		return null;
	}

}
