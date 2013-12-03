package eu.verdelhan.bitraac.algorithms;

import com.xeiam.xchange.dto.Order;
import eu.verdelhan.bitraac.data.ExchangeAccount;

public abstract class TradingAlgorithm {

    /** The exchange account of the user */
    private ExchangeAccount exchangeAccount;

    /**
     * @param initialUsdBalance the initial USD balance
     * @param initialBtcBalance the initial BTC balance
     */
    public TradingAlgorithm(double initialUsdBalance, double initialBtcBalance) {
        exchangeAccount = new ExchangeAccount(initialUsdBalance, initialBtcBalance);
    }

    /**
     * @return the exchange account of the user
     */
    public ExchangeAccount getExchangeAccount() {
        return exchangeAccount;
    }

    /**
     * Place an order (bid or ask).
     * @return the placed order
     */
    public abstract Order placeOrder();

}
