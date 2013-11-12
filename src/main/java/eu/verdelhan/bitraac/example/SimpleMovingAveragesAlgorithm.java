package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.Indicators;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;

public class SimpleMovingAveragesAlgorithm extends TradingAlgorithm {

    @Override
    public boolean isEnoughTrades() {
        return getPreviousTrades().size() > 10;
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
        if (isEnoughTrades()) {
            double movingAvg10 = Indicators.getMovingAverage(10).doubleValue();
            double movingAvg3 = Indicators.getMovingAverage(3).doubleValue();
            trendCoef = movingAvg3 / movingAvg10;
            // System.out.println("avg10="+movingAvg10+"  avg3="+movingAvg3+"  coef="+trendCoef);
        }
        return trendCoef;
    }

}
