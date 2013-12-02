package eu.verdelhan.bitraac.data;

import com.xeiam.xchange.dto.marketdata.Trade;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.joda.money.BigMoney;

/**
 * A period of time.
 */
public class Period {

    /** The duration of a period (in seconds) */
    public static final int DURATION = 300;

    private Date startTimestamp;
    private Date endTimestamp;

    private ArrayList<Trade> trades = new ArrayList<Trade>();

    /**
     * @param trade the first trade of the period
     */
    public Period(Trade trade) {
        this(trade.getTimestamp());
        trades.add(trade);
    }

    /**
     * @param startTimestamp the start date of the periode
     */
    public Period(Date startTimestamp) {
        this(startTimestamp,
             new Date(startTimestamp.getTime() + TimeUnit.SECONDS.toMillis(DURATION)));
    }

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
     * @return the end date of the period
     */
    public Date getEndTimestamp() {
        return endTimestamp;
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

    /**
     * Aka. pivot point.
     * (H + L + C) / 3
     * @return the typical price
     */
    public BigMoney getTypicalPrice() {
        BigMoney high = getHigh().getPrice();
        BigMoney low = getLow().getPrice();
        BigMoney close = getLast().getPrice();
        return high.plus(low).plus(close).dividedBy(3, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Period [" + "startTimestamp=" + startTimestamp + ", endTimestamp=" + endTimestamp + ", trades=" + trades.size() + ']';
    }
}
