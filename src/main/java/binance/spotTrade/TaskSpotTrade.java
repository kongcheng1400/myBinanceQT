package binance.spotTrade;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TaskSpotTrade implements Runnable{
    static final int MaxQLength = 16;
    static final int MinQLength = 8;
    static final double PositionPct = 0.5;
    static final double BurstThresholdPct = 0.0005;

    private ExchangeEntry exen;
    private CurrencyPair cp;
    private Integer orderbookDepth = 5;
    boolean bull = false;
    boolean bear = false;
    Queue<BigDecimal> tickerQ = new LinkedList<>();
    Queue<Double> tradesAmount = new LinkedList<>();
    private double latestTickerPrice;
    private double buyAt = 0.0;
    private double sellAt = 0.0;

    double myNetAssets = 0.0;

    public TaskSpotTrade(ExchangeEntry exen, CurrencyPair cp, Integer depth) {
        this.exen = exen;
        this.cp = cp;
        this.orderbookDepth = depth;
        myNetAssets = getNetAssets();
        log.info("at the beginning of trade, the initial net assets:{}", myNetAssets);
    }

    private double getNetAssets() {
        List<Double> assetList = exen.getAvailableAsset(cp);
        if (assetList != null) {

            myNetAssets = assetList.get(0) * exen.getPrice(cp).getLast().doubleValue() + assetList.get(1);
            log.info("Net assets: {}USDT; {}:{}, {}:{}", myNetAssets,
                    cp.base.toString(), assetList.get(0),
                    cp.counter.toString(), assetList.get(1));
            return  myNetAssets;
        }
        return 0;
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
                log.info("updating trades q: lastest is {}", amount);
            }
            List<String> strList = tradesAmount.stream().map(t->String.format("%.3f  ", t)).collect(Collectors.toList());
            log.info("after trades updates: "+ strList.toString());
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
                buyAt = Double.valueOf(String.format("%.2f", askPrice.doubleValue() * 0.618 + bidPrice.doubleValue() * 0.382 + 0.01));
                sellAt = Double.valueOf(String.format("%.2f", askPrice.doubleValue() * 0.382 + bidPrice.doubleValue() * 0.618 - 0.01));
                log.info("updating Orderbook, buy at {}, sell at {}", String.format("%9.3f", buyAt), String.format("%9.2f", sellAt));
            }

    }

    //update tickerQueue
    private void updateTickerQueue() {
        OrderBook ob = exen.getOrderbook(cp, orderbookDepth);
            //calculate the order price.
            //
        if (ob != null) {
            BigDecimal bidPrice;
            BigDecimal askPrice;
            bidPrice = ob.getBids().get(0).getLimitPrice();
            askPrice = ob.getAsks().get(0).getLimitPrice();

            //optical

            buyAt = askPrice.doubleValue() * 0.618 + bidPrice.doubleValue() * 0.382 + 0.01;
            sellAt = askPrice.doubleValue() * 0.382 + bidPrice.doubleValue() * 0.618 - 0.01;

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
            log.info("now ticker queue is : " + strList.toString());
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


            if (len > 2 && ((latestPrice - max1 > burstPrice) ||
                    ((latestPrice - max2) > burstPrice && tickerArray.get(len - 1).doubleValue() > tickerArray.get(len - 2).doubleValue())))
                bull = true;
            else
                bull = false;

            if (len > 2 && ((latestPrice - min1 < burstPrice * -1) ||
                    ((latestPrice - min2) < burstPrice * -1 && tickerArray.get(len - 1).doubleValue() < tickerArray.get(len - 2).doubleValue()))
            )
                bear = true;
            else
                bear = false;
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
        List<Double> assetList = null;
        if (bull == true || bear == true) {
            assetList = exen.getAvailableAsset(cp);
            if (assetList != null) {
                log.info("in SpotTrade, time:{} -- asset {}: {}, asset {}: {}",
                        DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()),
                        cp.base.toString(),
                        String.format("%.4f", assetList.get(0)),
                        cp.counter.toString(),
                        String.format("%.4f", assetList.get(1))
                );

                double amount = tradesAmount.stream().toList().get(tradesAmount.size() - 1);
                if (bull) {
                    double toBuy = Double.valueOf(String.format("%.3f", assetList.get(1) / latestTickerPrice));
                    log.info("TaskSpotTrade: to buy {} at {} with {}",
                            cp.base.toString(),
                            String.format("%4.4f", buyAt),
                            String.format("%4.4f", amount));
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
                        //exen.placeNewOrder(limitOrder);
                        toBuy -= amount;

                        //for simulation:
                        double bought = 0.0;
                        double tradingFee = 0.0;
                        bought = limitOrder.getOriginalAmount().doubleValue() * limitOrder.getLimitPrice().doubleValue();
                        tradingFee = 0.003*bought;

                    }
                }

                if (bear) {
                    double toSell = assetList.get(0) * 0.9;
                    double shortThreshhold = 0.1 * (toSell + assetList.get(1)/latestTickerPrice);
                    log.info("TaskSpotTrade: to sell {} at {} with {}",
                            cp.base.toString(),
                            String.format("%4.4f", sellAt),
                            String.format("%4.4f", toSell));

                    while (toSell > shortThreshhold) {
                        updateOrderBook();
                        LimitOrder limitOrder;
                        if (toSell < amount) {
                            limitOrder = new LimitOrder(Order.OrderType.ASK, new BigDecimal(String.format("%.3f",toSell-0.01)), cp, null, null,
                                    BigDecimal.valueOf(sellAt));
                        } else
                            limitOrder = new LimitOrder(Order.OrderType.ASK, new BigDecimal(String.format("%.3f",amount-0.01)), cp, null, null,
                                    BigDecimal.valueOf(sellAt));

                        //exen.placeNewOrder(limitOrder);

                    }
                }

            }
            getNetAssets();
        } else {
            log.info("from Task run: Not trade.");
        }
        log.info("after scan/trade system time: {}", System.currentTimeMillis());
    }
}
