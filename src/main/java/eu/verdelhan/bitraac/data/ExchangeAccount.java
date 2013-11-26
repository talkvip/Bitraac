package eu.verdelhan.bitraac.data;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import java.math.BigDecimal;
import org.joda.money.BigMoney;

/**
 * An exchange account.
 */
public class ExchangeAccount {

    private BigDecimal initialUsdBalance;
    private BigDecimal initialBtcBalance;

    private BigDecimal currentUsdBalance;
    private BigDecimal currentBtcBalance;

    public ExchangeAccount(BigDecimal initialUsdBalance, BigDecimal initialBtcBalance) {
        this.initialUsdBalance = initialUsdBalance;
        this.initialBtcBalance = initialBtcBalance;
        this.currentUsdBalance = initialUsdBalance;
        this.currentBtcBalance = initialBtcBalance;
    }

    /**
     * @param order the order to be placed
     * @param lastTrade the last trade data
     * @return true if there is enough money to place the order, false otherwise
     */
    public boolean isEnoughUsd(Order order, Trade lastTrade) {
        if (order.getType() == Order.OrderType.BID) {
            return (order.getTradableAmount().multiply(lastTrade.getPrice().getAmount()).compareTo(currentUsdBalance) <= 0);
        }
        return true;
    }

    /**
     * @param order the order to be placed
     * @return true if there is enough bitcoins to place the order, false otherwise
     */
    public boolean isEnoughBtc(Order order) {
        if (order.getType() == Order.OrderType.ASK) {
            return (order.getTradableAmount().compareTo(currentBtcBalance) <= 0);
        }
        return true;
    }

    /**
     * Buy an amount of BTC.
     * @param amount the amount of BTC to buy
     * @param price the unit price
     */
    public void buy(BigDecimal amount, BigMoney price) {
        currentUsdBalance = currentUsdBalance.subtract(amount.multiply(price.getAmount()));
        currentBtcBalance = currentBtcBalance.add(amount);
    }

    /**
     * Sells an amount of BTC.
     * @param amount the amount of BTC to sell
     * @param price the unit price
     */
    public void sell(BigDecimal amount, BigMoney price) {
        currentBtcBalance = currentBtcBalance.subtract(amount);
        currentUsdBalance = currentUsdBalance.add(amount.multiply(price.getAmount()));
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
}
