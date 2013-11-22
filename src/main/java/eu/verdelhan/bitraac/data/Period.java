package eu.verdelhan.bitraac.data;

import com.xeiam.xchange.dto.marketdata.Trade;
import java.util.ArrayList;
import java.util.Date;

/**
 * A period of time.
 */
public class Period {

    private Date startTimestamp;
    private Date endTimestamp;

    private ArrayList<Trade> trades = new ArrayList<Trade>();

    /**
     * @param startTimestamp the start date of the periode
     * @param endTimestamp the end date of the period
     */
    public Period(Date startTimestamp, Date endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    /**
     * @param date the date to be checked
     * @return true if the date is in the time period, false otherwise
     */
    public boolean inPeriod(Date date) {
        return date == null ? false : (!date.before(startTimestamp) && date.before(endTimestamp));
    }

    /**
     * Add a trade to the period
     * @param trade the trade to be added
     */
    public void addTrade(Trade trade) {
        trades.add(trade);
    }

    /**
     * @return the trades of the period
     */
    public ArrayList<Trade> getTrades() {
        return trades;
    }

    /**
     * @return the trade with the highest price
     */
    public Trade getHigh() {
        Trade highTrade = null;
        if (trades != null) {
            for (Trade trade : trades) {
                if (highTrade == null || highTrade.getPrice().isLessThan(trade.getPrice())) {
                    highTrade = trade;
                }
            }
        }
        return highTrade;
    }

    /**
     * @return the trade with the lowest price
     */
    public Trade getLow() {
        Trade lowTrade = null;
        if (trades != null) {
            for (Trade trade : trades) {
                if (lowTrade == null || lowTrade.getPrice().isGreaterThan(trade.getPrice())) {
                    lowTrade = trade;
                }
            }
        }
        return lowTrade;
    }

    /**
     * @return the last (closing) trade of the period
     */
    public Trade getLast() {
        return ((trades == null) || trades.isEmpty()) ? null : trades.get(trades.size()-1);
    }
}
