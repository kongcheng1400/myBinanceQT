package binance.spotTrade;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class TaskSpotTrade implements Runnable{
    static final int MaxQLength = 16;
    static final double PositionPct = 0.5;
    static final double BurstThresholdPct = 0.005;

    private final ExchangeEntry exen;
    private CurrencyPair cp;
    private Integer orderbookDepth = 5;
    Queue<BigDecimal> tickerQ = new LinkedList<>();

    public TaskSpotTrade(ExchangeEntry exen, CurrencyPair cp, Integer depth) {
        this.exen = exen;
        this.cp = cp;
        this.orderbookDepth = depth;
    }

    @Override
    public void run(){
        try {
            OrderBook ob = exen.getOrderbook(cp, orderbookDepth);
            //calculate the order price.
            //
            BigDecimal bidPrice;
            BigDecimal askPrice;
            bidPrice = ob.getBids().get(0).getLimitPrice();
            askPrice = ob.getAsks().get(0).getLimitPrice();
            log.info("bid price = {}, askPrice = {}", bidPrice, askPrice);

            //optical
            double buyAt, sellAt;
            buyAt = askPrice.doubleValue() * 0.618 + bidPrice.doubleValue() * 0.382 + 0.01;
            sellAt = askPrice.doubleValue() * 0.382 + bidPrice.doubleValue() * 0.618 - 0.01;
            log.info("buy at {}, sell at {}", String.format("%9.2f", buyAt), String.format("%9.2f", sellAt));

            //tickerPrice
            double tickerPrice;
            double a1, a2, a3, b1, b2, b3;
            b1 = ob.getBids().get(0).getLimitPrice().doubleValue();
            b2 = ob.getBids().get(1).getLimitPrice().doubleValue();
            b3 = ob.getBids().get(2).getLimitPrice().doubleValue();

            a1 = ob.getAsks().get(0).getLimitPrice().doubleValue();
            a2 = ob.getAsks().get(1).getLimitPrice().doubleValue();
            a3 = ob.getAsks().get(2).getLimitPrice().doubleValue();

            tickerPrice = (a1+b1)/2 * 0.7 + (a2+b2)/2 * 0.3 + (a3+b3)/2 * 0.1;


            //keep a fixed length ticker queue.
            if (tickerQ.size() >= MaxQLength)
                tickerQ.poll();
            else
                tickerQ.offer(BigDecimal.valueOf(tickerPrice));

            //only do trade after get enough ticker data.
            if (tickerQ.size() >= MaxQLength) {
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
                boolean bull = false;
                boolean bear = false;
                if (len > 2 && ((latestPrice - max1 > burstPrice) ||
                        ((latestPrice - max2) > burstPrice && tickerArray.get(len - 1).doubleValue() > tickerArray.get(len - 2).doubleValue())))
                    bull = true;

                if (len > 2 && ((latestPrice - min1 < burstPrice * -1) ||
                        ((latestPrice - min2) < burstPrice * -1 && tickerArray.get(len - 1).doubleValue() < tickerArray.get(len - 2).doubleValue()))
                )
                    bear = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
