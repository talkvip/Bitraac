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

	private ArrayList<Trade> trades = new ArrayList<Trade>(1000);

	public Period(Date startTimestamp, Date endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}

	public boolean inPeriod(Date date) {
		return date == null ? false : (!date.before(startTimestamp) && date.before(endTimestamp));
	}

	public void addTrade(Trade trade) {
		trades.add(trade);
	}

	public ArrayList<Trade> getTrades()
	{
		return trades;
	}
}
