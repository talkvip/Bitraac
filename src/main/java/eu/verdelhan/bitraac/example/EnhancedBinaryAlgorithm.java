package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.dto.Order;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;

public class EnhancedBinaryAlgorithm extends TradingAlgorithm {

	@Override
	public boolean isEnoughData()
	{
		return getPreviousChartData().size() > 20;
	}

	@Override
    public Order placeOrder() {
        return null;
    }

}
