package binance.spotTrade;

public class TaskCleanOrders implements Runnable{
    private ExchangeEntry exen;
    public TaskCleanOrders(ExchangeEntry exen) {
        this.exen = exen;
    }
    @Override
    public void run() {
        exen.cancellOrder();
    }
}
