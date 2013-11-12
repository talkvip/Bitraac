package eu.verdelhan.bitraac;

import au.com.bytecode.opencsv.CSVReader;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public class AlgorithmComparator {

	private static final Exchange EXCHANGE = ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName());
	private PollingMarketDataService marketDataService = EXCHANGE.getPollingMarketDataService();

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

    public void compare(TradingAlgorithm... algorithms) {
        for (TradingAlgorithm algorithm : algorithms) {
			BigDecimal btcUsd = null;
			currentUsdBalance = initialUsdBalance;
			currentBtcBalance = initialBtcBalance;
			for (Trade trade : getLocalTrades()) {
				//System.out.println("balance: USD=" + currentUsdBalance + " BTC="+ currentBtcBalance);
				algorithm.addTrade(trade);
				processMarketOrder((MarketOrder) algorithm.placeOrder(), trade);
				btcUsd = trade.getPrice().getAmount();
			}
			System.out.println("************");
			System.out.println("Result (assets): $" + getOverallEarnings(btcUsd));
			System.out.println("************");
        }
    }

	public void dumpBitstampData() {
		try
		{
			Trades trades = marketDataService.getTrades(Currencies.BTC, Currencies.USD, 0, 3);
			System.out.println("trades: off 0 lim 3");
			for (Trade t : trades.getTrades()) {
				System.out.println("t: "+t);
			}

			trades = marketDataService.getTrades(Currencies.BTC, Currencies.USD, 1, 3);
			System.out.println("trades: off 1 lim 3");
			for (Trade t : trades.getTrades()) {
				System.out.println("t: "+t);
			}

			trades = marketDataService.getTrades(Currencies.BTC, Currencies.USD, 1, 2);
			System.out.println("trades: off 1 lim 2");
			for (Trade t : trades.getTrades()) {
				System.out.println("t: "+t);
			}
		}
		catch (IOException ex)
		{
			Logger.getLogger(AlgorithmComparator.class.getName()).log(Level.SEVERE, null, ex);
		}
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

    public static ArrayList<Trade> getLocalTrades() {
        ArrayList<Trade> trades = new ArrayList<Trade>();
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(AlgorithmComparator.class.getClassLoader().getResourceAsStream("bitstamp_usd.0.csv")));
            CSVReader csvReader = new CSVReader(fileReader, ',');
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                Date timestamp = new Date(Long.parseLong(line[0]) * 1000);
                BigMoney price = BigMoney.of(CurrencyUnit.USD, new BigDecimal(line[1]));
                BigDecimal tradableAmount = new BigDecimal(line[2]);
                Trade trade = new Trade(null, tradableAmount, Currencies.BTC, Currencies.USD, price, timestamp, 0);
                trades.add(trade);
            }
        } catch (IOException ioe) {
            Logger.getLogger(AlgorithmComparator.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", ioe);
        }
        return trades;
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
