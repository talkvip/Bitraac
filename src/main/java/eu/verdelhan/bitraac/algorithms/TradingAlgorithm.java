package eu.verdelhan.bitraac.algorithms;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import java.util.ArrayList;

public abstract class TradingAlgorithm {

	private ArrayList<Trade> previousTrades = new ArrayList<Trade>(1000);

	/**
	 * Get the previous trades.
	 * Can be used for trend computation.
	 * @return the previous trades
	 */
	public ArrayList<Trade> getPreviousTrades()
	{
		return previousTrades;
	}

	/**
	 * Add a trade to previous trades.
	 * @param trade the trade to be added
	 */
	public void addTrade(Trade trade) {
		previousTrades.add(trade);
	}

	/**
	 * @return true if there is enough trades to execute the algorithm, false otherwise
	 */
	public abstract boolean isEnoughTrades();

	/**
	 * Place an order (bid or ask).
	 * @return the placed order
	 */
	public abstract Order placeOrder();

}
