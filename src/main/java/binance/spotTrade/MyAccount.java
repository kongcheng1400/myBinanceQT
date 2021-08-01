package binance.spotTrade;

import java.util.ArrayList;
import java.util.List;

public class MyAccount {
    private double counter;
    private double base;
    private List<Double> assetList;

    public MyAccount(double counter, double base) {
        this.counter = counter;
        this.base = base;
        assetList = new ArrayList<>();
        assetList.add(0, base);
        assetList.add(1,counter);
    }

    public double getCounter() {
        return counter;
    }

    public void setCounter(double counter) {
        this.counter = counter;
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public List<Double> getAssetList() {
        assetList.clear();
        assetList.add(0, base);
        assetList.add(1, counter);

        return assetList;
    }


    @Override
    public String toString() {
        return "MyAccount{" +
                "counter=" + counter +
                ", base=" + base +
                '}';
    }
}
