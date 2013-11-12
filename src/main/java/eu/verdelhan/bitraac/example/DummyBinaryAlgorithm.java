package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class DummyBinaryAlgorithm extends TradingAlgorithm {

    @Override
    public boolean isEnoughTrades() {
        return getPreviousTrades().size() > 2;
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
            ArrayList<Trade> trades = getPreviousTrades();
            BigDecimal previousPrice = trades.get(trades.size() - 2).getPrice().getAmount();
            BigDecimal lastPrice = trades.get(trades.size() - 1).getPrice().getAmount();
            trendCoef = previousPrice.divide(lastPrice, 12, RoundingMode.HALF_UP).doubleValue();
            // System.out.println("pp="+previousPrice+" lp="+lastPrice+
            // " coef="+trendCoef);
        }
        return trendCoef;
    }

}
