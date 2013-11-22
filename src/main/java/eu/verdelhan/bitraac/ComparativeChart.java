package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.SwingWrapper;
import java.util.ArrayList;
import java.util.Date;

public class ComparativeChart {

    private static Chart CHART = new Chart(800, 600);

    public static void addTradeSeries(String seriesName, ArrayList<Trade> trades) {
        ArrayList<Date> dates = new ArrayList<Date>();
        ArrayList<Number> prices = new ArrayList<Number>();
        for (Trade trade : trades) {
            dates.add(trade.getTimestamp());
            prices.add(trade.getPrice().getAmount());
        }
        CHART.addDateSeries(seriesName, dates, prices);
    }

    public static void show() {
        new SwingWrapper(CHART).displayChart();
    }

}
