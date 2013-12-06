package eu.verdelhan.bitraac.indicators;

/**
 * A technical indicator.
 *
 * See:
 *   - http://bitcoincharts.com/charts/
 *   - http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators
 *
 * @param <T> the data type of the indicator
 */
public interface Indicator<T> {

    /**
     * Execute the computation of the indicator.
     * @return the indicator value
     */
    T execute();
}
