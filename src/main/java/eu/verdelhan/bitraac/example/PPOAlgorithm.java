package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.Indicators;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import eu.verdelhan.bitraac.data.Period;
import java.math.BigDecimal;
import java.util.ArrayList;

public class PPOAlgorithm extends TradingAlgorithm {

    private ArrayList<Period> tradesByPeriods = ExchangeMarket.getTradesByPeriod(60);

    @Override
    public boolean isEnoughTrades() {
        return getPreviousTrades().size() > 26;
    }

    @Override
    public Order placeOrder() {
        Order order;
        double ppo = getPPO();
        if (ppo > 0.05) {
            order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(2), Currencies.BTC, Currencies.USD);
        } else if (ppo < -0.03) {
            order = new MarketOrder(Order.OrderType.BID, new BigDecimal(2), Currencies.BTC, Currencies.USD);
        } else {
            // Stability
            order = null;
        }
        //System.out.println("order: "+order);
        return order;
    }

    private double getPPO() {
        double ppo = 0;
        if (isEnoughTrades()) {
            ppo = Indicators.getPercentagePriceOscillator(tradesByPeriods, 12, 26).doubleValue();
            //System.out.println("ppo: " + ppo);
        }
        return ppo;
    }

}
