package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;
import java.util.ArrayList;

public class DummyBinaryAlgorithm extends TradingAlgorithm {

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
			order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(10), Currencies.BTC, Currencies.USD);
		} else if (trendCoef < 0.98) {
			// Down trend
			order = new MarketOrder(Order.OrderType.BID, new BigDecimal(10), Currencies.BTC, Currencies.USD);
		} else {
			// Stability
			order = null;
		}
        return order;
    }

	private double getTrendCoef() {
		double trendCoef = 1.0;
		if (isEnoughData()) {
			ArrayList<ChartData> data = getPreviousChartData();
			BigDecimal previousPrice = data.get(data.size()-2).getWeightedPrice();
			BigDecimal lastPrice = data.get(data.size()-1).getWeightedPrice();
			trendCoef = previousPrice.divide(lastPrice).doubleValue();
		}
		return trendCoef;
	}

}
