package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeAccount;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;

public class AlgorithmComparator {

    /**
     * @param algorithms the algorithms to be compared
     */
    public void compare(TradingAlgorithm... algorithms) {
        // Processing orders
        BigDecimal btcUsd = null;
        for (Trade trade : ExchangeMarket.getAllTrades()) {
            ExchangeMarket.addTrade(trade);
            for (TradingAlgorithm algorithm : algorithms) {
                processMarketOrder(algorithm, trade);
            }
            btcUsd = trade.getPrice().getAmount();
        }

        // Results
        for (TradingAlgorithm algorithm : algorithms) {
            ExchangeAccount account = algorithm.getExchangeAccount();
            System.out.println("Results for " + algorithm.getClass().getSimpleName() + ":"
                + "\n\tOverall earnings: $" + account.getOverallEarnings(btcUsd)
                + "\n\tAccount infos: " + account);
        }
    }

    /**
     * Process a market order.
     * @param algorithm the trading algorithm
     * @param lastTrade the last trade data
     */
    private void processMarketOrder(TradingAlgorithm algorithm, Trade lastTrade) {
        if (algorithm != null) {
            Order order = algorithm.placeOrder();
            if (order != null) {
                ExchangeAccount account = algorithm.getExchangeAccount();
                if (order.getType() == Order.OrderType.BID) {
                    // Buy
                    if (account.isEnoughUsd(order, lastTrade)) {
                        account.buy(order.getTradableAmount(), lastTrade.getPrice());
                    }
                } else if (order.getType() == Order.OrderType.ASK) {
                    // Sell
                    if (account.isEnoughBtc(order)) {
                        account.sell(order.getTradableAmount(), lastTrade.getPrice());
                    }
                }
            }
        }
    }
}
