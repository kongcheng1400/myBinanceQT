package binance.spotTrade;

public class TaskBalancePosition implements Runnable{
    private ExchangeEntry exen;
    public TaskBalancePosition(ExchangeEntry exen) {
        this.exen = exen;
    }

    @Override
    public void run() {
        //exen.binancePosition();
    }
}
