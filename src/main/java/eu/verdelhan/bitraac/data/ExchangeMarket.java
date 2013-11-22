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

    private static final ArrayList<Trade> TRADES = getLocalTrades();

    /**
     * @return the trades
     */
    public static ArrayList<Trade> getAllTrades() {
        return TRADES;
    }

    /**
     * @param duration the duration (in seconds) of each periods
     * @return the periods with their trades
     */
    public static ArrayList<Period> getTradesByPeriod(int duration) {
        ArrayList<Period> periods = new ArrayList<Period>(1000);

        // Building the list of periods
        Date firstTradeDate = TRADES.get(0).getTimestamp();
        Date lastTradeDate = TRADES.get(TRADES.size()-1).getTimestamp();
        Date periodStartDate = firstTradeDate;
        while (!periodStartDate.after(lastTradeDate)) {
            Date periodEndDate = new Date(periodStartDate.getTime() + TimeUnit.SECONDS.toMillis(duration));
            periods.add(new Period(periodStartDate, periodEndDate));
            periodStartDate = periodEndDate;
        }

        // Adding trades for each period
        for (Trade trade : TRADES) {
            Date tradeDate = trade.getTimestamp();
            for (Period period : periods) {
                if (period.inPeriod(tradeDate)) {
                    period.addTrade(trade);
                    break;
                }
            }
        }

        return periods;
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
