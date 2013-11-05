package eu.verdelhan.bitraac;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitcoincharts.BitcoinChartsExchange;
import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import com.xeiam.xchange.bitcoincharts.service.polling.BitcoinChartsPollingMarketDataService;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import java.math.BigDecimal;

public class AlgorithmComparator {

	private static final Exchange EXCHANGE = ExchangeFactory.INSTANCE.createExchange(BitcoinChartsExchange.class.getName());

	private BitcoinChartsPollingMarketDataService marketDataService = (BitcoinChartsPollingMarketDataService) EXCHANGE.getPollingMarketDataService();

	private BigDecimal initialBalance;

	public AlgorithmComparator(BigDecimal initialBalance)
	{
		this.initialBalance = initialBalance;
	}

    public void compare(TradingAlgorithm... algorithms) {
        for (TradingAlgorithm algorithm : algorithms) {
            System.out.println(algorithm);
        }
    }

	public void getData() {
		ChartData[] data = marketDataService.getChartData("bitstampUSD", 7);
		for (ChartData chartData : data) {
			System.out.println(chartData);
		}
	}
}
