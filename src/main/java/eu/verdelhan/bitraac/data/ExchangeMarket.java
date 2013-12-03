package eu.verdelhan.bitraac.data;

import au.com.bytecode.opencsv.CSVReader;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import eu.verdelhan.bitraac.AlgorithmComparator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

/**
 * An exchange market (e.g. Bitstamp, Mt.Gox, BTC-e, etc.)
 */
public class ExchangeMarket {

    private static final Exchange EXCHANGE = ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName());
    private static PollingMarketDataService marketDataService = EXCHANGE.getPollingMarketDataService();

    /** The transaction fee ratio (e.g. 0.002 for 0.2%) */
    private static final double TRANSACTION_FEE = 0.002;

    /** The list of all trades */
    private static final ArrayList<Trade> TRADES = getLocalTrades();

    /** The list of past periods */
    private static final ArrayList<Period> PREVIOUS_PERIODS = new ArrayList<Period>(100);

    /**
     * @return the transaction fee ratio (e.g. 0.002 for 0.2%)
     */
    public static double getTransactionFee() {
        return TRANSACTION_FEE;
    }

    /**
     * @return the trades
     */
    public static ArrayList<Trade> getAllTrades() {
        return TRADES;
    }

    /**
     * @param trade the trade to be added
     */
    public static void addTrade(Trade trade) {
        if (PREVIOUS_PERIODS.isEmpty()) {
            // First trade
            PREVIOUS_PERIODS.add(new Period(trade));
        } else {
            // Subsequent trades
            Period lastPeriod = PREVIOUS_PERIODS.get(PREVIOUS_PERIODS.size() - 1);
            while (!lastPeriod.inPeriod(trade.getTimestamp())) {
                PREVIOUS_PERIODS.add(new Period(lastPeriod.getEndTimestamp()));
                lastPeriod = PREVIOUS_PERIODS.get(PREVIOUS_PERIODS.size() - 1);
            }
            lastPeriod.addTrade(trade);
        }
    }
    
    /**
     * @param nbPeriods the number of periods
     * @return true if there is at least nbPeriods periods, false otherwise
     */
    public static boolean isEnoughPeriods(int nbPeriods) {
        return (PREVIOUS_PERIODS.size() >= nbPeriods);
    }

    /**
     * @param nbTrades the number of trades
     * @return true if the current period contains at least nbTrades trades, false otherwise
     */
    public static boolean isEnoughTrades(int nbTrades) {
        return !PREVIOUS_PERIODS.isEmpty() && (PREVIOUS_PERIODS.get(PREVIOUS_PERIODS.size() - 1).getTrades().size() >= nbTrades);
    }

    /**
     * @return the list of past periods
     */
    public static ArrayList<Period> getPreviousPeriods() {
        return PREVIOUS_PERIODS;
    }

    /**
     * @return the past trades for the current period
     */
    public static ArrayList<Trade> getPreviousTrades() {
        return PREVIOUS_PERIODS.get(PREVIOUS_PERIODS.size() - 1).getTrades();
    }

    /**
     * Dump Bitstamp trades.
     */
    public static void dumpBitstampData() {
        try
        {
            Trades trades = marketDataService.getTrades(Currencies.BTC, Currencies.USD);
            for (Trade t : trades.getTrades()) {
                System.out.println(t);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(AlgorithmComparator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * (Keeps only the last 7 days.)
     * @return the local (i.e. CSV-based) trades
     */
    private static ArrayList<Trade> getLocalTrades() {
        ArrayList<Trade> trades = new ArrayList<Trade>();
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(AlgorithmComparator.class.getClassLoader().getResourceAsStream("bitstamp_usd.14.csv")));
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

        if (!trades.isEmpty()) {
            // /!\ Performance patch /!\
            // Only keeping the last 7 days
            Trade lastTrade = trades.get(trades.size()-1);
            Date firstDateKept = new Date(lastTrade.getTimestamp().getTime() - TimeUnit.DAYS.toMillis(7));
            for (int i = trades.size() - 1; i >= 0; i--) {
                if (trades.get(i).getTimestamp().before(firstDateKept)) {
                    trades.remove(i);
                }
            }
        }
        return trades;
    }

}
