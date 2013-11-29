package eu.verdelhan.bitraac.algorithms;

import com.xeiam.xchange.dto.Order;

public abstract class TradingAlgorithm {

    /**
     * Place an order (bid or ask).
     * @return the placed order
     */
    public abstract Order placeOrder();

}
