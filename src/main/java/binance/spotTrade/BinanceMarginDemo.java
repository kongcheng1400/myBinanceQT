package binance.spotTrade;


import binance.exchange.BinanceMarginRest;
import binance.service.BinanceMarginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
public class BinanceMarginDemo {
    public static void main(String[] args) throws BinanceException, IOException {
        // TODO Auto-generated method stub
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        BinanceExchange exchange = (BinanceExchange) BinanceDemoUtils.createExchange();
        CurrencyPair cp =new CurrencyPair("BNB", "USDT");
        /*
        BinanceMarginRest binanceMarginRest = ExchangeRestProxyBuilder.forInterface(
                BinanceMarginRest.class, exchange.getExchangeSpecification())
                .build();
        BinanceMarginService myBMS = new BinanceMarginService(exchange, binanceMarginRest, exchange.getResilienceRegistries());
        log.info(om.writeValueAsString(myBMS.getSystemStatus()));
        log.info(om.writeValueAsString(myBMS.getMarginAccount()));
        */

        log.info(om.writeValueAsString(((BinanceMarketDataService)exchange.getMarketDataService()).getSystemStatus()));
        Trades trades_5 = exchange.getMarketDataService().getTrades(cp,null, null, null,5 );
        for (Trade tr: trades_5.getTrades())
            log.info(tr.toString());
        //log.info(exchange.getAccountService().getAccountInfo().toString());

        AccountInfo myAcc = exchange.getAccountService().getAccountInfo();
        ExchangeEntry exen = new ExchangeEntry(exchange);

        double myUSDT = myAcc.getWallet().getBalance(Currency.USDT).getAvailable().doubleValue();
        double myBNB = myAcc.getWallet().getBalance(Currency.BNB).getAvailable().doubleValue();
        log.info("My USDT balance: " + myUSDT);
        log.info("My BNB balance: " + myBNB);
        /*
        double amount = 0.1;
        double buyAt = 302;
        LimitOrder limitOrder = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(amount),cp,null,null,
                BigDecimal.valueOf(buyAt));
        String resp = exchange.getTradeService().placeLimitOrder(limitOrder);
        log.info("Limit order with response: " + resp);
         */

    }
}
