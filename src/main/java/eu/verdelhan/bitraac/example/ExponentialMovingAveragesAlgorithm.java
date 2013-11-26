package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.Overlays;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ExponentialMovingAveragesAlgorithm extends TradingAlgorithm {

    private ArrayList<Period> tradesByPeriods = ExchangeMarket.getTradesByPeriod(60);

    @Override
    public boolean isEnoughTrades() {
        return getPreviousTrades().size() > 26;
    }

    @Override
    public Order placeOrder() {
        Order order;
        double trendCoef = getTrendCoef();
        if (trendCoef > 1.02) {
            // Up trend
            order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(2), Currencies.BTC, Currencies.USD);
        } else if (trendCoef < 0.98) {
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
            double longTermAvg = Overlays.getSimpleMovingAverage(tradesByPeriods, 26).doubleValue();
            double shortTermAvg = Overlays.getSimpleMovingAverage(tradesByPeriods, 12).doubleValue();
            trendCoef = shortTermAvg / longTermAvg;
        }
        return trendCoef;
    }

}
