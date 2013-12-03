package eu.verdelhan.bitraac.example;

import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.MarketOrder;
import eu.verdelhan.bitraac.Indicators;
import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;
import eu.verdelhan.bitraac.data.ExchangeMarket;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MultiIndicatorsAlgorithm extends TradingAlgorithm {

    private static final int LONG_TERM_EMA_NB_PERIODS = 12;
    private static final int SHORT_TERM_EMA_NB_PERIODS = 4;
    private static final int AROON_NB_PERIODS = 6;
    private static final int ROC_NB_PERIODS = 48;
    private static final double ROC_HIGH_THRESHOLD = 10.0;

    private static final BigDecimal TRADE_MARGIN = new BigDecimal(0.01);

    public MultiIndicatorsAlgorithm(double initialUsdBalance, double initialBtcBalance) {
        super(initialUsdBalance, initialBtcBalance);
    }

    @Override
    public Order placeOrder() {
        Order order = null;
        if (ExchangeMarket.isEnoughPeriods(ROC_NB_PERIODS)) {
            if (isOverbought()) {
                // Overbought
                BigDecimal btcBalance = getExchangeAccount().getCurrentBtcBalance();
                btcBalance = btcBalance.subtract(TRADE_MARGIN);
                if (btcBalance.compareTo(BigDecimal.ZERO) == 1) {
                    order = new MarketOrder(Order.OrderType.ASK, btcBalance, Currencies.BTC, Currencies.USD);
                }
            } else if (isOversold()) {
                // Oversold
                BigDecimal lastTradePrice = ExchangeMarket.getPreviousTrades().get(ExchangeMarket.getPreviousTrades().size() - 1).getPrice().getAmount();
                BigDecimal btcToBeBought = getExchangeAccount().getCurrentUsdBalance().divide(lastTradePrice, RoundingMode.HALF_UP).subtract(TRADE_MARGIN);
                if (btcToBeBought.compareTo(BigDecimal.ZERO) == 1) {
                    order = new MarketOrder(Order.OrderType.BID, btcToBeBought, Currencies.BTC, Currencies.USD);
                }
            } else {
                double ppo = getTrendStrength();
                if (ppo > 0.5) {
                    // Strong up trend
                    order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(2), Currencies.BTC, Currencies.USD);
                } else if (ppo > 0.1) {
                    // Up trend
                    order = new MarketOrder(Order.OrderType.ASK, new BigDecimal(0.5), Currencies.BTC, Currencies.USD);
                } else if (ppo < -0.1) {
                    // Down trend
                    order = new MarketOrder(Order.OrderType.BID, new BigDecimal(0.5), Currencies.BTC, Currencies.USD);
                } else if (ppo < -0.4) {
                    // Strong down trend
                    order = new MarketOrder(Order.OrderType.BID, new BigDecimal(2), Currencies.BTC, Currencies.USD);
                } else {
                    // Stability
                    order = null;
                }
            }
        }
        return order;

    }

    private double getTrendStrength() {
        double ppo = 0;
        if (ExchangeMarket.isEnoughPeriods(LONG_TERM_EMA_NB_PERIODS)) {
            ppo = Indicators.getPercentagePriceOscillator(ExchangeMarket.getPreviousPeriods(), SHORT_TERM_EMA_NB_PERIODS, LONG_TERM_EMA_NB_PERIODS).doubleValue();
        }
        return ppo;
    }

    private boolean isConsolidation() {
        double aroonUp = Indicators.getAroonUp(ExchangeMarket.getPreviousPeriods(), AROON_NB_PERIODS);
        double aroonDown = Indicators.getAroonUp(ExchangeMarket.getPreviousPeriods(), AROON_NB_PERIODS);
        return (aroonUp < 50 && aroonDown < 50);
    }

    private boolean isOverbought() {
        return Indicators.getRateOfChange(ExchangeMarket.getPreviousPeriods(), ROC_NB_PERIODS) >= ROC_HIGH_THRESHOLD;
    }

    private boolean isOversold() {
        return Indicators.getRateOfChange(ExchangeMarket.getPreviousPeriods(), ROC_NB_PERIODS) <= (ROC_HIGH_THRESHOLD * -1.0);
    }
}
