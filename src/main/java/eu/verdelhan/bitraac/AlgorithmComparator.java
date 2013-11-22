package eu.verdelhan.bitraac;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;

public class AlgorithmComparator {

    private BigDecimal initialUsdBalance;
    private BigDecimal initialBtcBalance;
    private double transactionFee;

    private BigDecimal currentUsdBalance;
    private BigDecimal currentBtcBalance;

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
            currentUsdBalance = initialUsdBalance;
            currentBtcBalance = initialBtcBalance;
            for (Trade trade : ExchangeMarket.getAllTrades()) {
                //System.out.println("balance: USD=" + currentUsdBalance + " BTC="+ currentBtcBalance);
                algorithm.addTrade(trade);
                processMarketOrder((MarketOrder) algorithm.placeOrder(), trade);
                btcUsd = trade.getPrice().getAmount();
            }
            System.out.println("************");
            System.out.println("Result (assets): $" + getOverallEarnings(btcUsd));
            System.out.println("************");
        }
//        ComparativeChart.addTradeSeries("trades", getLocalTrades());
//        ComparativeChart.show();
    }

    /**
     * @param currentBtcUsd the current BTC/USD rate
     * @return the overall earnings (can be negative in case of loss) in USD
     */
    public double getOverallEarnings(BigDecimal currentBtcUsd) {
        BigDecimal usdDifference = currentUsdBalance.subtract(initialUsdBalance);
        BigDecimal btcDifference = currentBtcBalance.subtract(initialBtcBalance);
        return usdDifference.add(btcDifference.multiply(currentBtcUsd)).doubleValue();
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
                if (isEnoughUsd(order, lastTrade)) {
                    currentUsdBalance = currentUsdBalance.subtract(order.getTradableAmount().multiply(lastTrade.getPrice().getAmount()));
                    currentBtcBalance = currentBtcBalance.add(order.getTradableAmount());
                }
            } else if (order.getType() == Order.OrderType.ASK) {
                // Sell
                if (isEnoughBtc(order)) {
                    currentBtcBalance = currentBtcBalance.subtract(order.getTradableAmount());
                    currentUsdBalance = currentUsdBalance.add(order.getTradableAmount().multiply(lastTrade.getPrice().getAmount()));
                }
            }
        }
    }

    /**
     * @param order the order to be placed
     * @param lastTrade the last trade data
     * @return true if there is enough money to place the order, false otherwise
     */
    private boolean isEnoughUsd(Order order, Trade lastTrade) {
        if (order.getType() == Order.OrderType.BID) {
            return (order.getTradableAmount().multiply(lastTrade.getPrice().getAmount()).compareTo(currentUsdBalance) <= 0);
        }
        return true;
    }

    /**
     * @param order the order to be placed
     * @return true if there is enough bitcoins to place the order, false otherwise
     */
    private boolean isEnoughBtc(Order order) {
        if (order.getType() == Order.OrderType.ASK) {
            return (order.getTradableAmount().compareTo(currentBtcBalance) <= 0);
        }
        return true;
    }
}
