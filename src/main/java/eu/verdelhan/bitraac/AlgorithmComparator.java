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
    private double transactionFee;

    private ExchangeAccount account;

    /**
     * @param initialUsdBalance the initial USD balance
     * @param transactionFee the transaction (e.g. 0.5 for 0.5%)
     */
    public AlgorithmComparator(double initialUsdBalance, double transactionFee)
    {
        this(initialUsdBalance, 0, transactionFee);
    }

    /**
     * @param initialUsdBalance the initial USD balance
     * @param initialBtcBalance the initial BTC balance
     * @param transactionFee the transaction (e.g. 0.5 for 0.5%)
     */
    public AlgorithmComparator(double initialUsdBalance, double initialBtcBalance, double transactionFee)
    {
        this.initialUsdBalance = new BigDecimal(initialUsdBalance);
        this.initialBtcBalance = new BigDecimal(initialBtcBalance);
        this.transactionFee = transactionFee;
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
            System.out.println("Assets ("+algorithm.getClass().getSimpleName()+"): $" + account.getOverallEarnings(btcUsd));
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
