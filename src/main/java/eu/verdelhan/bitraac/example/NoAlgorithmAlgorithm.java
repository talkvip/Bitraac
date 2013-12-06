package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;
import java.util.List;

public class NoAlgorithmAlgorithm extends TradingAlgorithm {

    public NoAlgorithmAlgorithm(double initialUsdBalance, double initialBtcBalance) {
        super(initialUsdBalance, initialBtcBalance);
    }

    @Override
    public Order placeOrder() {
        Order order = null;
        if (ExchangeMarket.isEnoughTrades(1)) {
            List<Trade> pastTrades = ExchangeMarket.getPreviousTrades();
            Trade lastTrade = pastTrades.get(pastTrades.size() - 1);
            BigDecimal nbBtcToBuy = new BigDecimal(1000.0 / lastTrade.getPrice().getAmount().doubleValue());
            order = new MarketOrder(Order.OrderType.BID, nbBtcToBuy, Currencies.BTC, Currencies.USD);
        }
        return order;
    }
}
