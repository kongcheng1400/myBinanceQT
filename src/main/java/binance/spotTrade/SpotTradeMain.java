package binance.spotTrade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.account.BinanceAccountInformation;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpotTradeMain {
    public static void main(String[] args) throws InterruptedException, IOException {
        CurrencyPair cp = new CurrencyPair(Currency.ETH, Currency.USDT);
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        BinanceExchange exchange = (BinanceExchange) BinanceDemoUtils.createExchange();
        ExchangeEntry exen = new ExchangeEntry(exchange);
        exen.getExchangeStatus();
        //AccountInfo accInfo = exchange.getAccountService().getAccountInfo();

        /*
        int balancePositionInterval = 3000;

        int cleanOrdersInterval = 1200;



        go.scheduleWithFixedDelay(new TaskBalancePosition(exen), 600, balancePositionInterval, TimeUnit.MILLISECONDS);
        go.scheduleWithFixedDelay(new TaskCleanOrders(exen), 800, cleanOrdersInterval, TimeUnit.MILLISECONDS);

        Thread.sleep(20000);
        go.shutdownNow();
        */
        int spotTradeInterval = 1000;
        ScheduledExecutorService go = Executors.newScheduledThreadPool(1);
        go.scheduleWithFixedDelay(new TaskSpotTrade(exen, cp, 5), 10, spotTradeInterval, TimeUnit.MILLISECONDS);
        //Thread.sleep(240000);
        //go.shutdownNow();
    }
}
