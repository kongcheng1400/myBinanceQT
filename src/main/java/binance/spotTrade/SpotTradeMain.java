package binance.spotTrade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpotTradeMain {
    public static void main(String[] args) throws InterruptedException, IOException {
        CurrencyPair cp = CurrencyPair.BTC_USDT;
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        BinanceExchange exchange = (BinanceExchange) BinanceDemoUtils.createExchange();
        ExchangeEntry exen = new ExchangeEntry(exchange);
        exen.getExchangeStatus();


        /*
        int balancePositionInterval = 3000;
        int spotTradeInterval = 600;
        int cleanOrdersInterval = 1200;

        ScheduledExecutorService go = Executors.newScheduledThreadPool(4);
        go.scheduleWithFixedDelay(new TaskSpotTrade(exen), 10, spotTradeInterval, TimeUnit.MILLISECONDS);
        go.scheduleWithFixedDelay(new TaskBalancePosition(exen), 600, balancePositionInterval, TimeUnit.MILLISECONDS);
        go.scheduleWithFixedDelay(new TaskCleanOrders(exen), 800, cleanOrdersInterval, TimeUnit.MILLISECONDS);

        Thread.sleep(20000);
        go.shutdownNow();
        */
    }
}
