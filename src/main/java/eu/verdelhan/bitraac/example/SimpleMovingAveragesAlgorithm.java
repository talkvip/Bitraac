package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import eu.verdelhan.bitraac.indicators.SMA;
import java.math.BigDecimal;

public class SimpleMovingAveragesAlgorithm extends TradingAlgorithm {

    public SimpleMovingAveragesAlgorithm(double initialUsdBalance, double initialBtcBalance) {
        super(initialUsdBalance, initialBtcBalance);
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
        if (ExchangeMarket.isEnoughPeriods(25)) {
            double movingAvg25 = new SMA(ExchangeMarket.getPreviousPeriods(), 25).execute().doubleValue();
            double movingAvg10 = new SMA(ExchangeMarket.getPreviousPeriods(), 10).execute().doubleValue();
            trendCoef = movingAvg10 / movingAvg25;
        }
        return trendCoef;
    }

}
