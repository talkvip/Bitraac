package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.Indicators;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;

public class PPOAlgorithm extends TradingAlgorithm {

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
        if (ExchangeMarket.isEnoughPeriods(26)) {
            ppo = Indicators.getPercentagePriceOscillator(ExchangeMarket.getPreviousPeriods(), 12, 26).doubleValue();
            //System.out.println("ppo: " + ppo);
        }
        return ppo;
    }

}
