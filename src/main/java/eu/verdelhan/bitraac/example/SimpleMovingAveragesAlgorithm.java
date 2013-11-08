package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class SimpleMovingAveragesAlgorithm extends TradingAlgorithm {

	@Override
	public boolean isEnoughData()
	{
		return getPreviousChartData().size() > 10;
	}

	@Override
    public Order placeOrder() {
		Order order;
		double trendCoef = getTrendCoef();
		if (trendCoef > 1.02) {
			// Up trend
			order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(2), Currencies.BTC, Currencies.USD);
		} else if (trendCoef < 0.99) {
			// Down trend
			order = new MarketOrder(Order.OrderType.BID, new BigDecimal(2), Currencies.BTC, Currencies.USD);
		} else {
			// Stability
			order = null;
		}
        return order;
    }

	private double getTrendCoef() {
		double trendCoef = 1.0;
		if (isEnoughData()) {
			double movingAvg10 = getMovingAverage(10);
			double movingAvg3 = getMovingAverage(3);
			trendCoef = movingAvg3 / movingAvg10;
			//System.out.println("avg10="+movingAvg10+"  avg3="+movingAvg3+"  coef="+trendCoef);
		}
		return trendCoef;
	}

	private double getMovingAverage(int lastValues) {
		ArrayList<ChartData> data = getPreviousChartData();
		int nbValues = getPreviousChartData().size();
		assert lastValues >= nbValues : "Not enough values";

		BigDecimal average = new BigDecimal(0);
		int firstValueIndex = (nbValues-lastValues) > 0 ? nbValues-lastValues : 0;
		for (int i = firstValueIndex; i < nbValues; i++) {
			average = average.add(data.get(i).getWeightedPrice());
		}
		return average.divide(new BigDecimal(lastValues), RoundingMode.HALF_UP).doubleValue();
	}

}
