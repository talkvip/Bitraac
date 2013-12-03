package eu.verdelhan.bitraac.data;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.joda.money.BigMoney;

/**
 * An exchange account.
 */
public class ExchangeAccount {

    private BigDecimal initialUsdBalance;
    private BigDecimal initialBtcBalance;

    private BigDecimal currentUsdBalance;
    private BigDecimal currentBtcBalance;

    private BigDecimal feeBalance = BigDecimal.ZERO;

    private int tradeCounter = 0;

    /**
     * @param initialUsdBalance the initial USD balance
     * @param initialBtcBalance the initial BTC balance
     */
    public ExchangeAccount(double initialUsdBalance, double initialBtcBalance) {
        this(new BigDecimal(initialUsdBalance), new BigDecimal(initialBtcBalance));
    }

    /**
     * @param initialUsdBalance the initial USD balance
     * @param initialBtcBalance the initial BTC balance
     */
    public ExchangeAccount(BigDecimal initialUsdBalance, BigDecimal initialBtcBalance) {
        this.initialUsdBalance = initialUsdBalance;
        this.initialBtcBalance = initialBtcBalance;
        this.currentUsdBalance = initialUsdBalance;
        this.currentBtcBalance = initialBtcBalance;
    }

    /**
     * @return the current USD balance
     */
    public BigDecimal getCurrentUsdBalance() {
        return currentUsdBalance;
    }

    /**
     * @return the current BTC balance
     */
    public BigDecimal getCurrentBtcBalance() {
        return currentBtcBalance;
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
     * (Deduct the exchange transaction fee so the real amount of buyed BTC will be lower)
     * @param amount the amount of BTC to buy
     * @param price the unit price
     */
    public void buy(BigDecimal amount, BigMoney price) {
        // Deducting transaction fee
        BigDecimal usdAmount = deductFee(amount.multiply(price.getAmount()));
        currentUsdBalance = currentUsdBalance.subtract(usdAmount);
        currentBtcBalance = currentBtcBalance.add(usdAmount.divide(price.getAmount(), RoundingMode.HALF_UP));
        // Updating the trade counter
        tradeCounter++;
    }

    /**
     * Sells an amount of BTC.
     * (Deduct the exchange transaction fee)
     * @param amount the amount of BTC to sell
     * @param price the unit price
     */
    public void sell(BigDecimal amount, BigMoney price) {
        currentBtcBalance = currentBtcBalance.subtract(amount);
        // Deducting transaction fee
        BigDecimal usdAmount = deductFee(amount.multiply(price.getAmount()));
        currentUsdBalance = currentUsdBalance.add(usdAmount);
        // Updating the trade counter
        tradeCounter++;
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
     * @return the overall deducted fees in USD
     */
    public double getOverallDeductedFees() {
        return feeBalance.doubleValue();
    }

    @Override
    public String toString() {
        return "ExchangeAccount [initialUsdBalance=" + initialUsdBalance.doubleValue()
                + ", initialBtcBalance=" + initialBtcBalance.doubleValue()
                + ", currentUsdBalance=" + currentUsdBalance.doubleValue()
                + ", currentBtcBalance=" + currentBtcBalance.doubleValue()
                + ", feeBalance=" + feeBalance.doubleValue()
                + ", tradeCounter=" + tradeCounter
                + "]";
    }

    /**
     * @param amount the USD amount before fee deduction
     * @return the USD amount after fee deduction
     */
    private BigDecimal deductFee(BigDecimal amount) {
        BigDecimal feeAmount = amount.multiply(new BigDecimal(ExchangeMarket.getTransactionFee())).setScale(2, BigDecimal.ROUND_UP);
        feeBalance = feeBalance.add(feeAmount);
        return amount.subtract(feeAmount);
    }
}
