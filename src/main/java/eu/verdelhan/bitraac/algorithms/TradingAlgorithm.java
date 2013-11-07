package eu.verdelhan.bitraac.algorithms;

import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import com.xeiam.xchange.dto.Order;
import java.util.ArrayList;

public abstract class TradingAlgorithm {

	private ArrayList<ChartData> previousChartData = new ArrayList<ChartData>(1000);

	/**
	 * Get the previous data.
	 * Can be used for trend computation.
	 * @return the previous data
	 */
	public ArrayList<ChartData> getPreviousChartData()
	{
		return previousChartData;
	}

	/**
	 * Add a data sample to previous data.
	 * @param chartData the data sample to be added
	 */
	public void addChartData(ChartData chartData) {
		previousChartData.add(chartData);
	}

	/**
	 * @return true if there is enough data to execute the algorithm, false otherwise
	 */
	public abstract boolean isEnoughData();

	/**
	 * Place an order (bid or ask).
	 * @return the placed order
	 */
	public abstract Order placeOrder();

}
