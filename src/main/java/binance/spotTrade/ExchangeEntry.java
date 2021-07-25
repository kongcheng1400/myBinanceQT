package binance.spotTrade;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.meta.BinanceSystemStatus;
import org.knowm.xchange.binance.service.BinanceAccountService;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ExchangeEntry {
    private BinanceExchange bex;
    private Lock lock = new ReentrantLock();
    private String exName;
    private BinanceMarketDataService marketDataService;
    private BinanceAccountService accountService;
    private BinanceTradeService tradeService;
    public ExchangeEntry(BinanceExchange bex) {
        this.bex = bex;
        this.exName = bex.getClass().getName();
        this.marketDataService = (BinanceMarketDataService) bex.getMarketDataService();
        this.accountService = (BinanceAccountService) bex.getAccountService();
        this.tradeService = (BinanceTradeService) bex.getTradeService();
    }

    public boolean getExchangeStatus() throws IOException {
        BinanceSystemStatus status = marketDataService.getSystemStatus();

        if (Integer.valueOf(status.getStatus()) == 0) {
            log.info("System status is fine, message from Binance: " + status.getMsg());
            return true;
        }
        else {
            log.info("System status is abnormal, message from Binance: " + status.getMsg());
            return false;
        }
    }

    public OrderBook getOrderbook(CurrencyPair cp, Integer limit) throws IOException {
        lock.lock();
        try {
            return marketDataService.getOrderBook(cp, limit);
        } finally {
            lock.unlock();
        }
    }

    public void getPrice(CurrencyPair cp)
    {
        lock.lock();
        try {
            marketDataService.getTicker(cp);
            System.out.println(exName + " get price.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    public void trade()
    {
        lock.lock();
        try {
            System.out.println(exName + " issue trade.");
        } finally {
            lock.unlock();
        }
    }

    public void cancellOrder()
    {
        lock.lock();
        try {
            System.out.println(exName + " cancell Orders.");
        } finally {
            lock.unlock();
        }
    }

    public void binancePosition()
    {
        lock.lock();
        try {
            System.out.println(exName + " balancing positions");
        } finally {
            lock.unlock();
        }
    }
}
