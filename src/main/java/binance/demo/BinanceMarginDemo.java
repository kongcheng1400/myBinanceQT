package binance.demo;


import binance.exchange.BinanceMarginRest;
import binance.service.BinanceMarginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.currency.CurrencyPair;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class BinanceMarginDemo {
    public static void main(String[] args) throws BinanceException, IOException {
        // TODO Auto-generated method stub
        CurrencyPair cp = CurrencyPair.BTC_USDT;
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        BinanceExchange exchange = (BinanceExchange) BinanceDemoUtils.createExchange();
        BinanceMarginRest binanceMarginRest = ExchangeRestProxyBuilder.forInterface(
                BinanceMarginRest.class, exchange.getExchangeSpecification())
                .build();
        BinanceMarginService myBMS = new BinanceMarginService(exchange, binanceMarginRest, exchange.getResilienceRegistries());
        log.info(om.writeValueAsString(myBMS.getSystemStatus()));
        log.info(om.writeValueAsString(myBMS.getMarginAccount()));
    }
}
