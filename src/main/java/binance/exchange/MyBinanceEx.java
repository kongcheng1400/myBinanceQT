package binance.exchange;


import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;

public class MyBinanceEx {
    Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class);
    static {
        System.out.println("Hello, Git!");
    }
}
