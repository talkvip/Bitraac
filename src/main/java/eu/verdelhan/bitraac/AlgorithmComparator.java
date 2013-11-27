package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeAccount;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;

public class AlgorithmComparator {

    private BigDecimal initialUsdBalance;
    private BigDecimal initialBtcBalance;

    private ExchangeAccount account;

    /**
     * @param initialUsdBalance the initial USD balance
     */
    public AlgorithmComparator(double initialUsdBalance) {
        this(initialUsdBalance, 0);
    }

    /**
     * @param initialUsdBalance the initial USD balance
     * @param initialBtcBalance the initial BTC balance
     */
    public AlgorithmComparator(double initialUsdBalance, double initialBtcBalance) {
        this.initialUsdBalance = new BigDecimal(initialUsdBalance);
        this.initialBtcBalance = new BigDecimal(initialBtcBalance);
    }

    /**
     * @param algorithms the algorithms to be compared
     */
    public void compare(TradingAlgorithm... algorithms) {
        for (TradingAlgorithm algorithm : algorithms) {
            BigDecimal btcUsd = null;
            account = new ExchangeAccount(initialUsdBalance, initialBtcBalance);
            for (Trade trade : ExchangeMarket.getAllTrades()) {
                algorithm.addTrade(trade);
                processMarketOrder((MarketOrder) algorithm.placeOrder(), trade);
                btcUsd = trade.getPrice().getAmount();
            }
            System.out.println("Results for " + algorithm.getClass().getSimpleName() + ":"
                    + "\n\tOverall earnings: $" + account.getOverallEarnings(btcUsd)
                    + "\n\tAccount infos: " + account);
        }
//        ComparativeChart.addTradeSeries("trades", getLocalTrades());
//        ComparativeChart.show();
    }

    /**
     * Process a market order.
     * @param order the order to be processed
     * @param lastTrade the last trade data
     */
    private void processMarketOrder(MarketOrder order, Trade lastTrade) {
        if (order != null) {
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
