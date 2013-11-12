package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;

public class NoAlgorithmAlgorithm extends TradingAlgorithm {

    @Override
    public boolean isEnoughTrades() {
        return getPreviousTrades().size() > 0;
    }

    @Override
    public Order placeOrder() {
        Order order = null;
        if (isEnoughTrades()) {
            BigDecimal nbBtcToBuy = new BigDecimal(1000 / 203.978226405186);
            order = new MarketOrder(Order.OrderType.BID, nbBtcToBuy, Currencies.BTC, Currencies.USD);
        }
        return order;
    }
}
