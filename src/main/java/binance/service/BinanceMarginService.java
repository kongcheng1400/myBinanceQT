package binance.service;

import static org.knowm.xchange.binance.BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER;

import java.io.IOException;

import binance.dto.BinanceMarginAccountDTO;
import binance.exchange.BinanceMarginRest;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;

import org.knowm.xchange.binance.service.BinanceBaseService;
import org.knowm.xchange.client.ResilienceRegistries;


public class BinanceMarginService extends BinanceBaseService{
    public BinanceMarginService(
            BinanceExchange exchange,
            BinanceMarginRest binance,
            ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }
    /*
    static BinanceMarginRest marginService = RestProxyFactory.createProxy(BinanceMarginRest.class, "https://api.binance.com/");
    public static void main(String[] args) throws JsonProcessingException {
        // TODO Auto-generated method stub
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        BinanceSystemStatus resp = marginService.getSystemStatus();
        System.out.println(om.writeValueAsString(resp));
        //getMarginAccount(getRecvWindow(), getTimestampFactory(), apiKey, signatureCreator)

    }
    */
    public BinanceMarginAccountDTO getMarginAccount() throws BinanceException, IOException {
        return decorateApiCall(
                () -> ((BinanceMarginRest)binance).getMarginAccount(getTimestampFactory(), apiKey, signatureCreator))
                .withRetry(retry("account"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), 5)
                .call();
    }
}