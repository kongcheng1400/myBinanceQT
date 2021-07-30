package binance.spotTrade;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.meta.BinanceSystemStatus;
import org.knowm.xchange.binance.service.BinanceAccountService;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public boolean getExchangeStatus()  {
        try {
            BinanceSystemStatus status = marketDataService.getSystemStatus();
            if (Integer.valueOf(status.getStatus()) == 0) {
                log.info("System status is fine, message from Binance: " + status.getMsg());
                return true;
            }
            else {
                log.info("System status is abnormal, message from Binance: " + status.getMsg());
                return false;
            }
        } catch (IOException e) {
            log.warn("getSystemStatus Error.");
            e.printStackTrace();
        }
        log.warn("Checking binance system status error!");
        return false;
    }

    public OrderBook getOrderbook(CurrencyPair cp, Integer limit)  {
        lock.lock();
        try {
            return marketDataService.getOrderBook(cp, limit);
        } catch (IOException e) {
            log.warn("retrieve orderbook failed.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public List<Trade> getTrades_limit(CurrencyPair cp, Integer limit) {
        lock.lock();
        try {
            //retrieve the 5 latest trade
            //Trades trades_5 = exchange.getMarketDataService().getTrades(cp,null, null, null,5 );
            return marketDataService.getTrades(cp,null, null, null,5).getTrades();

        } catch (IOException e) {
            log.warn("retrieve trades failed");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public ArrayList<Double> getAvailableAsset(CurrencyPair cp) {
        lock.lock();
        try {
            AccountInfo myAcc = bex.getAccountService().getAccountInfo();
            double cpBase = myAcc.getWallet().getBalance(cp.base).getAvailable().doubleValue();
            double cpCounter = myAcc.getWallet().getBalance(cp.counter).getAvailable().doubleValue();
            ArrayList<Double> myArr = new ArrayList<>();
            myArr.add(cpBase);
            myArr.add(cpCounter);

            return myArr;

        } catch (IOException e) {
            log.warn("get my Asset failed");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String placeNewOrder(LimitOrder limitOrder)
    {
        lock.lock();
        try {
            return tradeService.placeLimitOrder(limitOrder);
        } catch (IOException e) {
            log.warn("get my Asset failed");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return null;

    }



    public Ticker getPrice(CurrencyPair cp)
    {
        lock.lock();
        try {
            return marketDataService.getTicker(cp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }
}
