package binance.spotTrade;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TaskSpotTrade implements Runnable{
    static final int MaxQLength = 16;
    static final int MinQLength = 8;
    //static final double BurstThresholdPct = 0.0005;
    static final double BurstThresholdPct = 0.0005;
    static final double TradingFeeRate = 0.00075;

    private final ExchangeEntry exen;
    private final CurrencyPair cp;
    private final Integer orderbookDepth;
    boolean bull = false;
    boolean bear = false;
    Queue<BigDecimal> tickerQ = new LinkedList<>();
    Queue<Double> tradesAmount = new LinkedList<>();
    private double latestTickerPrice;
    private double buyAt = 0.0;
    private double sellAt = 0.0;


    double myInitialAssets;
    double myNetAssets = 0.0;
    MyAccount myAcc;
    int sellTimes = 0;
    int buyTimes = 0;
    int totalTradingTimes = 0;
    double totalSell = 0;
    double totalBuy = 0;
    double totalTradingFees = 0;
    double totalTradingAmount = 0;


    public TaskSpotTrade(ExchangeEntry exen, CurrencyPair cp, Integer depth) {
        this.exen = exen;
        this.cp = cp;
        this.orderbookDepth = depth;
        myAcc = new MyAccount(1000, 0.3);
        myInitialAssets = myAcc.getBase() * exen.getPrice(cp).getLast().doubleValue() + myAcc.getCounter();
        log.info("at the beginning of simulation,My Net assets:{}, {} {},{} {}", myInitialAssets,
                String.format("%.3f", myAcc.getBase()),
                cp.base,
                String.format("%.3f", myAcc.getCounter()),
                cp.counter
                );
    }


    private void tradingSummary() {
        myNetAssets = myAcc.getBase() * latestTickerPrice + myAcc.getCounter();
        log.info("total Assets:{} {}, {}:{}, {}:{}, profits={}, Fees={}",
                String.format("%.4f",myNetAssets), cp.counter,
                cp.base, String.format("%.4f",myAcc.getBase()),
                cp.counter, String.format("%.4f",myAcc.getCounter()),
                String.format("%.4f",myNetAssets - myInitialAssets),
                String.format("%.4f",totalTradingFees)
        );
        log.info("total trading {} times, buy {} times, sell {} times, total buy {} {}, total sell {} {}, total trading:{} {}, fees:{}",
                totalTradingTimes, buyTimes, sellTimes,
                String.format("%.3f",totalBuy), cp.base, String.format("%.3f",totalSell), cp.base,
                String.format("%.3f", totalTradingAmount),cp.counter,
                String.format("%.3f",totalTradingFees)
                );
    }

    //try to smooth the trades: using the 5last trading amount as the reference.
    private void updateTrades() {
        List<Trade> tradeList = exen.getTrades_limit(cp, 5);
        if (tradeList != null) {
            double amount = 0;
            for (Trade tr : tradeList) {
                amount += tr.getOriginalAmount().doubleValue();
            }

            if (tradesAmount.size() > 1) {
                double lastAmount;
                lastAmount = tradesAmount.stream().toList().get(tradesAmount.size() - 1);
                amount = 0.7 * amount + 0.3 * lastAmount;
            }

            if (tradesAmount.size() > MaxQLength)
                tradesAmount.poll();
            else {
                tradesAmount.offer(Double.valueOf(String.format("%.4f", amount)));
                //log.info("updating trades q: lastest is {}", amount);
            }
            List<String> strList = tradesAmount.stream().map(t->String.format("%.3f  ", t)).collect(Collectors.toList());
            log.info("after trades updates: "+ strList);
        }
    }



    private void updateOrderBook()  {

            OrderBook ob = exen.getOrderbook(cp, orderbookDepth);
            if (ob != null) {

                BigDecimal bidPrice;
                BigDecimal askPrice;
                bidPrice = ob.getBids().get(0).getLimitPrice();
                askPrice = ob.getAsks().get(0).getLimitPrice();
                log.info("bid price = {}, askPrice = {}", bidPrice, askPrice);

                //optimise price
                buyAt = Double.parseDouble(String.format("%.3f", askPrice.doubleValue() * 0.618 + bidPrice.doubleValue() * 0.382 ));
                sellAt = Double.parseDouble(String.format("%.3f", askPrice.doubleValue() * 0.382 + bidPrice.doubleValue() * 0.618 ));
                log.info("updating Orderbook, buy at {}, sell at {}", String.format("%.3f", buyAt), String.format("%.2f", sellAt));
            }

    }

    //update tickerQueue
    private void updateTickerQueue() {
        OrderBook ob = exen.getOrderbook(cp, orderbookDepth);
            //calculate the order price.
            //
        if (ob != null) {

            //tickerPrice
            double a1, a2, a3, b1, b2, b3;
            b1 = ob.getBids().get(0).getLimitPrice().doubleValue();
            b2 = ob.getBids().get(1).getLimitPrice().doubleValue();
            b3 = ob.getBids().get(2).getLimitPrice().doubleValue();

            a1 = ob.getAsks().get(0).getLimitPrice().doubleValue();
            a2 = ob.getAsks().get(1).getLimitPrice().doubleValue();
            a3 = ob.getAsks().get(2).getLimitPrice().doubleValue();
            /*
            log.info("a1 = {}, a2 = {}, a3 = {}, b1 = {}, b2= {}, b3 = {}",
                    String.format("%.4f", a1),
                    String.format("%.4f", a2),
                    String.format("%.4f", a3),
                    String.format("%.4f", b1),
                    String.format("%.4f", b2),
                    String.format("%.4f", b3)
            );
            */

            latestTickerPrice = (a1+b1)/2 * 0.7 + (a2+b2)/2 * 0.2 + (a3+b3)/2 * 0.1;


            //keep a fixed length ticker queue.
            //update the ticker price queue.
            if (tickerQ.size() >= MaxQLength)
                tickerQ.poll();

            tickerQ.offer(BigDecimal.valueOf(latestTickerPrice));
            //log.info("update ticker queue with value of {}", String.format("%.3f",latestTickerPrice));

            List<String> strList = tickerQ.stream().map(t->String.format("%.3f ", t.doubleValue())).collect(Collectors.toList());
            log.info("now ticker queue is : " + strList);
        }
    }

    private void calculateBullBear() {
        //only do trade after get enough ticker data.
        if (tickerQ.size() >= MinQLength) {
            //bull calculation
            int len = tickerQ.size();
            ArrayList<BigDecimal> tickerArray = new ArrayList<>(tickerQ);
            double burstPrice = tickerArray.get(len - 1).doubleValue() * BurstThresholdPct;
            //max value at ticker at len-6 and len-2
            double max1 = Collections.max(tickerArray.subList(len - 6, len - 1)).doubleValue();
            double max2 = Collections.max(tickerArray.subList(len - 6, len - 2)).doubleValue();
            double min1 = Collections.min(tickerArray.subList(len - 6, len - 1)).doubleValue();
            double min2 = Collections.min(tickerArray.subList(len - 6, len - 2)).doubleValue();
            double latestPrice = tickerArray.get(len - 1).doubleValue();

            log.info("analyzing the ticker que to check the trend:");
            log.info("burstPrice = {}, max1 = {}, max2 = {}, min1 = {}, min2 = {}, latestPrice = {}",
                    String.format("%.3f",burstPrice),
                    String.format("%.3f",max1),
                    String.format("%.3f",max2),
                    String.format("%.3f",min1),
                    String.format("%.3f",min2),
                    String.format("%.3f",latestPrice));


            bull = (latestPrice - max1 > burstPrice) ||
                    ((latestPrice - max2) > burstPrice && tickerArray.get(len - 1).doubleValue() > tickerArray.get(len - 2).doubleValue());

            bear = (latestPrice - min1 < burstPrice * -1) ||
                    ((latestPrice - min2) < burstPrice * -1 && tickerArray.get(len - 1).doubleValue() < tickerArray.get(len - 2).doubleValue());


        }

        log.info("bull = {}, bear = {}", bull, bear);

    }

    /*
    * 1.取得当前价格更新ticker价格队列
    * 2.取得当前成交量更新成交量队列
    * 3.计算趋势及下单
    * */
    @Override
    public void run(){
        log.info("Cuurent system time: {}", System.currentTimeMillis());
        updateTrades();
        updateTickerQueue();
        calculateBullBear();
        List<Double> assetList;
        if (bull || bear) {
            assetList = myAcc.getAssetList();
            if (assetList != null) {

                double amount = tradesAmount.stream().toList().get(tradesAmount.size() - 1);
                if (bull) {
                    double toBuy = Double.parseDouble(String.format("%.3f", assetList.get(1) / latestTickerPrice));
                    while (toBuy > 0.2) {
                        updateOrderBook();
                        LimitOrder limitOrder;
                        if (toBuy < amount) {
                            limitOrder = new LimitOrder(Order.OrderType.BID, new BigDecimal(String.format("%.3f", toBuy- 0.01)), cp, null, null,
                                    BigDecimal.valueOf(buyAt));
                        } else
                        {
                            limitOrder = new LimitOrder(Order.OrderType.BID, new BigDecimal(String.format("%.3f", amount-0.01)), cp, null, null,
                                    BigDecimal.valueOf(buyAt));
                        }
                        toBuy -= amount;

                        //for simulation:
                        double bought;
                        double tradingFee;
                        bought = limitOrder.getOriginalAmount().doubleValue() * limitOrder.getLimitPrice().doubleValue();
                        tradingFee = TradingFeeRate * bought;
                        myAcc.setBase(myAcc.getBase() + limitOrder.getOriginalAmount().doubleValue());
                        myAcc.setCounter(myAcc.getCounter() - tradingFee - bought);
                        totalTradingTimes += 1;
                        buyTimes+=1;
                        totalTradingFees += tradingFee;
                        totalTradingAmount += bought;
                        totalBuy += limitOrder.getOriginalAmount().doubleValue();
                        log.info("TaskSpotTrade LimitOrder:  buy {} {} at {}",
                                String.format("%.3f", limitOrder.getOriginalAmount().doubleValue()),
                                cp.base.toString(),
                                String.format("%.3f", limitOrder.getLimitPrice().doubleValue()));
                    }
                }

                if (bear) {
                    double toSell = assetList.get(0) * 0.9;
                    double shortThreshhold = 0.1 * (toSell + assetList.get(1)/latestTickerPrice);
                    while (toSell > shortThreshhold) {
                        updateOrderBook();
                        LimitOrder limitOrder;
                        if (toSell < amount) {
                            limitOrder = new LimitOrder(Order.OrderType.ASK, new BigDecimal(String.format("%.3f",toSell-0.01)), cp, null, null,
                                    BigDecimal.valueOf(sellAt));
                        } else
                            limitOrder = new LimitOrder(Order.OrderType.ASK, new BigDecimal(String.format("%.3f",amount-0.01)), cp, null, null,
                                    BigDecimal.valueOf(sellAt));
                        toSell -= amount;

                        //for simulation:
                        double sought = limitOrder.getOriginalAmount().doubleValue() * limitOrder.getLimitPrice().doubleValue();
                        double tradingFee = TradingFeeRate * sought;
                        myAcc.setBase(myAcc.getBase() - limitOrder.getOriginalAmount().doubleValue());
                        myAcc.setCounter(myAcc.getCounter() - tradingFee + sought);
                        totalTradingTimes += 1;
                        sellTimes+=1;
                        totalTradingFees += tradingFee;
                        totalTradingAmount += sought;
                        totalSell += limitOrder.getOriginalAmount().doubleValue();
                        log.info("TaskSpotTrade LimitOrder:  sell {} {} at {}",
                                String.format("%.3f", limitOrder.getOriginalAmount().doubleValue()),
                                cp.base.toString(),
                                String.format("%.3f", limitOrder.getLimitPrice().doubleValue()));
                    }
                }

            }
            tradingSummary();
        } else {
            log.info("from Task run: Not trade.");
        }
        log.info("after scan/trade system time: {}", System.currentTimeMillis());
    }
}
