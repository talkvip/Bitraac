package eu.verdelhan.bitraac;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import au.com.bytecode.opencsv.CSVReader;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitcoincharts.BitcoinChartsExchange;
import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import com.xeiam.xchange.bitcoincharts.service.polling.BitcoinChartsPollingMarketDataService;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.service.polling.PollingMarketDataService;

import eu.verdelhan.bitraac.algorithms.TradingAlgorithm;

public class AlgorithmComparator {

	private static final Exchange EXCHANGE = ExchangeFactory.INSTANCE.createExchange(BitcoinChartsExchange.class.getName());
	private static final Exchange BITSTAMP_EXCHANGE = ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName());

	private BitcoinChartsPollingMarketDataService marketDataService = (BitcoinChartsPollingMarketDataService) EXCHANGE.getPollingMarketDataService();
	private PollingMarketDataService bitstampMarketDataService = BITSTAMP_EXCHANGE.getPollingMarketDataService();

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
			for (ChartData sample : getLocalData()) {
				//System.out.println("balance: USD=" + currentUsdBalance + " BTC="+ currentBtcBalance);
				algorithm.addChartData(sample);
				processMarketOrder((MarketOrder) algorithm.placeOrder(), sample);
				btcUsd = sample.getWeightedPrice();
			}
			System.out.println("************");
			System.out.println("Result (assets): $" + getOverallEarnings(btcUsd));
			System.out.println("************");
        }
    }

	public void dumpBitstampData() {
		try
		{
			Trades trades = bitstampMarketDataService.getTrades(Currencies.BTC, Currencies.USD, 0, 3);
			System.out.println("trades: off 0 lim 3");
			for (Trade t : trades.getTrades()) {
				System.out.println("t: "+t);
			}

			trades = bitstampMarketDataService.getTrades(Currencies.BTC, Currencies.USD, 1, 3);
			System.out.println("trades: off 1 lim 3");
			for (Trade t : trades.getTrades()) {
				System.out.println("t: "+t);
			}

			trades = bitstampMarketDataService.getTrades(Currencies.BTC, Currencies.USD, 1, 2);
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

	public void dumpBitcoinchartsData() {
		ChartData[] data = marketDataService.getChartData("bitstampUSD", 7);
		for (ChartData chartData : data) {
			System.out.println(chartData);
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

    public ArrayList<Trade> getLocalTrades() throws IOException {
        ArrayList<Trade> trades = new ArrayList<Trade>();
        
        FileReader fileReader = new FileReader("bitstamp_usd.0.csv");
        CSVReader csvReader = new CSVReader(fileReader, ',');
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            Date timestamp = new Date(Long.parseLong(line[0]) * 1000);
            BigMoney price = BigMoney.of(CurrencyUnit.USD, new BigDecimal(line[1]));
            BigDecimal tradableAmount = new BigDecimal(line[2]);
            Trade trade = new Trade(null, tradableAmount, Currencies.BTC, Currencies.USD, price, timestamp, 0);
            trades.add(trade);
        }
        
        return trades;
    }

	/**
	 * @return sample data
	 */
	public ChartData[] getLocalData()
	{
		ArrayList<ArrayList<String>> dataList = new ArrayList<ArrayList<String>>(100);
		dataList.add(new ArrayList<String>(Arrays.asList("1383091200", "204.0", "202.05", "205.0", "203.04", "702.18588657", "143230.6317493015", "203.978226405186")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383094800", "203.04", "202.55", "204.21", "204.2", "161.52080063", "32889.1570886031", "203.621805738465")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383098400", "204.2", "203.0", "204.66", "204.46", "140.98486155", "28736.4147943033", "203.826244026292")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383102000", "204.46", "203.12", "204.6", "203.59", "187.74590146", "38317.2767084757", "204.091148784088")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383105600", "203.59", "199.62", "204.54", "200.0", "2996.72757586", "601856.7766267436", "200.838001250088")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383109200", "200.0", "197.0", "200.59", "197.79", "1694.74712552", "338206.2494831865", "199.561482884601")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383112800", "197.79", "197.55", "200.59", "200.57", "1632.45315879", "326829.1955631751", "200.207395724252")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383116400", "200.57", "200.01", "200.59", "200.57", "727.72857867", "145891.5172541231", "200.475179249873")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383120000", "200.57", "199.37", "200.59", "200.55", "661.97650925", "132596.4925567981", "200.303924238982")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383123600", "200.55", "200.5", "202.98", "202.9", "1764.56138335", "354871.7988764771", "201.110486846742")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383127200", "202.9", "201.98", "203.0", "202.99", "550.00674749", "111530.9726176723", "202.781098825883")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383130800", "202.99", "202.39", "206.0", "206.0", "2166.94235025", "441680.319688923", "203.826520644616")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383134400", "206.0", "204.0", "206.0", "205.98", "767.80683434", "157742.2219821921", "205.445191325739")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383138000", "205.98", "205.0", "206.0", "205.03", "404.91678925", "83271.4434597582", "205.650755094638")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383141600", "205.03", "201.45", "205.75", "202.76", "959.7787372", "194974.619424773", "203.145383271961")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383145200", "202.76", "201.98", "204.99", "202.65", "846.57541108", "172058.5683845514", "203.240687282721")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383148800", "202.65", "202.66", "205.01", "205.0", "760.39143088", "155444.7899693787", "204.427329999606")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383152400", "205.0", "202.55", "205.0", "202.71", "379.06231416", "77111.2744071874", "203.426380113954")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383156000", "202.71", "201.45", "203.7", "201.6", "718.61368601", "145569.8148273375", "202.570334605778")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383159600", "201.6", "198.11", "201.69", "199.67", "2637.79420548", "527481.7774064344", "199.970784798373")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383163200", "199.67", "199.44", "201.54", "201.5", "397.21041269", "79544.4092738138", "200.257613427404")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383166800", "201.5", "199.65", "201.55", "201.39", "667.14679691", "133682.7523480704", "200.379815907449")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383170400", "201.39", "195.0", "201.55", "196.15", "2845.28627104", "560506.894709169", "196.99490361098")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383174000", "196.15", "195.0", "199.0", "197.54", "721.98430158", "141541.6375521316", "196.045311847334")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383177600", "197.54", "197.3", "200.11", "200.11", "493.50527046", "97892.5258365085", "198.361662369406")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383181200", "200.11", "197.44", "200.27", "200.27", "419.28342912", "83471.4655923343", "199.081241458852")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383184800", "200.27", "199.04", "200.84", "200.84", "243.49906327", "48596.7960020659", "199.576932040104")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383188400", "200.84", "200.0", "201.0", "200.01", "161.88633735", "32409.6393795181", "200.199966903001")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383192000", "200.01", "199.85", "201.0", "199.85", "74.20409944", "14869.6516862592", "200.388547243034")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383195600", "199.85", "198.9", "200.15", "200.14", "357.32745963", "71198.1072648489", "199.251709730263")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383199200", "200.14", "198.91", "200.15", "199.28", "90.96391036", "18169.5228383747", "199.744302619211")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383202800", "199.28", "199.01", "200.0", "199.9", "144.46793143", "28871.8589445206", "199.849604398261")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383206400", "199.9", "195.1", "199.9", "199.61", "938.71381069", "185961.5147458584", "198.102459586877")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383210000", "199.61", "196.07", "199.95", "198.0", "983.7631924", "194886.3407250764", "198.102899387432")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383213600", "198.0", "197.1", "200.0", "198.99", "311.73173262", "61889.3324878451", "198.53395086758")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383217200", "198.99", "198.42", "200.43", "200.32", "451.60754821", "90189.5378704299", "199.707773326436")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383220800", "200.32", "199.08", "200.5", "200.26", "515.8139664", "103083.728840972", "199.846719080563")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383224400", "200.26", "197.99", "200.55", "198.0", "916.41014913", "181919.7336569538", "198.513442730485")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383228000", "198.0", "197.99", "199.97", "199.89", "941.03314359", "186667.2708217996", "198.36418312502")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383231600", "199.89", "198.7", "201.44", "200.33", "951.01571609", "190573.2255754977", "200.389144313008")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383235200", "200.33", "199.26", "201.7", "201.7", "703.92863393", "141446.6999847151", "200.938977570816")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383238800", "201.7", "201.5", "202.49", "202.48", "872.43450324", "176236.5732387675", "202.00550595405")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383242400", "202.48", "201.59", "202.97", "202.79", "461.43735555", "93437.5306351702", "202.492341617638")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383246000", "202.79", "202.06", "203.75", "203.74", "1168.35463095", "237356.2351061822", "203.154272528696")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383249600", "203.74", "202.0", "203.75", "202.26", "367.16745259", "74430.6642330112", "202.71585541686")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383253200", "202.26", "202.26", "203.49", "202.3", "907.44038137", "183900.1588698847", "202.658116880629")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383256800", "202.3", "201.77", "203.49", "203.49", "1101.7597954", "223073.2422125825", "202.469942308608")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383260400", "203.49", "201.93", "203.55", "203.54", "163.74123333", "33283.1611554273", "203.266828266459")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383264000", "203.54", "201.9", "203.54", "201.95", "215.76437059", "43669.6543077943", "202.39511365283")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383267600", "201.95", "201.76", "203.29", "203.25", "188.79616425", "38181.0243453496", "202.234110512918")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383271200", "203.25", "201.76", "203.48", "203.23", "189.62097255", "38447.872766043", "202.761710632535")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383274800", "203.23", "201.76", "203.38", "203.27", "61.98478041", "12569.9197983572", "202.790422345827")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383278400", "203.27", "201.5", "203.27", "202.5", "148.7399402", "30053.1481342941", "202.051635182075")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383282000", "202.5", "201.01", "202.73", "202.47", "104.75060122", "21148.2746712122", "201.891678185178")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383285600", "202.47", "200.23", "203.09", "202.43", "245.52819153", "49534.6686728485", "201.747377212266")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383289200", "202.43", "201.3", "203.38", "203.37", "129.76769127", "26339.613039137", "202.975122554456")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383292800", "203.37", "202.05", "203.38", "202.37", "166.20068098", "33720.5861903942", "202.890782345543")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383296400", "202.37", "202.0", "203.37", "202.37", "272.86641101", "55405.5761069199", "203.050188192234")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383300000", "202.37", "201.47", "203.07", "202.9", "187.15079042", "37791.4160439415", "201.930304216887")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383303600", "202.9", "201.62", "203.12", "202.45", "293.91956012", "59575.6622526085", "202.693765016133")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383307200", "202.45", "201.72", "203.09", "201.9", "66.96401257", "13570.5877293825", "202.654936712412")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383310800", "201.9", "201.69", "202.95", "202.8", "204.16137234", "41279.2100613718", "202.189129061239")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383314400", "202.8", "201.82", "202.89", "202.8", "147.56146275", "29886.9215712966", "202.538799862205")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383318000", "202.8", "201.8", "202.9", "202.9", "158.14992711", "32030.2331275403", "202.530811824288")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383321600", "202.9", "201.93", "203.3", "202.81", "672.28865637", "136337.686896893", "202.796351842441")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383325200", "202.81", "201.93", "203.26", "203.25", "167.03821298", "33794.4649017457", "202.315771336658")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383328800", "203.25", "202.21", "203.26", "203.23", "150.87255709", "30590.7686963505", "202.75899929304")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383332400", "203.23", "202.23", "203.27", "203.1", "82.84681705", "16786.8513423445", "202.625181510754")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383336000", "203.1", "201.14", "203.26", "202.98", "229.25035033", "46332.7398093689", "202.105426415594")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383339600", "202.98", "202.13", "203.37", "203.37", "648.93683974", "131825.0951488564", "203.140100971418")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383343200", "203.37", "202.24", "203.37", "203.0", "72.62220339", "14738.2154002232", "202.94365513912")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383346800", "203.0", "202.14", "203.37", "202.15", "157.02643544", "31891.7026778759", "203.097666889737")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383350400", "202.15", "201.2", "203.28", "201.95", "297.28275882", "60048.1753897047", "201.990104061375")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383354000", "201.95", "201.95", "203.2", "203.2", "91.69331718", "18626.7859658392", "203.142241318127")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383357600", "203.2", "202.02", "203.37", "203.37", "50.6177781", "10271.0488853519", "202.9138628934")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383361200", "203.37", "202.31", "203.38", "203.3", "89.95058525", "18289.7922433528", "203.331553569328")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383364800", "203.3", "202.66", "204.98", "203.74", "1771.38958881", "361839.1267061753", "204.268518338337")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383368400", "203.74", "203.74", "204.87", "204.5", "80.6564564", "16495.2435070578", "204.512375615077")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383372000", "204.5", "203.81", "204.5", "204.5", "55.51283009", "11351.0168486738", "204.47555691668")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383375600", "204.5", "203.74", "204.88", "204.4", "131.44108141", "26860.4422557475", "204.353478894187")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383379200", "204.4", "203.81", "204.4", "204.29", "49.83444431", "10178.543582605", "204.247157233026")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383382800", "204.29", "203.81", "204.5", "204.49", "129.33321547", "26437.8591299359", "204.416622859488")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383386400", "204.49", "203.14", "205.0", "204.88", "925.24412135", "189194.0251858423", "204.480115917725")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383390000", "204.88", "203.14", "204.93", "204.9", "188.43857281", "38560.5539851958", "204.631957301417")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383393600", "204.9", "203.33", "205.0", "203.5", "570.44640549", "116683.219593047", "204.547208063865")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383397200", "203.5", "203.32", "205.0", "204.99", "283.59189795", "58062.231686239", "204.73868296645")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383400800", "204.99", "202.68", "205.15", "204.1", "420.68294278", "85654.279042631", "203.60768249029")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383404400", "204.1", "203.16", "204.6", "204.0", "174.3682616", "35581.8329074979", "204.061407626592")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383408000", "204.0", "203.19", "204.69", "204.65", "212.61034354", "43380.8438851659", "204.039197542637")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383411600", "204.65", "203.45", "204.65", "204.22", "120.8560962", "24631.5880454289", "203.809231142689")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383415200", "204.22", "203.53", "204.68", "204.66", "159.34914011", "32551.6913550808", "204.279052479418")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383418800", "204.66", "203.57", "204.98", "204.7", "310.68494091", "63655.9254580449", "204.888995493621")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383422400", "204.7", "203.73", "204.98", "203.78", "84.11619326", "17200.1640834294", "204.481009147243")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383426000", "203.78", "203.7", "204.94", "204.52", "237.13832886", "48543.4042945522", "204.705011323627")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383429600", "204.52", "203.8", "204.93", "204.5", "59.8232534", "12242.4014528288", "204.642856365095")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383433200", "204.5", "204.3", "204.9", "204.86", "105.0357053", "21511.8124253543", "204.804760094798")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383436800", "204.86", "203.72", "204.87", "204.86", "209.89268467", "42943.9853960211", "204.599724204487")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383440400", "204.86", "203.64", "204.88", "204.86", "692.40783867", "141673.3013202485", "204.609615038991")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383444000", "204.86", "203.51", "204.86", "204.7", "134.43767862", "27449.0282528611", "204.176600895112")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383447600", "204.7", "203.53", "204.88", "204.88", "429.92300046", "87992.3714760472", "204.670072040571")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383451200", "204.88", "203.56", "204.88", "204.85", "55.15816864", "11285.2257647203", "204.597542720742")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383454800", "204.85", "203.8", "204.87", "204.86", "8.62735843", "1763.0148744744", "204.351643527856")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383458400", "204.86", "203.92", "204.88", "203.94", "35.51757917", "7265.602690858", "204.563567130581")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383462000", "203.94", "203.8", "204.87", "204.79", "18.66673119", "3820.2236839968", "204.654132805177")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383465600", "204.79", "203.56", "204.88", "204.84", "83.50585383", "17060.7138259915", "204.305603062553")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383469200", "204.84", "203.76", "204.81", "204.3", "40.37876048", "8248.2319929135", "204.271550064023")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383472800", "204.3", "203.63", "204.41", "203.99", "98.28884949", "20038.3424510467", "203.871980952279")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383476400", "203.99", "203.63", "204.42", "203.7", "177.29806787", "36119.2388754371", "203.720431414519")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383480000", "203.7", "203.71", "204.98", "204.45", "359.57060199", "73641.7153622178", "204.804605700957")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383483600", "204.45", "203.73", "204.45", "203.81", "101.71603812", "20781.7098164758", "204.311042787161")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383487200", "203.81", "203.81", "205.0", "204.31", "798.9004038", "163463.6603437987", "204.610812018967")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383490800", "204.31", "204.32", "205.49", "205.16", "913.26874511", "187342.2518086575", "205.133760255962")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383494400", "205.16", "205.16", "206.47", "206.47", "1427.07284362", "294117.1502352005", "206.098204131701")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383498000", "206.47", "206.06", "206.85", "206.85", "621.55227555", "128401.1456328734", "206.581410259103")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383501600", "206.85", "206.46", "208.0", "207.75", "1806.85473841", "374736.9346457178", "207.397377708116")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383505200", "207.75", "206.5", "208.0", "207.5", "433.57993136", "89895.6450090304", "207.333500715904")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383508800", "207.5", "207.14", "208.52", "208.49", "269.45973761", "56109.0244309631", "208.227859674427")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383512400", "208.49", "206.33", "208.97", "207.2", "1439.43885346", "299640.5050851553", "208.164802808334")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383516000", "207.2", "207.2", "209.09", "209.09", "353.59753107", "73857.0690043697", "208.873259892046")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383519600", "209.09", "209.0", "210.0", "210.0", "718.84868811", "150871.8823917559", "209.879888337042")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383523200", "210.0", "210.0", "211.97", "211.95", "765.12861314", "161514.1464582332", "211.094113701221")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383526800", "211.95", "210.8", "212.97", "212.88", "497.38892722", "105548.3281714263", "212.204820805633")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383530400", "212.88", "211.08", "212.97", "211.73", "191.07553594", "40661.3990832864", "212.802747788993")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383534000", "211.73", "209.8", "211.98", "210.99", "195.07518444", "41063.9607639207", "210.503252280922")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383537600", "210.99", "207.22", "211.0", "210.96", "348.86388283", "73155.6625608123", "209.696865056281")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383541200", "210.96", "210.87", "211.0", "210.97", "92.15947081", "19442.7294107754", "210.968327399138")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383544800", "210.97", "209.5", "211.0", "211.0", "132.51676737", "27907.762049262", "210.597968869409")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383548400", "211.0", "210.8", "211.99", "211.99", "420.19145992", "88679.7567088963", "211.04607105956")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383552000", "211.99", "211.23", "212.19", "211.31", "237.1777044", "50268.0545747807", "211.942579939991")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383555600", "211.31", "211.12", "212.97", "212.97", "788.93498188", "167388.0468829883", "212.169634668891")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383559200", "212.97", "212.23", "214.8", "214.69", "1673.40466723", "358149.7412091925", "214.024586056665")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383562800", "214.69", "214.69", "215.9", "215.9", "2263.92807317", "487792.6451534918", "215.462960565913")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383566400", "215.9", "215.3", "217.67", "217.64", "1667.25605785", "360261.0196891139", "216.080198355186")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383570000", "217.64", "216.22", "217.89", "217.76", "547.72420107", "119095.3890645726", "217.436784483715")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383573600", "217.76", "217.42", "219.0", "219.0", "2059.14924761", "449394.7400535094", "218.242917833718")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383577200", "219.0", "218.99", "225.47", "223.04", "3139.75249435", "699737.8976221689", "222.864031123903")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383580800", "223.04", "217.0", "223.49", "220.92", "2029.03673654", "445960.3343606921", "219.789186824269")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383584400", "220.92", "221.1", "225.68", "224.0", "1954.01317477", "438077.7813161211", "224.193872883016")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383588000", "224.0", "223.93", "227.51", "226.42", "2420.19671369", "545552.4717246552", "225.416582312795")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383591600", "226.42", "225.01", "227.63", "226.1", "1278.45940208", "289672.5984862767", "226.579426781164")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383595200", "226.1", "225.05", "227.4", "225.47", "873.02747794", "197012.7014670589", "225.666094647939")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383598800", "225.47", "225.0", "226.7", "225.11", "880.17706159", "198552.9506558716", "225.582964292656")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383602400", "225.11", "224.0", "226.95", "226.95", "1252.82167614", "282629.7078806286", "225.594522559207")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383606000", "226.95", "226.83", "230.4", "226.83", "2554.32863645", "583588.1828418676", "228.470281589504")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383609600", "226.83", "226.51", "230.49", "229.21", "584.23893408", "133800.838118503", "229.017325470099")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383613200", "229.21", "226.31", "230.77", "230.77", "387.44337712", "88351.8984509816", "228.0382209853")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383616800", "230.77", "230.27", "231.75", "231.3", "374.8163302", "86530.4374625662", "230.860905703852")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383620400", "231.3", "228.61", "231.75", "229.79", "409.64697636", "94157.6824865627", "229.850793293337")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383624000", "229.79", "228.62", "231.25", "230.7", "136.35212958", "31418.3973605539", "230.421024279787")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383627600", "230.7", "226.24", "231.0", "228.0", "688.64329554", "157195.5399792984", "228.268453344969")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383631200", "228.0", "226.0", "229.65", "229.3", "1408.98655273", "319828.3438141961", "226.991764537787")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383634800", "229.3", "228.01", "231.34", "229.2", "817.37742515", "188098.6797558866", "230.12463271954")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383638400", "229.2", "228.15", "229.5", "229.46", "522.46475969", "119739.3791672787", "229.181733210725")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383642000", "229.46", "228.15", "229.5", "229.5", "444.67557846", "101937.5663990731", "229.240307624051")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383645600", "229.5", "228.41", "231.78", "231.78", "1720.00600907", "397361.0692479671", "231.023070357074")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383649200", "231.78", "230.1", "235.0", "234.57", "1980.69771577", "462975.6914504966", "233.743739776322")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383652800", "234.57", "234.0", "236.85", "236.85", "859.66448387", "202226.8480431151", "235.239272806454")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383656400", "236.85", "234.28", "239.8", "239.49", "1897.4882486", "451473.8839273656", "237.932374158561")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383660000", "239.49", "238.03", "244.0", "244.0", "2846.21728089", "687063.2478955915", "241.395220424193")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383663600", "244.0", "240.0", "245.56", "244.07", "3610.86664105", "877014.4770962134", "242.881990468965")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383667200", "244.07", "243.05", "247.97", "247.39", "3276.56484011", "807092.1599789631", "246.322657833278")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383670800", "247.39", "241.2", "248.97", "246.69", "2152.0990223", "526493.4375216136", "244.641827381594")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383674400", "246.69", "245.7", "250.79", "247.8", "3292.40141216", "820327.198762567", "249.157710761759")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383678000", "247.8", "244.0", "250.0", "244.0", "1130.33803947", "279360.677225263", "247.147903963536")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383681600", "244.0", "228.63", "245.18", "236.45", "9602.19296935", "2250686.204923719", "234.392936291519")));
		dataList.add(new ArrayList<String>(Arrays.asList("1383685200", "236.45", "236.0", "243.0", "241.0", "2559.53381256", "610922.2164432697", "238.684956395335")));

		ChartData[] chartData = new ChartData[dataList.size()];
		for (int i = 0; i < chartData.length; i++) {
			chartData[i] = new ChartData(dataList.get(i));
		}
		return chartData;
	}

	/**
	 * Process a market order.
	 * @param order the order to be processed
	 * @param marketData the current market data
	 */
	private void processMarketOrder(MarketOrder order, ChartData marketData) {
		if (order != null) {
			if (order.getType() == Order.OrderType.BID) {
				// Buy
				if (isEnoughUsd(order, marketData)) {
					currentUsdBalance = currentUsdBalance.subtract(order.getTradableAmount().multiply(marketData.getWeightedPrice()));
					currentBtcBalance = currentBtcBalance.add(order.getTradableAmount());
				}
			} else if (order.getType() == Order.OrderType.ASK) {
				// Sell
				if (isEnoughBtc(order)) {
					currentBtcBalance = currentBtcBalance.subtract(order.getTradableAmount());
					currentUsdBalance = currentUsdBalance.add(order.getTradableAmount().multiply(marketData.getWeightedPrice()));
				}
			}
		}
	}

	/**
	 * @param order the order to be placed
	 * @param marketData the current market data
	 * @return true if there is enough money to place the order, false otherwise
	 */
	private boolean isEnoughUsd(Order order, ChartData marketData) {
		if (order.getType() == Order.OrderType.BID) {
			return (order.getTradableAmount().multiply(marketData.getWeightedPrice()).compareTo(currentUsdBalance) <= 0);
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
